package net.thevpc.dbrman.store;

import net.thevpc.dbrman.api.DatabaseDriver;
import net.thevpc.dbrman.json.DbJsonDumper;
import net.thevpc.dbrman.json.StoreReaderJsonDumper;
import net.thevpc.dbrman.options.DbToDumpOptions;
import net.thevpc.dbrman.options.DbToJsonOptions;
import net.thevpc.dbrman.options.DumpToDbOptions;
import net.thevpc.dbrman.options.DumpToJsonOptions;
import net.thevpc.dbrman.util.DbInfoModuleInstaller;

public class DbrmanService {
    static {
        DbInfoModuleInstaller.init();
    }
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
