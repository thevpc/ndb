package net.thevpc.nsql;

import net.thevpc.nuts.util.NOptional;

public enum NSqlDialect {
    POSTGRESQL
    ,MSSQLSERVER
    ,MSSQLSERVER_JTDS
    ,SYBASE
    ,MYSQL
    ;

    public static NOptional<NSqlDialect> parse(String value) {
        if (value != null) {
            try {
                NSqlDialect u = NSqlDialect.valueOf(value.toUpperCase());
                return NOptional.of(u);
            } catch (Exception e) {
                //
            }
            switch (value.toUpperCase()){
                case "POSTGRES":return NOptional.of(NSqlDialect.POSTGRESQL);
                case "SQLSERVER":return NOptional.of(NSqlDialect.MSSQLSERVER);
                case "SQLSERVER_JTDS":
                case "SQLSERVER-JTDS":
                case "SQLSERVERJTDS":
                    return NOptional.of(NSqlDialect.MSSQLSERVER_JTDS);
            }
        }
        return NOptional.ofNamedEmpty("dialect for " + value);
    }
}
