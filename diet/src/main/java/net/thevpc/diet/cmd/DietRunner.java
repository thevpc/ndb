package net.thevpc.diet.cmd;

import net.thevpc.dbrman.api.DatabaseDriver;
import net.thevpc.dbrman.options.DbToDumpOptions;
import net.thevpc.dbrman.options.DbToJsonOptions;
import net.thevpc.dbrman.options.DumpToDbOptions;
import net.thevpc.dbrman.options.DumpToJsonOptions;
import net.thevpc.dbrman.store.DbrmanService;
import net.thevpc.dbrman.io.In;
import net.thevpc.diet.cmd.options.DietOptions;

import java.io.File;

public class DietRunner {
    public static void run(DietOptions o) {
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

    private static void dumToJson(DietOptions o) {
        DumpToJsonOptions options = new DumpToJsonOptions();
        options.setIn(new In(new File(o.file)));
        options.setOut(System.out);
        new DbrmanService().dumpToJson(options);
    }

    private static void dbToDump(DietOptions o) {
        try(DatabaseDriver driver= DatabaseDriver.of(o.cnx)){
            DbToDumpOptions eo = new DbToDumpOptions();
            eo.file = o.file;
            eo.compress = o.compress;
            eo.maxRows = o.maxRows;
            eo.tableNameFilter = o.tableNameFilter;
            eo.data = o.data;
            eo.exploded = o.exploded;
            eo.cnx = o.cnx;
            new DbrmanService().dbToDump(eo,driver);
        }
    }

    private static void doDbToJson(DietOptions o) {
        try(DatabaseDriver driver= DatabaseDriver.of(o.cnx)){
            DbToJsonOptions jo=new DbToJsonOptions();
            jo.exploded=o.exploded;
            jo.cnx = o.cnx;
            jo.tableNameFilter = o.tableNameFilter;
            jo.data=o.data;
            jo.maxRows=o.maxRows;
            new DbrmanService().dbToJson(jo,driver);
        }
    }

    private static void dumpToDb(DietOptions o) {
        try(DatabaseDriver driver= DatabaseDriver.of(o.cnx)){
            DumpToDbOptions io=new DumpToDbOptions();
            io.setData(o.data);
            io.setTableNameFilter(o.tableNameFilter);
            io.setSchemaMode(o.schemaMode);
            new DbrmanService().dumpToDb(io,driver);
        }
    }
}
