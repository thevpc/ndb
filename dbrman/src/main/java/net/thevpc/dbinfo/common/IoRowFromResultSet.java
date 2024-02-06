package net.thevpc.dbinfo.common;

import net.thevpc.dbinfo.model.ColumnDefinition;
import net.thevpc.dbinfo.model.TableDefinition;
import net.thevpc.vio2.api.IoCell;
import net.thevpc.vio2.api.IoRow;

import java.sql.ResultSet;
import java.util.List;

class IoRowFromResultSet implements IoRow {
    private final AbstractDatabaseDriver abstractDatabaseDriver;
    private final List<ColumnDefinition> columns;
    private final ResultSet rs;
    private final TableDefinition tableMetaData;
    int index;

    public IoRowFromResultSet(AbstractDatabaseDriver abstractDatabaseDriver, List<ColumnDefinition> columns, ResultSet rs, TableDefinition tableMetaData) {
        this.abstractDatabaseDriver = abstractDatabaseDriver;
        this.columns = columns;
        this.rs = rs;
        this.tableMetaData = tableMetaData;
        index = 0;
    }

    @Override
    public IoCell nextColumn() {
        if (index < columns.size()) {
            return abstractDatabaseDriver.createCell(rs, columns.get(index++));
        } else {
            return null;
        }
    }

    @Override
    public TableDefinition getDefinition() {
        return tableMetaData;
    }
}
