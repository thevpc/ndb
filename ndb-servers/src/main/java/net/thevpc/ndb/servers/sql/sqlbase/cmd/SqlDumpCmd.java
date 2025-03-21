package net.thevpc.ndb.servers.sql.sqlbase.cmd;

import net.thevpc.ndb.servers.NdbConfig;
import net.thevpc.ndb.servers.base.cmd.DumpCmd;
import net.thevpc.ndb.servers.sql.sqlbase.SqlSupport;

public class SqlDumpCmd<C extends NdbConfig> extends DumpCmd<C> {
    public SqlDumpCmd(SqlSupport<C> support, String... names) {
        super(support, names);
    }
    @Override
    public SqlSupport<C> getSupport() {
        return (SqlSupport<C>) super.getSupport();
    }

}
