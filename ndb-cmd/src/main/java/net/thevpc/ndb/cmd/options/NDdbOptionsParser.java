package net.thevpc.ndb.cmd.options;

import net.thevpc.nsql.NSqlConnectionStringBuilder;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.util.NBlankable;

import java.io.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.thevpc.nsql.NSqlDialect;

public class NDdbOptionsParser {
    public static NDdbOptions parse(String... args) {
        return parse(args, new File(System.getProperty("user.dir")));
    }

    public static NDdbOptions parse(String[] args, File cwd) {
        NDdbOptions o = new NDdbOptions();
        for (String arg : args) {
            NArg p = NArg.of(arg);
            fillAny(p, o, cwd);
        }
        return o;
    }


    public static void fillFile(File p, NDdbOptions o) {
        File parentFile = p.getParentFile();
        if (parentFile == null) {
            parentFile = new File(System.getProperty("user.dir"));
        }
        try (BufferedReader br = new BufferedReader(new FileReader(p))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) {
                    //just ignore
                } else {
                    if (NBlankable.isBlank(line)) {
                        for (String s : CmdLineFileParser.parseArgsFromLine(line)) {
                            fillAny(NArg.of(s), o, parentFile);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void fillAny(NArg p, NDdbOptions o, File cwd) {
        if (fillInclude(p, o, cwd)) {
            return;
        }
        if (fillCommons(p, o,cwd)) {
            return;
        }
        if (fillDB(p, o)) {
            return;
        }
        if (fillDebug(p, o)) {
            return;
        }
        if (fillSchemaMode(p, o)) {
            return;
        }
        if (fillActions(p, o)) {
            return;
        }
        throw new IllegalArgumentException("unsupported option " + p);
    }

    public static boolean fillInclude(NArg p, NDdbOptions o, File cwd) {
        if (p.isOption()) {
            switch (p.key()) {
                case "--@include": {
                    File file = new File(cwd, p.getValue().asString().get());
                    fillFile(file, o);
                    return true;
                }
            }
        } else {
            if (p.getKey().asString().get().startsWith("@")){
                File file = new File(cwd, p.key().substring(1));
                fillFile(file, o);
                return true;
            }
        }
        return false;
    }

    public static boolean fillCommons(NArg p, NDdbOptions o, File cwd) {
        if (p.isOption()) {
            String pVal = p.getValue().asString().orNull();
            switch (p.key()) {
                case "--no-data": {
                    o.data = !(NBlankable.isBlank(p.getValue()) || Boolean.parseBoolean(pVal));
                    return true;
                }
                case "--data": {
                    o.data = (NBlankable.isBlank(p.getValue()) || Boolean.parseBoolean(pVal));
                    return true;
                }
                case "--exploded": {
                    o.exploded = (NBlankable.isBlank(p.getValue()) || Boolean.parseBoolean(pVal));
                    return true;
                }
                case "--max-rows": {
                    o.maxRows = Long.parseLong(pVal);
                    return true;
                }
                case "--file": {
                    o.file = pVal;
                    if(o.file!=null){
                        if(!new File(o.file).isAbsolute()){
                            o.file=new File(cwd,o.file).getPath();
                        }
                    }
                    return true;
                }
                case "--compress": {
                    o.compress = (NBlankable.isBlank(p.getValue()) || Boolean.parseBoolean(pVal));
                    return true;
                }

                case "--tables":
                case "--table": {
                    String v = pVal;
                    if (v != null) {
                        o.tableNameFilter.add(v);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean fillActions(NArg p, NDdbOptions o) {
        if (!p.isOption()) {
            if (o.action == null) {
                switch (p.key()) {
                    case "restore":
                    case "import":
                    case "dump-to-db":
                    case "dump2db": {
                        o.action = NDdbOptions.Cmd.DUMP_TO_DB;
                        return true;
                    }
                    case "dump":
                    case "export":
                    case "db-to-export":
                    case "db2export": {
                        o.action = NDdbOptions.Cmd.DB_TO_DUMP;
                        return true;
                    }
                    case "dump-to-json":
                    case "dump2json": {
                        o.action = NDdbOptions.Cmd.DUMP_TO_JSON;
                        return true;
                    }
                    case "db-to-json":
                    case "db2json": {
                        o.action = NDdbOptions.Cmd.DB_TO_JSON;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean fillDebug(NArg p, NDdbOptions o) {
        if (p.isOption()) {
            switch (p.key()) {
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
        }
        return false;
    }

    public static void setDebugLevel(Level level) {
        Logger logger = Logger.getLogger("net.thevpc");
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

    public static boolean fillSchemaMode(NArg p, NDdbOptions o) {
        if (p.isOption()) {
            String value = p.getValue().asString().orNull();
            switch (p.key()) {
                case "--drop-database": {
                    o.schemaMode.setDropDatabase((NBlankable.isBlank(value) || Boolean.parseBoolean(value)));
                    return true;
                }
                case "--database": {
                    o.schemaMode.setDatabase(value);
                    return true;
                }
                case "--create-database": {
                    o.schemaMode.setCreateDatabase((NBlankable.isBlank(value) || Boolean.parseBoolean(value)));
                    return true;
                }
                case "--create-table": {
                    o.schemaMode.setCreateTable((NBlankable.isBlank(value) || Boolean.parseBoolean(value)));
                    return true;
                }
                case "--create-column": {
                    o.schemaMode.setCreateColumn((NBlankable.isBlank(value) || Boolean.parseBoolean(value)));
                    return true;
                }
                case "--update-row": {
                    o.schemaMode.setUpdateRow((NBlankable.isBlank(value) || Boolean.parseBoolean(value)));
                    return true;
                }
                case "--drop-table": {
                    o.schemaMode.setDropTable((NBlankable.isBlank(value) || Boolean.parseBoolean(value)));
                    return true;
                }
                case "--clear-table": {
                    o.schemaMode.setClearTable((NBlankable.isBlank(value) || Boolean.parseBoolean(value)));
                    return true;
                }
                case "--drop-column": {
                    o.schemaMode.setDropColumn((NBlankable.isBlank(value) || Boolean.parseBoolean(value)));
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean fillDB(NArg p, NDdbOptions o) {
        if (p.isOption()) {
            switch (p.key()) {
                case "--db-user": {
                    o.cnx.setUsername(p.getValue().asString().get());
                    return true;
                }
                case "--db-password": {
                    o.cnx.setPassword(p.getValue().asString().get());
                    return true;
                }
                case "--db-url": {
                    o.cnx.setUrl(p.getValue().asString().get());
                    return true;
                }
                case "--db-name": {
                    o.cnx.setDbName(p.getValue().asString().get());
                    return true;
                }
                case "--db-host": {
                    o.cnx.setHost(p.getValue().asString().get());
                    return true;
                }
                case "--db-port": {
                    o.cnx.setPort(p.getValue().asString().get());
                    return true;
                }
                case "--db-type": {
                    o.cnx.setDialect(NSqlDialect.parse(p.getValue().asString().get()).get());
                    return true;
                }
                case "--db": {
                    o.cnx= NSqlConnectionStringBuilder.parse(p.getValue().asString().get());
                    return true;
                }
            }
        }
        return false;
    }
}
