package net.thevpc.nsql.dump.options;

import net.thevpc.nsql.dump.io.In;
import net.thevpc.nsql.dump.util.NameFilter;

public class DumpToDbOptions {
    private TableRestoreOptions schemaMode;
    private In in;
    private NameFilter tableNameFilter = new NameFilter();
    private boolean data = true;

    public TableRestoreOptions getSchemaMode() {
        return schemaMode;
    }

    public DumpToDbOptions setSchemaMode(TableRestoreOptions schemaMode) {
        this.schemaMode = schemaMode;
        return this;
    }

    public In getIn() {
        return in;
    }

    public DumpToDbOptions setIn(In in) {
        this.in = in;
        return this;
    }

    public NameFilter getTableNameFilter() {
        return tableNameFilter;
    }

    public DumpToDbOptions setTableNameFilter(NameFilter tableNameFilter) {
        this.tableNameFilter = tableNameFilter;
        return this;
    }

    public boolean isData() {
        return data;
    }

    public DumpToDbOptions setData(boolean data) {
        this.data = data;
        return this;
    }
}
