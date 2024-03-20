package net.thevpc.dbrman.common;

import net.thevpc.dbrman.model.ColumnDefinition;
import net.thevpc.dbrman.model.TableDefinition;
import net.thevpc.vio2.api.IoCell;
import net.thevpc.vio2.api.IoRow;
import net.thevpc.vio2.impl.RepeatableReadIoCellArr;

import java.sql.ResultSet;
import java.util.List;

class IoRowFromResultSet implements IoRow {
    private final AbstractDatabaseDriver abstractDatabaseDriver;
    private final List<ColumnDefinition> columns;
    private final ResultSet rs;
    private final TableDefinition tableMetaData;
    //    int index;
    private IoCell[] cells;

    public IoRowFromResultSet(AbstractDatabaseDriver abstractDatabaseDriver, List<ColumnDefinition> columns, ResultSet rs, TableDefinition tableMetaData) {
        this.abstractDatabaseDriver = abstractDatabaseDriver;
        this.columns = columns;
        this.rs = rs;
        this.tableMetaData = tableMetaData;
//        this.index = 0;
    }

    @Override
    public IoRow repeatable() {
        return new RepeatableReadIoCellArr(this);
    }

    public IoCell[] getColumns() {
        if (cells == null) {
            cells = new IoCell[columns.size()];
            for (int i = 0; i < cells.length; i++) {
                cells[i] = abstractDatabaseDriver.createCell(rs, columns.get(i)).repeatable();
            }
        }
        return cells;
    }

//    @Override
//    public IoCell nextColumn() {
//        if (index < columns.size()) {
//            return abstractDatabaseDriver.createCell(rs, columns.get(index++));
//        } else {
//            return null;
//        }
//    }

    @Override
    public TableDefinition getDefinition() {
        return tableMetaData;
    }
}
