package net.thevpc.diet.test;

import net.thevpc.dbrman.io.In;
import net.thevpc.dbrman.options.DumpToJsonOptions;
import net.thevpc.dbrman.store.DbrmanService;
import net.thevpc.diet.cmd.DietRunner;
import net.thevpc.diet.cmd.options.DietOptionsParser;

import java.io.File;

public class DietTestExploded2 {
    public static void main(String[] args) {
        DbrmanService service = new DbrmanService();
        service.dumpToJson(
                new DumpToJsonOptions()
//                            .setIn(new In("/home/vpc/ttt/dump/"))
                        .setIn(new In("C:\\Users\\vpc\\Documents\\aaa"))
                        .setData(true)
                        .setLobFolder(new File("C:\\Users\\vpc\\Documents\\aaa-imported"))
                        .setPretty(true)
        );
    }
}

