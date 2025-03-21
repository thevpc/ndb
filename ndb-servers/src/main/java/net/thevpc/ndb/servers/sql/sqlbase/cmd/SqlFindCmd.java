package net.thevpc.ndb.servers.sql.sqlbase.cmd;

import net.thevpc.ndb.servers.ExtendedQuery;
import net.thevpc.ndb.servers.NdbConfig;
import net.thevpc.ndb.servers.base.cmd.FindCmd;
import net.thevpc.ndb.servers.sql.sqlbase.SqlSupport;

import java.util.Arrays;

public class SqlFindCmd<C extends NdbConfig> extends FindCmd<C> {
    public SqlFindCmd(SqlSupport<C> support, String... names) {
        super(support, names);
    }

    @Override
    public SqlSupport<C> getSupport() {
        return (SqlSupport<C>) super.getSupport();
    }

    @Override
    protected void run(ExtendedQuery eq, C options) {
        getSupport().runSQL(Arrays.asList(eq.getRawQuery()), options);
    }

}
