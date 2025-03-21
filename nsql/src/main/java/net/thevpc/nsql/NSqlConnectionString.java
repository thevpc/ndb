package net.thevpc.nsql;

import net.thevpc.nuts.util.*;
import net.thevpc.tson.TsonElement;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class NSqlConnectionString {
    private final String url;
    private final String username;
    private final String password;
    private final NSqlDialect dialect;
    private final String driverClass;

    public static NOptional<NSqlConnectionString> of(String url) {
        if (url == null) {
            return NOptional.ofNamedEmpty("url");
        }
        return NSqlConnectionStringBuilder.parse(url).build();
    }

    public static NOptional<NSqlConnectionString> of(NSqlConnectionStringBuilder info) {
        if (info == null) {
            return NOptional.ofNamedEmpty("builder");
        }
        return info.build();
    }


    public static NOptional<NSqlConnectionString> resolveCommonFromUrl(String url) {
        if (url != null) {
            if (url.startsWith("jdbc:postgresql:")) {
                return NOptional.of(
                        new NSqlConnectionString(url, "postgres", "postgres", NSqlDialect.POSTGRESQL, "org.postgresql.Driver")
                );
            } else if (url.startsWith("jdbc:mssqlserver:") || url.startsWith("jdbc:sqlserver:")) {
                return NOptional.of(
                        new NSqlConnectionString(url, "sa", null, NSqlDialect.MSSQLSERVER, "com.microsoft.sqlserver.jdbc.SQLServerDriver")
                );
            } else if (url.startsWith("jdbc:jtds:sqlserver:")) {
                return NOptional.of(
                        new NSqlConnectionString(url, "sa", null, NSqlDialect.MSSQLSERVER_JTDS, "net.sourceforge.jtds.jdbc.Driver")
                );
            } else if (url.startsWith("jdbc:jtds:sybase:")) {
                return NOptional.of(
                        new NSqlConnectionString(url, "sa", null, NSqlDialect.SYBASE, "net.sourceforge.jtds.jdbc.Driver")
                );
            }
        }
        return NOptional.ofNamedEmpty("common params for url " + url);
    }

    public static NSqlConnectionString ofTson(Function<String, TsonElement> props) {
        return new NSqlConnectionString(
                NSqlTsonUtils.stringOf(props.apply("url")),
                NSqlTsonUtils.stringOf(props.apply("username")),
                NSqlTsonUtils.stringOf(props.apply("password")),
                NSqlDialect.parse(NSqlTsonUtils.stringOf(props.apply("dialect"))).orNull(),
                NSqlTsonUtils.stringOf(props.apply("driverClass"))
        );
    }

    public static NSqlConnectionString ofString(Function<String, String> props) {
        return new NSqlConnectionString(
                props.apply("url"),
                props.apply("username"),
                props.apply("password"),
                NSqlDialect.parse(props.apply("dialect")).orNull(),
                props.apply("driverClass")
        );
    }

    public NSqlConnectionString(String url, String username, String password, NSqlDialect dialect, String driverClass) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.dialect = dialect;
        this.driverClass = driverClass;
    }

    public NSqlConnectionString autoResolve() {
        NOptional<NSqlConnectionString> o = resolveCommonFromUrl(url);
        if (o.isPresent()) {
            NSqlConnectionString bp = o.get();
            String url = this.url;
            String username = this.username;
            String password = this.password;
            NSqlDialect dialect = this.dialect;
            String driverClass = this.driverClass;

            if (username == null) {
                username = bp.username;
                if (password == null) {
                    password = bp.password;
                }
            }
            if (dialect == null) {
                dialect = bp.getDialect();
            }
            if (driverClass == null) {
                driverClass = bp.getDriverClass();
            }
            return new NSqlConnectionString(url, username, password, dialect, driverClass);
        }
        return this;
    }

    public NSqlDialect getDialect() {
        return dialect;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NSqlConnectionString that = (NSqlConnectionString) o;
        return Objects.equals(url, that.url) && Objects.equals(username, that.username) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, username, password);
    }

    @Override
    public String toString() {
        return "SqlConnectionParams{" +
                "url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", dialect=" + dialect +
                ", driverClass='" + driverClass + '\'' +
                '}';
    }
}
