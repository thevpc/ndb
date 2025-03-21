package net.thevpc.nsql.dump.util;

import net.thevpc.nsql.*;
import net.thevpc.nsql.dump.api.NSqlDump;
import net.thevpc.nsql.dump.common.DefaultNSqlDump;
import net.thevpc.nsql.dump.impl.PostgreSqlDatabaseDriver;
import net.thevpc.nsql.dump.impl.SqlServerDatabaseDriver;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseDriverFactories {
    static {
        NSqlDumpModuleInstaller.init();
    }

    public static Logger LOG = Logger.getLogger(DatabaseDriverFactories.class.getName());


    private static void safeLoadClazz(String n) {
        try {
            Class.forName(n);
            LOG.log(Level.FINE, "loaded " + n);
        } catch (Exception ex) {
            LOG.log(Level.FINEST, "failed loading " + n + " : " + ex);
        }
    }

    public static NSqlDump createSqlDump(NSqlConnectionString cnx) {
        return createSqlDump(NSqlConnectionFactory.of(cnx).create());
    }

    public static NSqlDump createSqlDump(NSqlConnectionStringBuilder cnx) {
        return createSqlDump(NSqlConnectionFactory.of(cnx).create());
    }

    public static NSqlDump createSqlDump(NSqlConnection c) {
//        try {
//            String dr = c.getMetaData().getDriverName();
//            DbType dbType = parseDbTypeFromDriverName(dr);
//            if (dbType == null) {
//                throw new IllegalArgumentException("unsupported driver " + dr);
//            }
        switch (c.getDialect()) {
            case POSTGRESQL:
                return new PostgreSqlDatabaseDriver(c);
            case MSSQLSERVER:
                return new SqlServerDatabaseDriver(c);
//            case JTDS_SQLSERVER:
//                return new JtdsSqlServerDatabaseDriver(c);
        }
//        } catch (SQLException ex) {
//            throw new UncheckedSqlException(ex);
//        }
        return new DefaultNSqlDump(c);
    }
}
