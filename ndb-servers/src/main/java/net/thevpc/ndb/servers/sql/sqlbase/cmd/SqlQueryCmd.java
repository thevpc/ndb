package net.thevpc.ndb.servers.sql.sqlbase.cmd;

import net.thevpc.ndb.servers.ExtendedQuery;
import net.thevpc.ndb.servers.NdbConfig;
import net.thevpc.ndb.servers.base.cmd.QueryCmd;
import net.thevpc.ndb.servers.sql.sqlbase.SqlSupport;

import java.util.Arrays;

public class SqlQueryCmd<C extends NdbConfig> extends QueryCmd<C> {
    public SqlQueryCmd(SqlSupport<C> support, String... names) {
        super(support, names);
        this.names.addAll(Arrays.asList("run-sql", "sql"));
    }
    @Override
    public SqlSupport<C> getSupport() {
        return (SqlSupport<C>) super.getSupport();
    }

    @Override
    protected void runRawQuery(ExtendedQuery eq, C options) {
        getSupport().runSQL(Arrays.asList(eq.getRawQuery()), options);
    }
}
