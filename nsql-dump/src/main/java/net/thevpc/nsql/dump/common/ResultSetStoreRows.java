package net.thevpc.nsql.dump.common;

import net.thevpc.nsql.dump.model.TableDefinitionAsStoreStructDefinition;
import net.thevpc.nsql.dump.model.TableRowsDefinitionAsStoreRowsDefinition;
import net.thevpc.nsql.NSqlColumn;
import net.thevpc.nsql.UncheckedSqlException;
import net.thevpc.nsql.model.NSqlTableDefinition;
import net.thevpc.nsql.model.NSqlTableId;
import net.thevpc.lib.nserializer.api.IoRow;
import net.thevpc.lib.nserializer.impl.AbstractStoreRows;
import net.thevpc.lib.nserializer.model.StoreRowsDefinition;
import net.thevpc.lib.nserializer.model.StoreStructDefinition;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

public class ResultSetStoreRows extends AbstractStoreRows {
    private final DefaultNSqlDump abstractDatabaseDriver;
    private final ResultSet rs;
    private final NSqlTableDefinition tableMetaData;
    private final TableDefinitionAsStoreStructDefinition structDefinition;
    StoreRowsDefinition t;
    boolean stopped;

    public ResultSetStoreRows(DefaultNSqlDump abstractDatabaseDriver, ResultSet rs, NSqlTableId table, NSqlTableDefinition tableMetaData) {
        this.abstractDatabaseDriver = abstractDatabaseDriver;
        this.rs = rs;
        this.tableMetaData = tableMetaData;
        structDefinition = new TableDefinitionAsStoreStructDefinition(tableMetaData);
        ResultSetMetaData md;
        try {
            md = rs.getMetaData();
        } catch (SQLException ex) {
            throw new UncheckedSqlException(ex);
        }
        t = abstractDatabaseDriver.createRowsDefinition(md);
        TableRowsDefinitionAsStoreRowsDefinition a=(TableRowsDefinitionAsStoreRowsDefinition) t;
        a.getTableRowsDefinition().setCatalogName(table.getCatalogName());
        a.getTableRowsDefinition().setSchemaName(table.getSchemaName());
        a.getTableRowsDefinition().setTableName(table.getTableName());
    }

    @Override
    public StoreStructDefinition getDefinition() {
        return structDefinition;
    }

    @Override
    public IoRow nextRow() {
        if (stopped) {
            return null;
        }
        try {
            if (rs.next()) {
                List<NSqlColumn> columns = tableMetaData.getColumns();
                return new IoRowFromResultSet(abstractDatabaseDriver, columns, rs, structDefinition);
            } else {
                stopped = true;
                return null;
            }
        } catch (SQLException ex) {
            throw new UncheckedSqlException(ex);
        }
    }

    @Override
    public void close() {
        super.close();
        try {
            rs.close();
        } catch (SQLException e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }
}
