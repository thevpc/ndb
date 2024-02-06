package net.thevpc.diet.test;

import net.thevpc.dbinfo.api.DatabaseDriver;
import net.thevpc.dbinfo.options.DumpToDbOptions;
import net.thevpc.dbinfo.options.DumpToJsonOptions;
import net.thevpc.dbinfo.options.TableRestoreOptions;
import net.thevpc.dbinfo.store.DbrmanService;
import net.thevpc.dbinfo.io.In;
import net.thevpc.diet.cmd.DietRunner;
import net.thevpc.diet.cmd.options.DietOptionsParser;

public class DietTestESA {
    public static void main(String[] args) {
        try (DatabaseDriver driver = DatabaseDriver.of("postgres://postgres:postgres@/esa")) {
            DbrmanService service = new DbrmanService();
//            service.dumpToDb(
//                    new DumpToDbOptions()
//                            .setIn(new In("/home/vpc/ttt/dump/"))
//                            .setData(true)
//                            .setSchemaMode(new TableRestoreOptions().setCreateTable(true))
//                    ,driver
//            );

            service.dumpToJson(
                    new DumpToJsonOptions()
                            .setIn(new In("/home/vpc/ttt/dump/"))
                            .setData(false)
            );

        }
    }
}
