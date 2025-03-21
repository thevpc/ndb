package net.thevpc.nsql.dump.model;

import net.thevpc.nsql.dump.util.NSqlDumpModuleInstaller;
import net.thevpc.nsql.model.NSqlTableId;
import net.thevpc.lib.nserializer.model.StoreStructId;

import java.util.Objects;

public class TableIdAsStoreStructId implements StoreStructId {
    static {
        NSqlDumpModuleInstaller.init();
    }
    private final NSqlTableId tableId;

    public TableIdAsStoreStructId(NSqlTableId tableId) {
        this.tableId = tableId;
    }

    public NSqlTableId getTableId() {
        return tableId;
    }

    @Override
    public String getFullName() {
        return tableId.getFullName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableIdAsStoreStructId that = (TableIdAsStoreStructId) o;
        return Objects.equals(tableId, that.tableId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tableId);
    }
}
