package net.thevpc.nsql.dump.common;

import net.thevpc.nsql.dump.model.ColumnIdAsStoreFieldId;
import net.thevpc.nsql.NSqlColumn;
import net.thevpc.nsql.dump.util.SqlColumnTypeToStoreUtils;
import net.thevpc.lib.nserializer.model.StoreDataType;
import net.thevpc.lib.nserializer.model.StoreFieldDefinition;
import net.thevpc.lib.nserializer.model.StoreFieldId;

public class SqlColumnAsStoreFieldDefinition implements StoreFieldDefinition {
    private final StoreDataType fileColType;
    private final NSqlColumn column;

    public SqlColumnAsStoreFieldDefinition(StoreDataType fileColType, NSqlColumn column) {
        this.fileColType = fileColType;
        this.column = column;
    }
    public SqlColumnAsStoreFieldDefinition(NSqlColumn column) {
        this.fileColType = SqlColumnTypeToStoreUtils.toStoreDataType(column);
        this.column = column;
    }

    public NSqlColumn getColumn() {
        return column;
    }

    @Override
    public StoreDataType getStoreType() {
        return fileColType;
    }

    @Override
    public String getFullName() {
        return new ColumnIdAsStoreFieldId(column.getColumnId()).getFullName();
    }

    @Override
    public String getFieldName() {
        return column.getFieldName();
    }

    @Override
    public StoreFieldId toFieldId() {
        return new ColumnIdAsStoreFieldId(column.getColumnId());
    }
}
