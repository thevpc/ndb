package net.thevpc.ndb.servers.sql.postgres.cmd;

import net.thevpc.ndb.servers.sql.postgres.NPostgresSupport;
import net.thevpc.ndb.servers.sql.postgres.NPostgresConfig;
import net.thevpc.ndb.servers.sql.sqlbase.cmd.SqlShowDatabasesCmd;

public class PostgresShowDatabasesCmd extends SqlShowDatabasesCmd<NPostgresConfig> {
    public PostgresShowDatabasesCmd(NPostgresSupport support) {
        super(support,"show-db","show-dbs");
    }

    @Override
    protected String createShowDatabasesSQL(NPostgresConfig options) {
        return "SELECT distinct(schemaname) FROM pg_catalog.pg_tables WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema'";
    }
}
