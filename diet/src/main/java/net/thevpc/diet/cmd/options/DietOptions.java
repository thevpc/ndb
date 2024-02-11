package net.thevpc.diet.cmd.options;

import net.thevpc.dbrman.model.CnxInfo;
import net.thevpc.dbrman.options.TableRestoreOptions;
import net.thevpc.vio2.util.NameFilter;

public class DietOptions {
    public CnxInfo cnx = new CnxInfo();
    public NameFilter tableNameFilter = new NameFilter();
    public boolean data=true;
    public boolean compress=true;
    public boolean exploded=false;
    public long maxRows=-1;
    public String file;
    public Cmd action;
    public TableRestoreOptions schemaMode = new TableRestoreOptions();
    static public enum Cmd{
        DB_TO_DUMP,
        DB_TO_JSON,
        DUMP_TO_DB,
        DUMP_TO_JSON,
    }
}
