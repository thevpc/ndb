package net.thevpc.diet.sql;

import net.thevpc.diet.cmd.options.TableRestoreOptions;
import net.thevpc.diet.io.IoRow;
import net.thevpc.diet.io.RepeatableReadIoCell;
import net.thevpc.diet.io.RepeatableReadIoCellArr;
import net.thevpc.diet.io.StoreRows;
import net.thevpc.diet.util.StringUtils;
import net.thevpc.diet.model.StoreColumnDefinition;
import net.thevpc.diet.model.StoreTableDefinition;
import net.thevpc.diet.model.TableId;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultTableImporter implements TableImporter {
    public static Logger LOG = Logger.getLogger(DefaultTableImporter.class.getName());
    protected StoreRows rows;
    protected DatabaseDriver dbHelper;
    private PreparedStatement insertRowPreparedStatement;
    private String insertRowPreparedStatementSQL;
    private TableRestoreOptions schemaMode;
    private TableId newTable;

    public DefaultTableImporter(DatabaseDriver dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void importData(StoreRows rows, TableRestoreOptions schemaMode) {
        clearState();
        this.schemaMode = schemaMode == null ? new TableRestoreOptions() : schemaMode;
        try {
            newTable = new TableId(
                    dbHelper.getConnection().getCatalog(),
                    dbHelper.getConnection().getSchema(),
                    rows.getDefinition().getTableName()
            );
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
        this.rows = rows;
        try {
            importData();
        } finally {
            clearState();
        }
    }

    public void clearState() {
        this.rows = null;
        this.newTable = null;
    }

    protected void importData() {
        if (schemaMode.isClearTable()) {
            dbHelper.clearTable(newTable);
        }
        IoRow r = null;
        _createInsertQuery();
        while ((r = rows.nextRow()) != null) {
            importRow(r);
        }
    }

    private void _createInsertQuery() {
        StringBuilder sb = new StringBuilder("insert into ");
        StoreColumnDefinition[] columns = rows.getDefinition().getColumns();
        sb.append(dbHelper.escapeIdentifier(newTable.getTableName()));
        sb.append("(");
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(dbHelper.escapeIdentifier(columns[i].getColumnName()));
        }
        sb.append(")");
        sb.append(" values ");
        sb.append("(");
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("?");
        }
        sb.append(")");
        try {
            insertRowPreparedStatementSQL = sb.toString();
            insertRowPreparedStatement = dbHelper.getConnection().prepareStatement(insertRowPreparedStatementSQL);
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    public void importRow(IoRow row) {
        try (RepeatableReadIoCellArr cc = new RepeatableReadIoCellArr(row)) {
            executeInsert(cc);
        }
    }

    private void executeInsert(RepeatableReadIoCellArr cc) {
        RepeatableReadIoCell[] cells = cc.getCells();
        Object[] vals = new Object[cells.length];
        for (int i = 0; i < cells.length; i++) {
            RepeatableReadIoCell c = cells[i];
            Object vv = c.getObject();
            dbHelper.prepareStatement(insertRowPreparedStatement, i + 1, c.getMetaData().getStoreType(), vv);
            vals[i] = c.getMetaData().getStoreType().name() + "@" + StringUtils.litString(vv);
        }
        LOG.log(Level.FINEST, "[" + newTable.toStringId() + "] " + insertRowPreparedStatementSQL + " :: " + Arrays.asList(vals));
        try {
            insertRowPreparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

//    private void executeInsertOrUpdate(RepeatableReadIoCellArr cc) {
//        RepeatableReadIoCell[] cells = cc.getCells();
//        Object[] vals = new Object[cells.length];
//        Object[] svals = new Object[cells.length];
//        for (int i = 0; i < cells.length; i++) {
//            RepeatableReadIoCell c = cells[i];
//            Object vv = c.getObject();
//            vals[i] = vv;
//            svals[i] = String.valueOf(vv) + "@" + c.getMetaData().getStoreType().name();
//        }
//
//        LOG.log(Level.FINEST, "[" + rows.getDefinition().getTableName() + "] " + insertRowPreparedStatementSQL + " :: " + Arrays.asList(vals));
//        try {
//            insertRowPreparedStatement.executeUpdate();
//        } catch (SQLException e) {
//            throw new UncheckedSQLException(e);
//        }
//    }

    public void updateSchema(StoreTableDefinition definition, TableRestoreOptions schemaMode) {
        boolean logDropped = false;
        boolean logCreated = false;
        Set<String> createdColumns = new HashSet<>();
        Set<String> updatedColumns = new HashSet<>();
        Set<String> droppedColumns = new HashSet<>();

        StoreTableDefinition newMd = definition;
        TableId newTable = null;
        try {
            newTable = new TableId(
                    dbHelper.getConnection().getCatalog(),
                    dbHelper.getConnection().getSchema(),
                    newMd.getTableName()
            );
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
        StoreTableDefinition oldMd = dbHelper.getTableMetaData(newTable);
        if (oldMd != null) {
            if (schemaMode.isDropTable()) {
                dbHelper.dropTable(newTable);
                logDropped = true;
                oldMd = null;
            }
        }
        if (oldMd == null) {
            dbHelper.createTable(newMd);
            logCreated = true;
        } else {
            StoreColumnDefinition[] newCols = newMd.getColumns();
            StoreColumnDefinition[] oldCols = oldMd.getColumns();
            for (StoreColumnDefinition newCol : newCols) {
                StoreColumnDefinition oldCol = Arrays.stream(oldCols).filter(x -> x.getColumnName().equals(newCol.getColumnName())).findAny().orElse(null);
                if (oldCol == null) {
                    if (schemaMode.isCreateColumn()) {
                        dbHelper.createColumn(newCol);
                        createdColumns.add(newCol.getColumnName());
                    } else {
                        throw new IllegalArgumentException("missing column " + newCol);
                    }
                } else {
                    //check types???
                }
            }
            for (StoreColumnDefinition oldCol : oldCols) {
                StoreColumnDefinition newCol = Arrays.stream(newCols).filter(x -> x.getColumnName().equals(oldCol.getColumnName())).findAny().orElse(null);
                if (newCol == null) {
                    if (schemaMode.isDropTable()) {
                        dbHelper.dropColumn(oldCol);
                        droppedColumns.add(newCol.getColumnName());
                    } else {
                        throw new IllegalArgumentException("cannot drop column " + oldCol);
                    }
                } else {
                    //already checked type???
                }
            }
        }

        StringBuilder ll=new StringBuilder();
        if(logDropped){
            ll.append("re-created");
        }else if(logCreated){
            ll.append("created");
        }
        if(!createdColumns.isEmpty()){
            if(ll.length()>0){
                ll.append(", ");
            }
            ll.append("added columns : ").append(createdColumns);
        }
        if(!droppedColumns.isEmpty()){
            if(ll.length()>0){
                ll.append(", ");
            }
            ll.append("dropped columns : "+droppedColumns);
        }
        if(!updatedColumns.isEmpty()){
            if(ll.length()>0){
                ll.append(", ");
            }
            ll.append("altered columns : "+updatedColumns);
        }
        if(ll.length()>0) {
            LOG.log(Level.FINE, "[" + newTable.toStringId() + "] " + ll);
        }else{
            LOG.log(Level.FINE, "[" + newTable.toStringId() + "] clean and uptodate.");
        }
//        deactivateConstraints();
    }

    @Override
    public void enableConstraints(StoreTableDefinition d, TableRestoreOptions schemaMode) {

    }

    @Override
    public void disableConstraints(StoreTableDefinition d, TableRestoreOptions schemaMode) {

    }

    public void deactivateConstraints() {

    }


}
