package net.thevpc.diet.test;

import net.thevpc.dbrman.api.DatabaseDriver;
import net.thevpc.dbrman.options.DumpToDbOptions;
import net.thevpc.dbrman.options.TableRestoreOptions;
import net.thevpc.dbrman.store.DbrmanService;
import net.thevpc.dbrman.io.In;

public class DietTestESA {
    public static void main(String[] args) {
        try (DatabaseDriver driver = DatabaseDriver.of("postgres://postgres:postgres@/pfe1")) {
            DbrmanService service = new DbrmanService();
            service.dumpToDb(
                    new DumpToDbOptions()
//                            .setIn(new In("/home/vpc/ttt/dump/"))
                            .setIn(new In("/home/vpc/Documents/aaa/"))
                            .setData(true)
                            .setSchemaMode(new TableRestoreOptions().setCreateTable(true))
                    ,driver
            );

//            service.dumpToJson(
//                    new DumpToJsonOptions()
//                            .setIn(new In("/home/vpc/ttt/dump/"))
//                            .setData(false)
//            );

        }
    }
}
