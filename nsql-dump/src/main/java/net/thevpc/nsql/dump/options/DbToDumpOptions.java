package net.thevpc.nsql.dump.options;

import net.thevpc.nsql.NSqlConnectionString;
import net.thevpc.nsql.NSqlConnectionStringBuilder;
import net.thevpc.nsql.dump.DumpProgressMonitor;
import net.thevpc.nsql.dump.io.OutputProvider;
import net.thevpc.nsql.dump.util.NameFilter;

public class DbToDumpOptions {
    public OutputProvider out;
    public boolean compress;
    public long maxRows=-1;
    public NameFilter tableNameFilter = new NameFilter();
    public boolean data = true;
    public boolean exploded = true;
    public DumpProgressMonitor monitor;


    public DumpProgressMonitor getMonitor() {
        return monitor;
    }

    public DbToDumpOptions setMonitor(DumpProgressMonitor monitor) {
        this.monitor = monitor;
        return this;
    }

    public OutputProvider getOut() {
        return out;
    }

    public DbToDumpOptions setOut(OutputProvider out) {
        this.out = out;
        return this;
    }

    public boolean isCompress() {
        return compress;
    }

    public DbToDumpOptions setCompress(boolean compress) {
        this.compress = compress;
        return this;
    }

    public long getMaxRows() {
        return maxRows;
    }

    public DbToDumpOptions setMaxRows(long maxRows) {
        this.maxRows = maxRows;
        return this;
    }

    public NameFilter getTableNameFilter() {
        return tableNameFilter;
    }

    public DbToDumpOptions setTableNameFilter(NameFilter tableNameFilter) {
        this.tableNameFilter = tableNameFilter;
        return this;
    }

    public boolean isData() {
        return data;
    }

    public DbToDumpOptions setData(boolean data) {
        this.data = data;
        return this;
    }

    public boolean isExploded() {
        return exploded;
    }

    public DbToDumpOptions setExploded(boolean exploded) {
        this.exploded = exploded;
        return this;
    }

}
