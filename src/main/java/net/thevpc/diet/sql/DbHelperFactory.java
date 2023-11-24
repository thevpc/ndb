package net.thevpc.diet.sql;

import net.thevpc.diet.sql.impl.PostgreSqlDatabaseDriver;
import net.thevpc.diet.sql.impl.SqlServerDatabaseDriver;
import net.thevpc.diet.util.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbHelperFactory {
    public static Logger LOG = Logger.getLogger(DbHelperFactory.class.getName());

    public static DbType parseDbTypeFromDriverName(String dbType) {
        if (dbType == null) {
            return null;
        }
        if (
                dbType.toLowerCase().contains("sqlserver")
                        || dbType.contains("SQL Server")
        ) {
            return DbType.SQLSERVER;
        } else if (dbType.toLowerCase().contains("postgresql")) {
            return DbType.POSTGRESQL;
        }
        return null;
    }

    public static DbType parseDbType(String dbType) {
        if (dbType == null) {
            return null;
        }
        switch (dbType.toLowerCase().trim()) {
            case "mssql":
            case "mssqlserver":
            case "sqlserver":
            case "sql-server":
                return DbType.SQLSERVER;
            case "postgresql":
            case "psql":
            case "postgres":
            case "pg":
                return DbType.POSTGRESQL;
        }
        return null;
    }

    public static String checkValidDbType(String dbType) {
        if (dbType == null) {
            throw new IllegalArgumentException("empty dbtype");
        }
        DbType u = parseDbType(dbType);
        if (u == null) {
            throw new IllegalArgumentException("invalid dbtype " + dbType + " acceptable types are : sqlserver,postgresql");
        }
        return dbType;
    }

    public static DatabaseDriver create(CnxInfo cnxInfo) {
        cnxInfo = cnxInfo.copy();
        String url = cnxInfo.getUrl();
        String host = cnxInfo.getHost();
        String db = cnxInfo.getDbName();
        String port = cnxInfo.getPort();
        String user = cnxInfo.getUser();
        String password = cnxInfo.getPassword();
        String type = checkValidDbType(cnxInfo.getType());
        if (url != null && url.trim().length() > 0) {
            //do nothing
        } else {
            switch (parseDbType(type)) {
                case POSTGRESQL: {
                    if (StringUtils.isBlank(host)) {
                        host = "localhost";
                    }
                    if (StringUtils.isBlank(port)) {
                        port = "5432";
                    }
                    if (StringUtils.isBlank(db)) {
                        db = "postgres";
                    }
                    if (StringUtils.isBlank(user)) {
                        user = "postgres";
                    }
                    url = "jdbc:postgresql://" + host + ":" + port + "/" + db;
                    break;
                }
                case MYSQL: {
                    if (StringUtils.isBlank(host)) {
                        host = "localhost";
                    }
                    if (StringUtils.isBlank(port)) {
                        port = "3306";
                    }
                    if (StringUtils.isBlank(db)) {
                        db = "test";
                    }
                    if (StringUtils.isBlank(user)) {
                        user = "mysql";
                    }
                    url = "jdbc:mysql://" + host + ":" + port + "/" + db;
                    break;
                }
                case SQLSERVER: {
                    // jdbc:sqlserver://[serverName[\instanceName][:portNumber]][;property=value[;property=value]]
                    if (StringUtils.isBlank(host)) {
                        host = "localhost";
                    }
                    if (StringUtils.isBlank(port)) {
                        port = "1433";
                    }
                    if (StringUtils.isBlank(db)) {
                        db = "master";
                    }
                    if (StringUtils.isBlank(user)) {
                        user = "sa";
                    }
                    url = "jdbc:sqlserver://" + host + ":" + port + ";databaseName=" + db + ";encrypt=false;";
                    break;
                }
            }
        }
        return create(url, user, password);
    }

    public static DatabaseDriver create(String url, String login, String pwd) {
        try {
            safeLoadClazz("org.postgresql.Driver");
            safeLoadClazz("com.ibm.db2.jcc.DB2Driver");
            safeLoadClazz("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            safeLoadClazz("oracle.jdbc.driver.OracleDriver");
            Connection c = DriverManager.getConnection(url, login, pwd);
            return create(c);
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
    }

    private static void safeLoadClazz(String n) {
        try {
            Class.forName(n);
            LOG.log(Level.FINE, "loaded " + n);
        } catch (Exception ex) {
            LOG.log(Level.FINEST, "failed loading " + n + " : " + ex);
        }
    }

    public static DatabaseDriver create(Connection c) {
        try {
            String dr = c.getMetaData().getDriverName();
            DbType dbType = parseDbTypeFromDriverName(dr);
            if (dbType == null) {
                throw new IllegalArgumentException("unsupported driver " + dr);
            }
            switch (dbType) {
                case POSTGRESQL:
                    return new PostgreSqlDatabaseDriver(c);
                case SQLSERVER:
                    return new SqlServerDatabaseDriver(c);
            }
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
        throw new IllegalArgumentException("unsupported connection");
    }
}
