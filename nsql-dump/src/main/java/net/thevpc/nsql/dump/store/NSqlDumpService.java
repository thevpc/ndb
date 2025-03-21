package net.thevpc.nsql.dump.store;

import net.thevpc.nsql.dump.api.NSqlDump;
import net.thevpc.nsql.dump.json.DbJsonDumper;
import net.thevpc.nsql.dump.json.StoreReaderJsonDumper;
import net.thevpc.nsql.dump.options.DbToDumpOptions;
import net.thevpc.nsql.dump.options.DbToJsonOptions;
import net.thevpc.nsql.dump.options.DumpToDbOptions;
import net.thevpc.nsql.dump.options.DumpToJsonOptions;
import net.thevpc.nsql.dump.util.NSqlDumpModuleInstaller;

public class NSqlDumpService {
    static {
        NSqlDumpModuleInstaller.init();
    }
    public void dumpToDb(DumpToDbOptions options, NSqlDump driver){
        new ActionImportDumpHelper().run(options,driver);
    }
    public void dbToDump(DbToDumpOptions options, NSqlDump driver){
        new ActionExportDumpHelper().run(options,driver);
    }
    public void dbToJson(DbToJsonOptions options, NSqlDump driver){
        new DbJsonDumper().run(options,driver);
    }
    public void dumpToJson(DumpToJsonOptions options){
        new StoreReaderJsonDumper().dumpFileToJson(options);
    }

}
