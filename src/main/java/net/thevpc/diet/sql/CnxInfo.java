package net.thevpc.diet.sql;

public class CnxInfo implements Cloneable{
    private String host;
    private String port;
    private String dbName;
    private String type;
    private String url;
    private String user;
    private String password;

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
}
