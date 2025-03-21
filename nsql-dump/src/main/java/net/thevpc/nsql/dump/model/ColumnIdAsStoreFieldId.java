package net.thevpc.nsql.dump.model;

import net.thevpc.nsql.dump.util.NSqlDumpModuleInstaller;
import net.thevpc.nsql.model.NSqlColumnId;
import net.thevpc.lib.nserializer.model.StoreFieldId;
import net.thevpc.lib.nserializer.model.StoreStructId;

import java.util.Objects;

public class ColumnIdAsStoreFieldId implements StoreFieldId {
    static {
        NSqlDumpModuleInstaller.init();
    }
    NSqlColumnId columnId;

    public ColumnIdAsStoreFieldId(NSqlColumnId columnId) {
        this.columnId = columnId;
    }

    @Override
    public String getFullName() {
        return columnId.getFullName();
    }

    @Override
    public StoreStructId getStructId() {
        return new TableIdAsStoreStructId(columnId.getTableId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnIdAsStoreFieldId that = (ColumnIdAsStoreFieldId) o;
        return Objects.equals(columnId, that.columnId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(columnId);
    }
}
