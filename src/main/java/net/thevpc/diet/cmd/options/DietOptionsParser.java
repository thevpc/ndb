package net.thevpc.diet.cmd.options;

import net.thevpc.diet.sql.DatabaseDriverFactory;
import net.thevpc.diet.util.Param;
import net.thevpc.diet.util.StringUtils;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DietOptionsParser {
    public static DietOptions parse(String... args) {
        DietOptions o = new DietOptions();
        for (String arg : args) {
            Param p = new Param(arg);
            if (p.isOption()) {
                switch (p.getKey()) {
                    case "--no-data": {
                        o.data = !(StringUtils.isBlank(p.getValue()) || Boolean.parseBoolean(p.getValue()));
                        break;
                    }
                    case "--data": {
                        o.data = (StringUtils.isBlank(p.getValue()) || Boolean.parseBoolean(p.getValue()));
                        break;
                    }
                    case "--exploded": {
                        o.exploded = (StringUtils.isBlank(p.getValue()) || Boolean.parseBoolean(p.getValue()));
                        break;
                    }
                    case "--max-rows": {
                        o.maxRows = Long.parseLong(p.getValue());
                        break;
                    }
                    case "--file": {
                        o.file = p.getValue();
                        break;
                    }
                    case "--compress": {
                        o.compress = (StringUtils.isBlank(p.getValue()) || Boolean.parseBoolean(p.getValue()));
                        break;
                    }

                    case "--tables":
                    case "--table": {
                        String v = p.getValue();
                        if (v != null) {
                            o.tableNameFilter.add(v);
                        }
                        break;
                    }
                    default: {
                        if (!fillDB(p, o)) {
                            if (!fillDebug(p, o)) {
                                if (!fillSchemaMode(p, o)) {
                                    throw new IllegalArgumentException("unsupported option " + arg);
                                }
                            }
                        }
                    }
                }
            } else {
                if (o.action == null) {
                    switch (arg) {
                        case "import":
                        case "export":
                        case "json": {
                            o.action = DietOptions.Cmd.valueOf(arg.toUpperCase());
                            break;
                        }
                        default: {
                            throw new IllegalArgumentException("unsupported action " + arg);
                        }
                    }
                } else {
                    throw new IllegalArgumentException("unsupported action " + arg);
                }
            }
        }
        return o;
    }

    public static boolean fillDebug(Param p, DietOptions o) {
        switch (p.getKey()) {
            case "--debug":
            case "--verbose":
            case "--log-finest": {
                setDebugLevel(Level.FINEST);
                return true;
            }
            case "--log-finer": {
                setDebugLevel(Level.FINER);
                return true;
            }
            case "--log-fine": {
                setDebugLevel(Level.FINE);
                return true;
            }
            case "--log-info": {
                setDebugLevel(Level.INFO);
                return true;
            }
            case "--log-config": {
                setDebugLevel(Level.CONFIG);
                return true;
            }
            case "--log-warning": {
                setDebugLevel(Level.WARNING);
                return true;
            }
        }
        return false;
    }

    public static void setDebugLevel(Level level) {
        Logger logger = Logger.getLogger("net.thevpc.diet");
        logger.setLevel(level);
        Logger logger0 = logger;
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n");
        while (logger0 != null) {
            for (Handler handler : logger0.getHandlers()) {
                handler.setLevel(level);
            }
            if (logger0.getUseParentHandlers()) {
                logger0 = logger0.getParent();
            } else {
                break;
            }
        }
        logger.log(Level.FINEST, "start debug mode : " + level);
    }

    public static boolean fillSchemaMode(Param p, DietOptions o) {
        switch (p.getKey()) {
            case "--drop-database": {
                o.schemaMode.setDropDatabase((StringUtils.isBlank(p.getValue()) || Boolean.parseBoolean(p.getValue())));
                return true;
            }
            case "--database": {
                o.schemaMode.setDatabase(p.getValue());
                return true;
            }
            case "--create-database": {
                o.schemaMode.setCreateDatabase((StringUtils.isBlank(p.getValue()) || Boolean.parseBoolean(p.getValue())));
                return true;
            }
            case "--create-table": {
                o.schemaMode.setCreateTable((StringUtils.isBlank(p.getValue()) || Boolean.parseBoolean(p.getValue())));
                return true;
            }
            case "--create-column": {
                o.schemaMode.setCreateColumn((StringUtils.isBlank(p.getValue()) || Boolean.parseBoolean(p.getValue())));
                return true;
            }
            case "--update-row": {
                o.schemaMode.setUpdateRow((StringUtils.isBlank(p.getValue()) || Boolean.parseBoolean(p.getValue())));
                return true;
            }
            case "--drop-table": {
                o.schemaMode.setDropTable((StringUtils.isBlank(p.getValue()) || Boolean.parseBoolean(p.getValue())));
                return true;
            }
            case "--clear-table": {
                o.schemaMode.setClearTable((StringUtils.isBlank(p.getValue()) || Boolean.parseBoolean(p.getValue())));
                return true;
            }
            case "--drop-column": {
                o.schemaMode.setDropColumn((StringUtils.isBlank(p.getValue()) || Boolean.parseBoolean(p.getValue())));
                return true;
            }
        }
        return false;
    }

    public static boolean fillDB(Param p, DietOptions o) {
        switch (p.getKey()) {
            case "--db-user": {
                o.cnx.setUser(p.getValue());
                return true;
            }
            case "--db-password": {
                o.cnx.setPassword(p.getValue());
                return true;
            }
            case "--db-url": {
                o.cnx.setUrl(p.getValue());
                return true;
            }
            case "--db-name": {
                o.cnx.setDbName(p.getValue());
                return true;
            }
            case "--db-host": {
                o.cnx.setHost(p.getValue());
                return true;
            }
            case "--db-port": {
                o.cnx.setPort(p.getValue());
                return true;
            }
            case "--db-type": {
                o.cnx.setType(DatabaseDriverFactory.checkValidDbType(p.getValue()));
                return true;
            }
            case "--db": {
                Pattern pat = Pattern.compile("(?<type>[a-z]+)://((?<user>[^:@/]+)(:(?<password>[^:@/]+))?@)?((?<host>[^:/?]*)(:(?<port>[0-9]+))?)/(?<db>[a-zA-Z0-9_-]+).*");
                Matcher m = pat.matcher(p.getValue());
                if (m.matches()) {
                    o.cnx.setType(DatabaseDriverFactory.checkValidDbType(m.group("type")));
                    o.cnx.setUser(m.group("user"));
                    o.cnx.setPassword(m.group("password"));
                    o.cnx.setHost(m.group("host"));
                    o.cnx.setPort(m.group("port"));
                    o.cnx.setDbName(m.group("db"));
                } else {
                    throw new IllegalArgumentException("invalid db url should be in the form dbtype://user:password@host:port/dbName");
                }
                return true;
            }
        }
        return false;
    }
}
