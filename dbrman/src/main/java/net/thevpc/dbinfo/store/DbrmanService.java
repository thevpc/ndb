package net.thevpc.dbinfo.store;

import net.thevpc.dbinfo.api.DatabaseDriver;
import net.thevpc.dbinfo.json.DbJsonDumper;
import net.thevpc.dbinfo.json.StoreReaderJsonDumper;
import net.thevpc.dbinfo.options.DbToDumpOptions;
import net.thevpc.dbinfo.options.DbToJsonOptions;
import net.thevpc.dbinfo.options.DumpToDbOptions;
import net.thevpc.dbinfo.options.DumpToJsonOptions;

public class DbrmanService {
    public void dumpToDb(DumpToDbOptions options, DatabaseDriver driver){
        new ActionImportDumpHelper().run(options,driver);
    }
    public void dbToDump(DbToDumpOptions options, DatabaseDriver driver){
        new ActionExportDumpHelper().run(options,driver);
    }
    public void dbToJson(DbToJsonOptions options, DatabaseDriver driver){
        new DbJsonDumper().run(options,driver);
    }
    public void dumpToJson(DumpToJsonOptions options){
        new StoreReaderJsonDumper().dumpFileToJson(options);
    }

}
