package net.thevpc.diet.cmd.options;

import net.thevpc.diet.sql.CnxInfo;
import net.thevpc.diet.sql.NameFilter;
import net.thevpc.diet.cmd.options.TableRestoreOptions;

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
        EXPORT,
        IMPORT,
        JSON,
    }
}
