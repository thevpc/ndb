package net.thevpc.ndb.servers.sql.sqlbase.cmd;

import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.elem.NElementParser;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.ndb.servers.ExtendedQuery;
import net.thevpc.ndb.servers.NdbConfig;
import net.thevpc.ndb.servers.base.cmd.RenameTableCmd;
import net.thevpc.ndb.servers.sql.sqlbase.SqlSupport;

import java.util.Arrays;
import java.util.Map;

public class SqlRenameTableCmd<C extends NdbConfig> extends RenameTableCmd<C> {
    public SqlRenameTableCmd(SqlSupport<C> support, String... names) {
        super(support, names);
    }
    @Override
    public SqlSupport<C> getSupport() {
        return (SqlSupport<C>) super.getSupport();
    }

    @Override
    protected void runRenameTable(ExtendedQuery eq, C options) {
        StringBuilder sql = new StringBuilder();

        StringBuilder setKeys = new StringBuilder();
        StringBuilder setVals = new StringBuilder();
        for (String s : eq.getSet()) {
            s = s.trim();
            if (s.length() > 0) {
                if (s.startsWith("{")) {
                    Map<String, Object> row = NElementParser.ofJson().parse(s, Map.class);
                    for (Map.Entry<String, Object> e : row.entrySet()) {
                        if (setKeys.length() > 0) {
                            setKeys.append(",");
                            setVals.append(",");
                        }
                        setKeys.append(e.getKey());
                        setVals.append(((SqlSupport<C>) support).formatLiteral(e.getValue()));
                    }
                } else {
                    if (setKeys.length() > 0) {
                        setKeys.append(",");
                    }
                    int i = s.indexOf('=');
                    if (i < 0) {
                        throw new NIllegalArgumentException(NMsg.ofC("invalid %s", s));
                    }
                    setKeys.append(s, 0, i);
                    setVals.append(s, i + 1, s.length());
                }
            }
        }
        if (setKeys.length() == 0) {
            throw new NIllegalArgumentException(NMsg.ofPlain("missing set"));
        }


        sql.append("insert into ").append(eq.getTable()).append(" (");
        sql.append(setKeys);
        sql.append(") values (");
        sql.append(setVals);
        sql.append(")");
        ((SqlSupport<C>) support).runSQL(Arrays.asList(sql.toString()), options);
    }
}
