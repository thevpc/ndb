package net.thevpc.ndb.cmd;

import net.thevpc.nsql.NSqlConnectionStringBuilder;
import net.thevpc.nsql.dump.api.NSqlDump;
import net.thevpc.nsql.dump.io.Out;
import net.thevpc.nsql.dump.io.OutputProvider;
import net.thevpc.nsql.dump.options.DbToDumpOptions;
import net.thevpc.nsql.dump.options.DbToJsonOptions;
import net.thevpc.nsql.dump.options.DumpToDbOptions;
import net.thevpc.nsql.dump.options.DumpToJsonOptions;
import net.thevpc.nsql.dump.store.NSqlDumpService;
import net.thevpc.nsql.dump.io.In;
import net.thevpc.ndb.cmd.options.NDdbOptions;
import net.thevpc.nuts.NApp;
import net.thevpc.nuts.NSearchCmd;
import net.thevpc.nuts.NSession;

import java.io.File;

public class NDdbRunner {
    public static void run(NDdbOptions o) {
        if (NSession.of().isGui()) {
            ClassLoader cl = NSearchCmd.of("net.thevpc.ndb:ndb-desktop#0.8.5.1")
                    .getResultClassLoader();
            Class<?> aClass = null;
            try {
                aClass = Class.forName("net.thevpc.ndb.desktop.NDbDesktopRunner", true, cl);
                Runnable r = (Runnable) aClass.newInstance();
                r.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (o.action == null) {
            throw new IllegalArgumentException("missing action");
        }
        switch (o.action) {
            case DB_TO_DUMP: {
                dbToDump(o);
                return;
            }
            case DB_TO_JSON: {
                doDbToJson(o);
                return;
            }
            case DUMP_TO_DB: {
                dumpToDb(o);
                return;
            }
            case DUMP_TO_JSON: {
                dumToJson(o);
                return;
            }
            default: {
                throw new IllegalArgumentException("unsupported action " + o.action);
            }
        }
    }

    private static void dumToJson(NDdbOptions o) {
        DumpToJsonOptions options = new DumpToJsonOptions();
        options.setIn(new In(new File(o.file)));
        options.setOut(new Out(System.out));
        new NSqlDumpService().dumpToJson(options);
    }

    private static void dbToDump(NDdbOptions o) {
        try (NSqlDump driver = NSqlDump.of(o.cnx)) {
            DbToDumpOptions eo = new DbToDumpOptions();
            eo.out = new OutputProvider(new File(o.file));
            eo.compress = o.compress;
            eo.maxRows = o.maxRows;
            eo.tableNameFilter = o.tableNameFilter;
            eo.data = o.data;
            eo.exploded = o.exploded;
            new NSqlDumpService().dbToDump(eo, driver);
        }
    }

    private static void doDbToJson(NDdbOptions o) {
        try (NSqlDump driver = NSqlDump.of(o.cnx)) {
            DbToJsonOptions jo = new DbToJsonOptions();
            jo.exploded = o.exploded;
            jo.cnx = o.cnx == null ? new NSqlConnectionStringBuilder() : o.cnx.copy();
            jo.tableNameFilter = o.tableNameFilter;
            jo.data = o.data;
            jo.maxRows = o.maxRows;
            new NSqlDumpService().dbToJson(jo, driver);
        }
    }

    private static void dumpToDb(NDdbOptions o) {
        try (NSqlDump driver = NSqlDump.of(o.cnx)) {
            DumpToDbOptions io = new DumpToDbOptions();
            io.setData(o.data);
            io.setTableNameFilter(o.tableNameFilter);
            io.setSchemaMode(o.schemaMode);
            io.setIn(new In(o.file));
            new NSqlDumpService().dumpToDb(io, driver);
        }
    }
}
