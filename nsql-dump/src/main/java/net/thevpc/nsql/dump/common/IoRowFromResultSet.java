package net.thevpc.nsql.dump.common;

import net.thevpc.nsql.NSqlColumn;
import net.thevpc.lib.nserializer.api.IoCell;
import net.thevpc.lib.nserializer.api.IoRow;
import net.thevpc.lib.nserializer.impl.RepeatableReadIoCellArr;
import net.thevpc.lib.nserializer.model.StoreStructDefinition;

import java.sql.ResultSet;
import java.util.List;

class IoRowFromResultSet implements IoRow {
    private final DefaultNSqlDump abstractDatabaseDriver;
    private final List<NSqlColumn> columns;
    private final ResultSet rs;
    private final StoreStructDefinition tableMetaData;
    //    int index;
    private IoCell[] cells;

    public IoRowFromResultSet(DefaultNSqlDump abstractDatabaseDriver, List<NSqlColumn> columns, ResultSet rs, StoreStructDefinition tableMetaData) {
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
    public StoreStructDefinition getDefinition() {
        return tableMetaData;
    }
}
