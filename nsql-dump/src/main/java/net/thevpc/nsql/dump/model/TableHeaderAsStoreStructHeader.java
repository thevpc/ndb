package net.thevpc.nsql.dump.model;

import net.thevpc.nsql.dump.util.NSqlDumpModuleInstaller;
import net.thevpc.nsql.model.NSqlTableHeader;
import net.thevpc.lib.nserializer.model.StoreStructHeader;
import net.thevpc.lib.nserializer.model.StoreStructId;

import java.util.Objects;

public class TableHeaderAsStoreStructHeader implements StoreStructHeader {
    static {
        NSqlDumpModuleInstaller.init();
    }
    private NSqlTableHeader tableHeader;

    public TableHeaderAsStoreStructHeader(NSqlTableHeader tableHeader) {
        this.tableHeader = tableHeader;
    }

    @Override
    public StoreStructId toStructId() {
        return new TableIdAsStoreStructId(tableHeader.toTableId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableHeaderAsStoreStructHeader that = (TableHeaderAsStoreStructHeader) o;
        return Objects.equals(tableHeader, that.tableHeader);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tableHeader);
    }
}
