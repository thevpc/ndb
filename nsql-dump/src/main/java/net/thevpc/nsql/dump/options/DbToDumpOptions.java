package net.thevpc.nsql.dump.options;

import net.thevpc.nsql.NSqlConnectionStringBuilder;
import net.thevpc.nsql.dump.io.OutputProvider;
import net.thevpc.nsql.dump.util.NameFilter;

public class DbToDumpOptions {
    public OutputProvider outp;
    public String file;
    public boolean compress;
    public long maxRows;
    public NameFilter tableNameFilter = new NameFilter();
    public boolean data = true;
    public boolean exploded = true;
    public NSqlConnectionStringBuilder cnx = new NSqlConnectionStringBuilder();
}
