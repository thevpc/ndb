package net.thevpc.dbinfo.options;

import net.thevpc.dbinfo.model.CnxInfo;
import net.thevpc.dbinfo.io.OutputProvider;
import net.thevpc.vio2.util.NameFilter;

public class DbToJsonOptions {
    public OutputProvider pout;
    public boolean exploded = false;
    public CnxInfo cnx = new CnxInfo();
    public NameFilter tableNameFilter = new NameFilter();
    public boolean data = true;
    public long maxRows = -1;

}
