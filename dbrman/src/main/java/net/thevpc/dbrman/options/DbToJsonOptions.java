package net.thevpc.dbrman.options;

import net.thevpc.dbrman.model.CnxInfo;
import net.thevpc.dbrman.io.OutputProvider;
import net.thevpc.vio2.util.NameFilter;

public class DbToJsonOptions {
    public OutputProvider pout;
    public boolean exploded = false;
    public CnxInfo cnx = new CnxInfo();
    public NameFilter tableNameFilter = new NameFilter();
    public boolean data = true;
    public long maxRows = -1;

}
