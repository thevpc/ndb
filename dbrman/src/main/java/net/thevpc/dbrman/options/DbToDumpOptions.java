package net.thevpc.dbrman.options;

import net.thevpc.dbrman.model.CnxInfo;
import net.thevpc.dbrman.io.OutputProvider;
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
