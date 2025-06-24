package net.thevpc.ndb.cmd.options;

import net.thevpc.nsql.NSqlConnectionStringBuilder;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.util.NBlankable;

import java.io.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.thevpc.nsql.NSqlDialect;

public class NDdbOptionsParser {
    public static NDdbOptions parse(NCmdLine cmdLine) {
        return parse(cmdLine, new File(System.getProperty("user.dir")));
    }

    public static NDdbOptions parse(NCmdLine cmdLine, File cwd) {
        NDdbOptions o = new NDdbOptions();
        while (cmdLine.hasNext()) {
            fillAny(cmdLine, o, cwd);
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
                        fillAny(NCmdLine.ofDefault(line), o, parentFile);
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void fillAny(NCmdLine cmdLine, NDdbOptions o, File cwd) {
        if (fillInclude(cmdLine, o, cwd)) {
            return;
        }
        if (fillCommons(cmdLine, o, cwd)) {
            return;
        }
        if (fillDB(cmdLine, o)) {
            return;
        }
        if (fillDebug(cmdLine, o)) {
            return;
        }
        if (fillSchemaMode(cmdLine, o)) {
            return;
        }
        if (fillActions(cmdLine, o)) {
            return;
        }
        NSession.of().configureLast(cmdLine);
    }

    public static boolean fillInclude(NCmdLine cmdLine, NDdbOptions o, File cwd) {
        NArg p = cmdLine.peek().get();
        if (p.isOption()) {
            switch (p.key()) {
                case "--@include": {
                    p = cmdLine.nextEntry().get();
                    File file = new File(cwd, p.getValue().asString().get());
                    fillFile(file, o);
                    return true;
                }
            }
        } else {
            if (p.getKey().asString().get().startsWith("@")) {
                p = cmdLine.next().get();
                File file = new File(cwd, p.key().substring(1));
                fillFile(file, o);
                return true;
            }
        }
        return false;
    }

    public static boolean fillCommons(NCmdLine cmdLine, NDdbOptions o, File cwd) {
        NArg p = cmdLine.peek().get();
        if (p.isOption()) {
            switch (p.key()) {
                case "--no-data": {
                    p = cmdLine.nextFlag().get();
                    o.data = !(p.getBooleanValue().get());
                    return true;
                }
                case "--data": {
                    p = cmdLine.nextFlag().get();
                    o.data = p.getBooleanValue().get();
                    return true;
                }
                case "--exploded": {
                    p = cmdLine.nextFlag().get();
                    o.exploded = p.getBooleanValue().get();
                    return true;
                }
                case "--max-rows": {
                    p = cmdLine.nextEntry().get();
                    o.maxRows = p.getValue().asLong().get();
                    return true;
                }
                case "--file": {
                    p = cmdLine.nextEntry().get();
                    o.file = p.stringValue();
                    if (o.file != null) {
                        if (!new File(o.file).isAbsolute()) {
                            o.file = new File(cwd, o.file).getPath();
                        }
                    }
                    return true;
                }
                case "--compress": {
                    p = cmdLine.nextFlag().get();
                    o.compress = p.getBooleanValue().get();
                    return true;
                }

                case "--tables":
                case "--table": {
                    p = cmdLine.nextEntry().get();
                    String v = p.stringValue();
                    if (v != null) {
                        o.tableNameFilter.add(v);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean fillActions(NCmdLine cmdLine, NDdbOptions o) {
        NArg p = cmdLine.peek().get();
        if (!p.isOption()) {
            if (o.action == null) {
                switch (p.key()) {
                    case "restore":
                    case "import":
                    case "dump-to-db":
                    case "dump2db": {
                        p = cmdLine.nextFlag().get();
                        o.action = NDdbOptions.Cmd.DUMP_TO_DB;
                        return true;
                    }
                    case "dump":
                    case "export":
                    case "db-to-export":
                    case "db2export": {
                        p = cmdLine.nextFlag().get();
                        o.action = NDdbOptions.Cmd.DB_TO_DUMP;
                        return true;
                    }
                    case "dump-to-json":
                    case "dump2json": {
                        p = cmdLine.nextFlag().get();
                        o.action = NDdbOptions.Cmd.DUMP_TO_JSON;
                        return true;
                    }
                    case "db-to-json":
                    case "db2json": {
                        p = cmdLine.nextFlag().get();
                        o.action = NDdbOptions.Cmd.DB_TO_JSON;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean fillDebug(NCmdLine cmdLine, NDdbOptions o) {
        NArg p = cmdLine.peek().get();
        if (p.isOption()) {
            switch (p.key()) {
                case "--debug":
                case "--verbose":
                case "--log-finest": {
                    p = cmdLine.nextFlag().get();
                    setDebugLevel(Level.FINEST);
                    return true;
                }
                case "--log-finer": {
                    p = cmdLine.nextFlag().get();
                    setDebugLevel(Level.FINER);
                    return true;
                }
                case "--log-fine": {
                    p = cmdLine.nextFlag().get();
                    setDebugLevel(Level.FINE);
                    return true;
                }
                case "--log-info": {
                    p = cmdLine.nextFlag().get();
                    setDebugLevel(Level.INFO);
                    return true;
                }
                case "--log-config": {
                    p = cmdLine.nextFlag().get();
                    setDebugLevel(Level.CONFIG);
                    return true;
                }
                case "--log-warning": {
                    p = cmdLine.nextFlag().get();
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

    public static boolean fillSchemaMode(NCmdLine cmdLine, NDdbOptions o) {
        NArg p = cmdLine.peek().get();
        if (p.isOption()) {
            switch (p.key()) {
                case "--drop-database": {
                    p = cmdLine.nextFlag().get();
                    o.schemaMode.setDropDatabase(p.booleanValue());
                    return true;
                }
                case "--database": {
                    p = cmdLine.nextFlag().get();
                    o.schemaMode.setDatabase(p.stringValue());
                    return true;
                }
                case "--create-database": {
                    p = cmdLine.nextFlag().get();
                    o.schemaMode.setCreateDatabase(p.booleanValue());
                    return true;
                }
                case "--create-table": {
                    p = cmdLine.nextFlag().get();
                    o.schemaMode.setCreateTable(p.booleanValue());
                    return true;
                }
                case "--create-column": {
                    p = cmdLine.nextFlag().get();
                    o.schemaMode.setCreateColumn(p.booleanValue());
                    return true;
                }
                case "--update-row": {
                    p = cmdLine.nextFlag().get();
                    o.schemaMode.setUpdateRow(p.booleanValue());
                    return true;
                }
                case "--drop-table": {
                    p = cmdLine.nextFlag().get();
                    o.schemaMode.setDropTable(p.booleanValue());
                    return true;
                }
                case "--clear-table": {
                    p = cmdLine.nextFlag().get();
                    o.schemaMode.setClearTable(p.booleanValue());
                    return true;
                }
                case "--drop-column": {
                    p = cmdLine.nextFlag().get();
                    o.schemaMode.setDropColumn(p.booleanValue());
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean fillDB(NCmdLine cmdLine, NDdbOptions o) {
        NArg p = cmdLine.peek().get();
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
                    o.cnx = NSqlConnectionStringBuilder.parse(p.getValue().asString().get());
                    return true;
                }
            }
        }
        return false;
    }
}
