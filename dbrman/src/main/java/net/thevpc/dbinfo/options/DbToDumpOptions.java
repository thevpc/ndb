package net.thevpc.dbinfo.options;

import net.thevpc.dbinfo.model.CnxInfo;
import net.thevpc.dbinfo.io.OutputProvider;
import net.thevpc.vio2.util.NameFilter;

public class DbToDumpOptions {
    public OutputProvider outp;
    public String file;
    public boolean compress;
    public long maxRows;
    public NameFilter tableNameFilter = new NameFilter();
    public boolean data = true;
    public boolean exploded = true;
    public CnxInfo cnx = new CnxInfo();
}
