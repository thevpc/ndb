package net.thevpc.diet.cmd.options;

import net.thevpc.dbrman.model.CnxInfo;
import net.thevpc.dbrman.util.DatabaseDriverFactories;
import net.thevpc.vio2.util.CmdLineFileParser;
import net.thevpc.vio2.util.Param;
import net.thevpc.vio2.util.StringUtils;

import java.io.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DietOptionsParser {
    public static DietOptions parse(String... args) {
        return parse(args, new File(System.getProperty("user.dir")));
    }

    public static DietOptions parse(String[] args, File cwd) {
        DietOptions o = new DietOptions();
        for (String arg : args) {
            Param p = new Param(arg);
            fillAny(p, o, cwd);
        }
        return o;
    }


    public static void fillFile(File p, DietOptions o) {
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
                    if (StringUtils.isBlank(line)) {
                        for (String s : CmdLineFileParser.parseArgsFromLine(line)) {
                            fillAny(new Param(s), o, parentFile);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void fillAny(Param p, DietOptions o, File cwd) {
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

    public static boolean fillInclude(Param p, DietOptions o, File cwd) {
        if (p.isOption()) {
            switch (p.getKey()) {
                case "--@include": {
                    File file = new File(cwd, p.getValue());
                    fillFile(file, o);
                    return true;
                }
            }
        } else {
            if (p.getKey().startsWith("@")) {
                File file = new File(cwd, p.getKey().substring(1));
                fillFile(file, o);
                return true;
            }
        }
        return false;
    }

    public static boolean fillCommons(Param p, DietOptions o, File cwd) {
        if (p.isOption()) {
            switch (p.getKey()) {
                case "--no-data": {
                    o.data = !(StringUtils.isBlank(p.getValue()) || Boolean.parseBoolean(p.getValue()));
                    return true;
                }
                case "--data": {
                    o.data = (StringUtils.isBlank(p.getValue()) || Boolean.parseBoolean(p.getValue()));
                    return true;
                }
                case "--exploded": {
                    o.exploded = (StringUtils.isBlank(p.getValue()) || Boolean.parseBoolean(p.getValue()));
                    return true;
                }
                case "--max-rows": {
                    o.maxRows = Long.parseLong(p.getValue());
                    return true;
                }
                case "--file": {
                    o.file = p.getValue();
                    if(o.file!=null){
                        if(!new File(o.file).isAbsolute()){
                            o.file=new File(cwd,o.file).getPath();
                        }
                    }
                    return true;
                }
                case "--compress": {
                    o.compress = (StringUtils.isBlank(p.getValue()) || Boolean.parseBoolean(p.getValue()));
                    return true;
                }

                case "--tables":
                case "--table": {
                    String v = p.getValue();
                    if (v != null) {
                        o.tableNameFilter.add(v);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean fillActions(Param p, DietOptions o) {
        if (!p.isOption()) {
            if (o.action == null) {
                switch (p.getKey()) {
                    case "restore":
                    case "import":
                    case "dump-to-db":
                    case "dump2db": {
                        o.action = DietOptions.Cmd.DUMP_TO_DB;
                        return true;
                    }
                    case "dump":
                    case "export":
                    case "db-to-export":
                    case "db2export": {
                        o.action = DietOptions.Cmd.DB_TO_DUMP;
                        return true;
                    }
                    case "dump-to-json":
                    case "dump2json": {
                        o.action = DietOptions.Cmd.DUMP_TO_JSON;
                        return true;
                    }
                    case "db-to-json":
                    case "db2json": {
                        o.action = DietOptions.Cmd.DB_TO_JSON;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean fillDebug(Param p, DietOptions o) {
        if (p.isOption()) {
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

    public static boolean fillSchemaMode(Param p, DietOptions o) {
        if (p.isOption()) {
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
        }
        return false;
    }

    public static boolean fillDB(Param p, DietOptions o) {
        if (p.isOption()) {
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
                    o.cnx.setType(DatabaseDriverFactories.checkValidDbType(p.getValue()));
                    return true;
                }
                case "--db": {
                    o.cnx=CnxInfo.parse(p.getValue());
                    return true;
                }
            }
        }
        return false;
    }
}
