package net.thevpc.diet.test;

import net.thevpc.nsql.dump.io.In;
import net.thevpc.nsql.dump.options.DumpToJsonOptions;
import net.thevpc.nsql.dump.store.NSqlDumpService;

import java.io.File;

public class DietTestExploded2 {
    public static void main(String[] args) {
        NSqlDumpService service = new NSqlDumpService();
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

