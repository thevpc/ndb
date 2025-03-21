package net.thevpc.nsql;

import net.thevpc.nuts.util.*;
import net.thevpc.tson.TsonElement;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NSqlConnectionStringBuilder implements Cloneable {

    private String host;
    private String applicationName;
    private String port;
    private String dbName;
    private NSqlDialect dialect;
    private String url;
    private String username;
    private String password;
    private String variant;
    private String instanceName;
    private String driverClass;
    private boolean integrationSecurity;

    public static NSqlConnectionStringBuilder parse(String value) {
        Pattern pat = Pattern.compile("(?<type>[a-z-]+)://((?<user>[^:@/]+)(:(?<password>[^:@/]+))?@)?((?<host>[^:/?]*)(:(?<port>[0-9]+))?)/(?<db>[a-zA-Z0-9_-]+).*");
        Matcher m = pat.matcher(value);
        if (m.matches()) {
            NSqlConnectionStringBuilder cnx = new NSqlConnectionStringBuilder();
            cnx.setDialect(NSqlDialect.parse(m.group("type")).orNull());
            cnx.setUsername(m.group("user"));
            cnx.setPassword(m.group("password"));
            cnx.setHost(m.group("host"));
            cnx.setPort(m.group("port"));
            cnx.setDbName(m.group("db"));
            return cnx;
        } else {
            throw new IllegalArgumentException("invalid db url should be in the form dbtype://user:password@host:port/dbName");
        }
    }


    public static NSqlConnectionStringBuilder ofTsonFunction(Function<String, TsonElement> props) {
        return new NSqlConnectionStringBuilder()
                .setUrl(NSqlTsonUtils.stringOf(props.apply("url")))
                .setUsername(NSqlTsonUtils.stringOf(props.apply("username")))
                .setPassword(NSqlTsonUtils.stringOf(props.apply("password")))
                .setDialect(NSqlDialect.parse(NSqlTsonUtils.stringOf(props.apply("dialect"))).orNull())
                .setDriverClass(NSqlTsonUtils.stringOf(props.apply("driverClass")));
    }

    public static NSqlConnectionStringBuilder ofStringFunction(Function<String, String> props) {
        return new NSqlConnectionStringBuilder()
                .setUrl(props.apply("url"))
                .setUsername(props.apply("username"))
                .setPassword(props.apply("password"))
                .setDialect(NSqlDialect.parse(props.apply("dialect")).orNull())
                .setDriverClass(props.apply("driverClass"));
    }

    public String getApplicationName() {
        return applicationName;
    }

    public NSqlConnectionStringBuilder setApplicationName(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public NSqlConnectionStringBuilder setInstanceName(String instanceName) {
        this.instanceName = instanceName;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public NSqlConnectionStringBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public NSqlConnectionStringBuilder setDriverClass(String driverClass) {
        this.driverClass = driverClass;
        return this;
    }

    public String getVariant() {
        return variant;
    }

    public NSqlConnectionStringBuilder setVariant(String variant) {
        this.variant = variant;
        return this;
    }

    public String getHost() {
        return host;
    }

    public NSqlConnectionStringBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    public String getPort() {
        return port;
    }

    public NSqlConnectionStringBuilder setPort(String port) {
        this.port = port;
        return this;
    }

    public String getDbName() {
        return dbName;
    }

    public NSqlConnectionStringBuilder setDbName(String dbName) {
        this.dbName = dbName;
        return this;
    }

    public NSqlDialect getDialect() {
        return dialect;
    }

    public NSqlConnectionStringBuilder setDialect(NSqlDialect type) {
        this.dialect = type;
        return this;
    }


    public String getUsername() {
        return username;
    }

    public NSqlConnectionStringBuilder setUsername(String user) {
        this.username = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public NSqlConnectionStringBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    public NSqlConnectionStringBuilder copy() {
        try {
            return (NSqlConnectionStringBuilder) clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isIntegrationSecurity() {
        return integrationSecurity;
    }

    public NSqlConnectionStringBuilder setIntegrationSecurity(boolean integrationSecurity) {
        this.integrationSecurity = integrationSecurity;
        return this;
    }

    public NOptional<NSqlConnectionString> build() {
        NSqlConnectionStringBuilder info = this;
        if (info != null) {
            NSqlDialect dialect = info.getDialect();
            if (dialect != null) {
                switch (dialect) {
                    case POSTGRESQL: {
                        info = prepareLoginPassword(info, "postgres", "postgres");
                        Map<String, String> p = new HashMap<>();
                        p.put("host", NStringUtils.firstNonBlank(info.getHost(), "localhost"));
                        String portString = preparePort(info.getPort(), "");
                        if (!portString.isEmpty()) {
                            portString = ":" + portString;
                        }
                        p.put("port", portString);
                        p.put("db", NStringUtils.firstNonBlank(info.getDbName(), "postgres"));
                        return NOptional.of(
                                new NSqlConnectionString(
                                        NMsg.ofV(NStringUtils.firstNonBlank("jdbc:postgresql://$host$port/$db",url), p).toString()
                                        , info.getUsername(), info.getPassword(), NSqlDialect.POSTGRESQL, NStringUtils.firstNonBlank(driverClass,"org.postgresql.Driver"))
                        );
                    }
                    case MSSQLSERVER: {
                        if ("jtds".equals(info.getVariant())) {
                            info = info.copy();
                            info.setDialect(NSqlDialect.MSSQLSERVER_JTDS);
                            return info.build();
                        }
                        info = prepareLoginPassword(info, "sa", null);
                        Map<String, String> p = new HashMap<>();
                        p.put("host", NStringUtils.firstNonBlank(info.getHost(), "localhost"));
                        String portString = preparePort(info.getPort(), "");
                        if (!portString.isEmpty()) {
                            portString = ":" + portString;
                        }
                        p.put("port", portString);
                        p.put("db", NStringUtils.firstNonBlank(info.getDbName(), "master"));
                        StringBuilder params = new StringBuilder();
                        if (!NBlankable.isBlank(info.getInstanceName())) {
                            params.append(";instanceName=").append(info.getInstanceName());
                            if (info.isIntegrationSecurity()) {
                                params.append(";integratedSecurity=true");
                            }
                            if (!NBlankable.isBlank(info.getApplicationName())) {
                                params.append(";applicationName=").append(info.getApplicationName());
                            }
                            if (!NBlankable.isBlank(info.getDbName())) {
                                params.append(";databaseName=").append(info.getDbName());
                            }
                        }
                        p.put("params", params.toString());
                        return NOptional.of(
                                new NSqlConnectionString(
                                        NMsg.ofV(NStringUtils.firstNonBlank("jdbc:sqlserver://$host$port$params",url), p).toString()
                                        , info.getUsername(), info.getPassword(), NSqlDialect.MSSQLSERVER, NStringUtils.firstNonBlank(driverClass,"com.microsoft.sqlserver.jdbc.SQLServerDriver"))
                        );
                    }
                    case MSSQLSERVER_JTDS: {
                        info = prepareLoginPassword(info, "sa", null);
                        Map<String, String> p = new HashMap<>();
                        p.put("host", NStringUtils.firstNonBlank(info.getHost(), "localhost"));
                        String portString = preparePort(info.getPort(), "");
                        if (!portString.isEmpty()) {
                            portString = ":" + portString;
                        }
                        p.put("port", portString);
                        p.put("db", NStringUtils.firstNonBlank(info.getDbName(), "master"));
                        StringBuilder params = new StringBuilder();
                        if (!NBlankable.isBlank(info.getInstanceName())) {
                            params.append(";instance=").append(info.getInstanceName());
                        }
                        p.put("params", params.toString());
                        return NOptional.of(
                                new NSqlConnectionString(
                                        NMsg.ofV(NStringUtils.firstNonBlank("jdbc:jtds:sqlserver://$host$port/$db$params",url), p).toString()
                                        , info.getUsername(), info.getPassword(), NSqlDialect.MSSQLSERVER_JTDS, NStringUtils.firstNonBlank(driverClass,"net.sourceforge.jtds.jdbc.Driver"))
                        );
                    }
                    case SYBASE: {
                        info = prepareLoginPassword(info, "sa", null);
                        Map<String, String> p = new HashMap<>();
                        p.put("host", NStringUtils.firstNonBlank(info.getHost(), "localhost"));
                        String portString = preparePort(info.getPort(), "");
                        if (!portString.isEmpty()) {
                            portString = ":" + portString;
                        }
                        p.put("port", portString);
                        p.put("db", NStringUtils.firstNonBlank(info.getDbName(), "master"));
                        StringBuilder params = new StringBuilder();
                        if (!NBlankable.isBlank(info.getInstanceName())) {
                            params.append(";instance=").append(info.getInstanceName());
                        }
                        p.put("params", params.toString());
                        return NOptional.of(
                                new NSqlConnectionString(
                                        NMsg.ofV(NStringUtils.firstNonBlank("jdbc:sybase://$host$port/$db$params",url), p).toString()
                                        , info.getUsername(), info.getPassword(), NSqlDialect.SYBASE, NStringUtils.firstNonBlank(driverClass,"net.sourceforge.jtds.jdbc.Driver"))
                        );
                    }
                    default:{
                        if(!NBlankable.isBlank(url) && !NBlankable.isBlank(driverClass)) {
                            return NOptional.of(new NSqlConnectionString(url, info.getUsername(), info.getPassword(), dialect, driverClass));
                        }
                    }
                }
            }
        }
        return NOptional.ofNamedEmpty("common params " + info);
    }

    private static NSqlConnectionStringBuilder prepareLoginPassword(NSqlConnectionStringBuilder ii, String defaultLogin, String defaultPassword) {
        if (NBlankable.isBlank(ii.getUsername()) && NBlankable.isBlank(ii.getPassword())) {
            if (!ii.isIntegrationSecurity()) {
                ii = ii.copy();
                ii = ii.setUsername(defaultLogin);
                ii = ii.setPassword(defaultPassword);
            }
        }
        return ii;
    }

    private static String preparePort(String port, String defaultValue) {
        if (!NBlankable.isBlank(port)) {
            NOptional<Integer> anInt = NLiteral.of(port).asInt();
            if (anInt.isPresent()) {
                int p = anInt.get();
                if (p > 0) {
                    return String.valueOf(p);
                }
            }
        }
        return defaultValue;
    }

}
