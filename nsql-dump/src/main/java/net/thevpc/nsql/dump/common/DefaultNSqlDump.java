/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nsql.dump.common;

import net.thevpc.lib.nserializer.api.*;
import net.thevpc.nsql.dump.DumpProgressEventImpl;
import net.thevpc.nsql.dump.DumpProgressEventType;
import net.thevpc.nsql.dump.DumpProgressMonitor;
import net.thevpc.nsql.dump.DumpProgressMonitors;
import net.thevpc.nsql.dump.api.NSqlDump;
import net.thevpc.nsql.dump.model.DefaultResultSetIoCell;
import net.thevpc.nsql.dump.model.TableDefinitionAsStoreStructDefinition;
import net.thevpc.nsql.dump.model.TableRowsDefinitionAsStoreRowsDefinition;
import net.thevpc.nsql.dump.options.TableRestoreOptions;
import net.thevpc.nsql.dump.util.NSqlDumpModuleInstaller;
import net.thevpc.nsql.UncheckedSqlException;
import net.thevpc.nsql.*;
import net.thevpc.nsql.model.*;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.lib.nserializer.impl.RepeatableReadIoCellArr;
import net.thevpc.lib.nserializer.model.StoreRowsDefinition;
import net.thevpc.lib.nserializer.util.StringUtils;

import java.io.*;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author vpc
 */
public class DefaultNSqlDump implements NSqlDump {
    static {
        NSqlDumpModuleInstaller.init();
    }

    public static Logger LOG = Logger.getLogger(DefaultNSqlDump.class.getName());
    private NSqlConnection connection;


    public DefaultNSqlDump(NSqlConnection connection) {
        this.connection = connection;
    }

    public NSqlConnection getConnection() {
        return connection;
    }


    public StoreRowsDefinition createRowsDefinition(ResultSetMetaData rs) {
        return new TableRowsDefinitionAsStoreRowsDefinition(connection.createRowsDefinition(rs));
    }

    public IoCell createCell(ResultSet rs, NSqlColumn column) {
        return new DefaultResultSetIoCell(column, rs);
    }

    @Override
    public void close() {
        connection.close();
    }


    protected static class PreparedStatementExt {
        PreparedStatement ps;
        String sql;
    }

    protected static class ImportDataContext implements NPrepareStatementContext {
        private PreparedStatementExt insertRowPreparedStatement;
        private TableRestoreOptions schemaMode;
        private NSqlTableId newTable;
        protected StoreRows rows;
        protected File externalLobFolder;
        protected boolean externalLob;
        protected boolean failFastSQL = false;

        @Override
        public boolean isExternalLob() {
            return externalLob;
        }

        @Override
        public Path getExternalLobFolder() {
            return externalLobFolder == null ? null : externalLobFolder.toPath();
        }
    }

    public void importData(StoreRows rows, TableRestoreOptions options) {
        ImportDataContext cc = new ImportDataContext();
        cc.schemaMode = options == null ? new TableRestoreOptions() : options;
        NSqlTableDefinition definition = ((TableDefinitionAsStoreStructDefinition) rows.getDefinition()).getTableDefinition();
        cc.newTable = new NSqlTableId(connection.getSchemaId(), definition.getTableName());
        cc.rows = rows;
        cc.externalLob = options.getLobFolder() != null;
        cc.externalLobFolder = options.getLobFolder() == null ? null : new File(options.getLobFolder(), cc.newTable.getTableName());
        if (cc.schemaMode.isClearTable()) {
            connection.deleteFromTable(cc.newTable);
        }
        DumpProgressMonitor monitor = cc.schemaMode.getMonitor();
        if (monitor == null) {
            monitor = DumpProgressMonitors.SILENT;
        }
        IoRow r = null;
        NSqlTableDefinition d2 = ((TableDefinitionAsStoreStructDefinition) rows.getDefinition()).getTableDefinition().copy();
        String newTableName = cc.newTable.getTableName();
        d2.setTableName(newTableName);
        NSqlTableDefinition newTableDef = connection.getTableDefinition(cc.newTable);
        cc.insertRowPreparedStatement = _createInsertQuery(d2);
        Map<String, NSqlColumn> newColumnsMap = newTableDef.getColumns().stream().collect(Collectors.toMap(x -> x.getColumnName().toLowerCase(), x -> x));
        //should we sort columns?
        List<NSqlColumn> newColumns2 = new ArrayList<>();
        for (NSqlColumn column : d2.getColumns()) {
            NSqlColumn u = newColumnsMap.get(column.getColumnName().toLowerCase());
            if (u == null) {
                throw new IllegalArgumentException("column " + newTableDef.getTableName() + "::" + column.getColumnName() + " not found.");
            }
            newColumns2.add(u);
        }
        StoreRowFilter filter = cc.schemaMode.getFilter();
        DumpProgressEventImpl e0 = new DumpProgressEventImpl().setTableName(newTableName).setProgress(Double.NaN);
        monitor.onEvent(e0.setEventType(DumpProgressEventType.START_TABLE).setMessage(NMsg.ofC("Importing table [%s]", newTableName)));
        long index = 0;
        long importedCount = 0;
        if (filter == null) {
            while ((r = rows.nextRow()) != null) {
                index++;
                try (IoRow ccc = new RepeatableReadIoCellArr(r)) {
                    importDataRow(ccc, newColumns2.toArray(new NSqlColumn[0]), cc);
                    monitor.onEvent(e0.setEventType(DumpProgressEventType.PROCESSED_ROW).setRowIndex(index).setRowCount(index).setMessage(NMsg.ofC("[%s] imported row %s", newTableName, index)));
                }
            }
            importedCount = index;
        } else {
            while ((r = rows.nextRow()) != null) {
                index++;
                try (IoRow ccc = new RepeatableReadIoCellArr(r)) {
                    StoreRowAction rr = filter.accept(ccc, index);
                    if (rr.isAccept()) {
                        importedCount++;
                        importDataRow(ccc, newColumns2.toArray(new NSqlColumn[0]), cc);
                        monitor.onEvent(e0.setEventType(DumpProgressEventType.PROCESSED_ROW).setRowIndex(index).setRowIndex(importedCount).setMessage(NMsg.ofC("[%s] imported row %s", newTableName, index)));
                    } else {
                        monitor.onEvent(e0.setEventType(DumpProgressEventType.SKIPPED_ROW).setRowIndex(index).setRowIndex(importedCount).setMessage(NMsg.ofC("[%s] skipped row %s", newTableName, index)));
                    }
                    if (rr.isStop()) {
                        monitor.onEvent(e0.setEventType(DumpProgressEventType.STOPPED).setRowIndex(index).setRowIndex(importedCount).setMessage(NMsg.ofC("[%s] stopped at row %s", newTableName, index)));
                        break;
                    }
                }
            }
        }
        monitor.onEvent(e0.setEventType(DumpProgressEventType.PROCESSED_TABLE).setRowIndex(index).setRowIndex(importedCount).setMessage(NMsg.ofC("Imported table [%s] : %s", newTableName, importedCount)));
    }

    private void importDataRow(IoRow ccc, NSqlColumn[] columns, ImportDataContext cc) {
        IoCell[] cells = ccc.getColumns();
        Object[] vals = new Object[cells.length];
        PreparedStatementExt ps = cc.insertRowPreparedStatement;
        for (int i = 0; i < cells.length; i++) {
            IoCell c = cells[i];
            Object vv = c.getObject();
            connection.prepareStatement(ps.ps, i + 1, columns[i], vv, cc);
            vals[i] = c.getDefinition().getStoreType().name() + "@" + StringUtils.litString(vv);
        }
        LOG.log(Level.FINEST, "[" + cc.newTable.getFullName() + "] " + ps.sql + " :: " + Arrays.asList(vals));
        try {
            ps.ps.executeUpdate();
        } catch (SQLException e) {
            if (cc.failFastSQL) {
                throw new UncheckedSqlException(e);
            } else {
                LOG.log(Level.SEVERE, "[" + cc.newTable.getFullName() + "] " + ps.sql + " :: " + Arrays.asList(vals) + " : " + e);
            }
        }
    }

    private PreparedStatementExt _createInsertQuery(NSqlTableDefinition def) {
        StringBuilder sb = new StringBuilder("insert into ");
        List<NSqlColumn> columns = (List) def.getColumns();
        sb.append(this.connection.escapeIdentifier(def.getTableName()));
        sb.append("(");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(this.connection.escapeIdentifier(columns.get(i).getColumnName()));
        }
        sb.append(")");
        sb.append(" values ");
        sb.append("(");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("?");
        }
        sb.append(")");
        PreparedStatementExt a = new PreparedStatementExt();
        try {
            a.sql = sb.toString();
            a.ps = this.getConnection().getConnection().prepareStatement(a.sql);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
        return a;
    }

    @Override
    public StoreRows getStoreRows(NSqlTableId table) {
        NSqlTableDefinition tableMetaData = connection.getTableDefinition(table);
        long startTime = System.currentTimeMillis();
        LOG.log(Level.FINEST, "[" + table.getFullName() + "] reading from DB... ");
        long count = connection.getApproximateTableCount(table);
        long endTime = System.currentTimeMillis();
        LOG.log(Level.FINEST, "[" + table.getFullName() + "] read in " + (endTime - startTime) + "ms... ");
        ResultSet rs = connection.getTableResultSet(table);
        return new ResultSetStoreRows(this, rs, count, table, tableMetaData);
    }

}
