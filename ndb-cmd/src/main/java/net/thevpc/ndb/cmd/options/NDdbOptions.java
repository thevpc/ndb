package net.thevpc.ndb.cmd.options;

import net.thevpc.nsql.NSqlConnectionStringBuilder;
import net.thevpc.nsql.dump.util.NameFilter;
import net.thevpc.nsql.dump.options.TableRestoreOptions;

public class NDdbOptions {
    public NSqlConnectionStringBuilder cnx = new NSqlConnectionStringBuilder();
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
