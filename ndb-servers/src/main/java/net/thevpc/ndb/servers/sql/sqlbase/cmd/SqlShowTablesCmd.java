package net.thevpc.ndb.servers.sql.sqlbase.cmd;

import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.ndb.servers.ExtendedQuery;
import net.thevpc.ndb.servers.NdbConfig;
import net.thevpc.ndb.servers.base.cmd.ShowTablesCmd;
import net.thevpc.ndb.servers.sql.sqlbase.SqlSupport;

import java.util.Arrays;

public class SqlShowTablesCmd<C extends NdbConfig> extends ShowTablesCmd<C> {
    public SqlShowTablesCmd(SqlSupport<C> support, String... names) {
        super(support, names);
    }
    @Override
    public SqlSupport<C> getSupport() {
        return (SqlSupport<C>) super.getSupport();
    }

    @Override
    protected void runShowTables(ExtendedQuery eq, C options) {
        ((SqlSupport<C>)support).runSQL(Arrays.asList(createShowTablesSQL(options)), options);
    }

    protected String createShowTablesSQL(C options) {
        throw new NIllegalArgumentException(NMsg.ofPlain("unsupported createShowTablesSQL"));
    }
}
