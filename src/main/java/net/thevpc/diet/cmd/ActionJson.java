package net.thevpc.diet.cmd;

import net.thevpc.diet.cmd.options.DietOptions;
import net.thevpc.diet.io.DbStore;
import net.thevpc.diet.io.StoreReader;
import net.thevpc.diet.io.StoreReaderJsonDumper;
import net.thevpc.diet.util.FileUtils;

import java.io.File;
import java.util.logging.Logger;

public class ActionJson {
    public static Logger LOG = Logger.getLogger(ActionJson.class.getName());
    DietOptions o;
    DbStore s;

    public ActionJson(DietOptions o, DbStore s) {
        this.o = o;
        this.s = s;
    }

    public void run() {
        File file = new File(o.file);
        for (File f : FileUtils.expandFile(o.file)) {
            StoreReaderJsonDumper d = new StoreReaderJsonDumper(new StoreReader(f));
            d.dump(System.out);
        }
    }
}
