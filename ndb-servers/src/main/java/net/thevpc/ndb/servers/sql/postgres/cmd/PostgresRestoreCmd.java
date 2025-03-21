package net.thevpc.ndb.servers.sql.postgres.cmd;

import net.thevpc.ndb.servers.sql.postgres.NPostgresConfig;
import net.thevpc.ndb.servers.sql.postgres.NPostgresSupport;
import net.thevpc.ndb.servers.sql.sqlbase.cmd.SqlRestoreCmd;

public class PostgresRestoreCmd extends SqlRestoreCmd<NPostgresConfig> {
    public PostgresRestoreCmd(NPostgresSupport support) {
        super(support);
    }

    @Override
    public NPostgresSupport getSupport() {
        return (NPostgresSupport) super.getSupport();
    }


}
