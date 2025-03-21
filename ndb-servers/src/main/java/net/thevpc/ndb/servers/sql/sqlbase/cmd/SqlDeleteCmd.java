package net.thevpc.ndb.servers.sql.sqlbase.cmd;

import net.thevpc.ndb.servers.ExtendedQuery;
import net.thevpc.ndb.servers.NdbConfig;
import net.thevpc.ndb.servers.base.cmd.DeleteCmd;
import net.thevpc.ndb.servers.sql.sqlbase.SqlSupport;

import java.util.Arrays;

public class SqlDeleteCmd<C extends NdbConfig> extends DeleteCmd<C> {
    public SqlDeleteCmd(SqlSupport<C> support, String... names) {
        super(support, names);
    }

    @Override
    public SqlSupport<C> getSupport() {
        return (SqlSupport<C>) super.getSupport();
    }

    @Override
    protected void runDelete(ExtendedQuery eq, C options) {
        StringBuilder sql = new StringBuilder();
        sql.append("delete  from ").append(options.getName());
        String whereSQL = getSupport().createWhere(eq.getWhere());
        if (!whereSQL.isEmpty()) {
            sql.append(" where ");
            sql.append(whereSQL);
        }
        getSupport().runSQL(Arrays.asList(sql.toString()), options);
    }

}
