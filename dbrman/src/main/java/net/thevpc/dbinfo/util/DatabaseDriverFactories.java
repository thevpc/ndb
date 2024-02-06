package net.thevpc.dbinfo.util;

import net.thevpc.dbinfo.api.DatabaseDriver;
import net.thevpc.dbinfo.impl.JtdsSqlServerDatabaseDriver;
import net.thevpc.dbinfo.impl.PostgreSqlDatabaseDriver;
import net.thevpc.dbinfo.impl.SqlServerDatabaseDriver;
import net.thevpc.dbinfo.model.CnxInfo;
import net.thevpc.dbinfo.model.DbType;
import net.thevpc.vio2.util.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseDriverFactories {
    static {
        DbInfoModuleInstaller.init();
    }

    public static Logger LOG = Logger.getLogger(DatabaseDriverFactories.class.getName());

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
            case "jtds-sqlserver":
                return DbType.JTDS_SQLSERVER;
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

    public static int parsePort(String p,int defaultValue) {
        if(p!=null){
            p=p.trim();
            if(p.length()>0){
                try {
                    int r = Integer.parseInt(p);
                    if(r>0){
                        return r;
                    }
                }catch (Exception ex){
                    //
                }
            }
        }
        return defaultValue;
    }

    public static DatabaseDriver createDatabaseDriver(CnxInfo cnxInfo) {
        cnxInfo = cnxInfo.copy();
        String url = cnxInfo.getUrl();
        String host = cnxInfo.getHost();
        String db = cnxInfo.getDbName();
        String port = cnxInfo.getPort();
        String user = cnxInfo.getUser();
        String password = cnxInfo.getPassword();
        String type = checkValidDbType(cnxInfo.getType());
        DbType dbType = null;
        if (url != null && url.trim().length() > 0) {
            //do nothing
        } else {
            dbType = parseDbType(type);
            switch (dbType) {
                case POSTGRESQL: {
                    if (StringUtils.isBlank(host)) {
                        host = "localhost";
                    }
                    port=String.valueOf(parsePort(port,5432));
                    if (StringUtils.isBlank(db)) {
                        db = "postgres";
                    }
                    if (StringUtils.isBlank(user)) {
                        user = "postgres";
                    }
                    url = "jdbc:postgresql://" + host + ":" + port + "/" + db;
                    safeLoadClazz("org.postgresql.Driver");
                    break;
                }
                case MYSQL: {
                    if (StringUtils.isBlank(host)) {
                        host = "localhost";
                    }
                    port=String.valueOf(parsePort(port,3306));
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
                    port=String.valueOf(parsePort(port,1433));
                    if (StringUtils.isBlank(db)) {
                        db = "master";
                    }
                    if (StringUtils.isBlank(user)) {
                        user = "sa";
                    }
                    url = "jdbc:sqlserver://" + host + ":" + port + ";databaseName=" + db + ";encrypt=false";
                    if (cnxInfo.isIntegrationSecurity()) {
                        url += ";integratedSecurity=true";
                    }
                    safeLoadClazz("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                    break;
                }
                case JTDS_SQLSERVER: {
                    // jdbc:sqlserver://[serverName[\instanceName][:portNumber]][;property=value[;property=value]]
                    if (StringUtils.isBlank(host)) {
                        host = "localhost";
                    }
                    port=String.valueOf(parsePort(port,1433));
                    if (StringUtils.isBlank(db)) {
                        db = "master";
                    }
                    if (StringUtils.isBlank(user)) {
                        user = "sa";
                    }
                    url = "jdbc:jtds:sqlserver://" + host + ":" + port + "/" + db;
                    if (cnxInfo.isIntegrationSecurity()) {
                        url += ";useNTLMv2=true";
                    }
                    safeLoadClazz("net.sourceforge.jtds.jdbc.Driver");
                    break;
                }
            }
        }
        return createDatabaseDriver(url, user, password, dbType);
    }

    public static DatabaseDriver createDatabaseDriver(String url, String login, String pwd, DbType dbType) {
        try {
            System.out.println("CNX:" + url + "  ::: " + login + " / " + pwd + " / " + dbType);
            //safeLoadClazz("com.ibm.db2.jcc.DB2Driver");
//            safeLoadClazz("oracle.jdbc.driver.OracleDriver");
            Connection c = DriverManager.getConnection(url, login, pwd);
            return createDatabaseDriver(c, dbType);
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

    public static DatabaseDriver createDatabaseDriver(Connection c, DbType dbType) {
//        try {
//            String dr = c.getMetaData().getDriverName();
//            DbType dbType = parseDbTypeFromDriverName(dr);
//            if (dbType == null) {
//                throw new IllegalArgumentException("unsupported driver " + dr);
//            }
        switch (dbType) {
            case POSTGRESQL:
                return new PostgreSqlDatabaseDriver(c);
            case SQLSERVER:
                return new SqlServerDatabaseDriver(c);
            case JTDS_SQLSERVER:
                return new JtdsSqlServerDatabaseDriver(c);
        }
//        } catch (SQLException ex) {
//            throw new UncheckedSQLException(ex);
//        }
        throw new IllegalArgumentException("unsupported connection");
    }
}
