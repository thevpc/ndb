package net.thevpc.dbinfo.model;

import net.thevpc.dbinfo.util.DatabaseDriverFactories;
import net.thevpc.dbinfo.util.DbInfoModuleInstaller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CnxInfo implements Cloneable{
    static {
        DbInfoModuleInstaller.init();
    }
    private String host;
    private String port;
    private String dbName;
    private String type;
    private String url;
    private String user;
    private String password;
    private boolean integrationSecurity;

    public static CnxInfo parse(String value) {
        Pattern pat = Pattern.compile("(?<type>[a-z-]+)://((?<user>[^:@/]+)(:(?<password>[^:@/]+))?@)?((?<host>[^:/?]*)(:(?<port>[0-9]+))?)/(?<db>[a-zA-Z0-9_-]+).*");
        Matcher m = pat.matcher(value);
        if (m.matches()) {
            CnxInfo cnx=new CnxInfo();
            cnx.setType(DatabaseDriverFactories.checkValidDbType(m.group("type")));
            cnx.setUser(m.group("user"));
            cnx.setPassword(m.group("password"));
            cnx.setHost(m.group("host"));
            cnx.setPort(m.group("port"));
            cnx.setDbName(m.group("db"));
            return cnx;
        } else {
            throw new IllegalArgumentException("invalid db url should be in the form dbtype://user:password@host:port/dbName");
        }
    }

    public String getHost() {
        return host;
    }

    public CnxInfo setHost(String host) {
        this.host = host;
        return this;
    }

    public String getPort() {
        return port;
    }

    public CnxInfo setPort(String port) {
        this.port = port;
        return this;
    }

    public String getDbName() {
        return dbName;
    }

    public CnxInfo setDbName(String dbName) {
        this.dbName = dbName;
        return this;
    }

    public String getType() {
        return type;
    }

    public CnxInfo setType(String type) {
        this.type = type;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public CnxInfo setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getUser() {
        return user;
    }

    public CnxInfo setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public CnxInfo setPassword(String password) {
        this.password = password;
        return this;
    }

    public CnxInfo copy() {
        try {
            return (CnxInfo) clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isIntegrationSecurity() {
        return integrationSecurity;
    }

    public CnxInfo setIntegrationSecurity(boolean integrationSecurity) {
        this.integrationSecurity = integrationSecurity;
        return this;
    }
}
