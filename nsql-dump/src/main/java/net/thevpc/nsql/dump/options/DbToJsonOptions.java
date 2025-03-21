package net.thevpc.nsql.dump.options;

import net.thevpc.nsql.NSqlConnectionStringBuilder;
import net.thevpc.nsql.dump.io.OutputProvider;
import net.thevpc.nsql.dump.util.NameFilter;

public class DbToJsonOptions {
    public OutputProvider pout;
    public boolean exploded = false;
    public NSqlConnectionStringBuilder cnx = new NSqlConnectionStringBuilder();
    public NameFilter tableNameFilter = new NameFilter();
    public boolean data = true;
    public long maxRows = -1;

}
