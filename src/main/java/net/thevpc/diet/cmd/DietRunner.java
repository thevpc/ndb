package net.thevpc.diet.cmd;

import net.thevpc.diet.cmd.options.DietOptions;
import net.thevpc.diet.io.DbStore;

import java.io.File;

public class DietRunner {
    public static void run(DietOptions o) {
        if (o.action == null) {
            throw new IllegalArgumentException("missing action");
        }
        switch (o.action) {
            case EXPORT: {
                doExport(o);
                return;
            }
            case IMPORT: {
                doImport(o);
                return;
            }
            case JSON: {
                doJson(o);
                return;
            }
            default: {
                throw new IllegalArgumentException("unsupported action " + o.action);
            }
        }
    }

    private static void doJson(DietOptions o) {
        new ActionJson(o, new DbStore().setOut(new File(o.file)))
                .run();
    }

    private static void doExport(DietOptions o) {
        new ActionExport(o, new DbStore().setDb(o.cnx).setOut(new File(o.file)))
                .run();
    }

    private static void doImport(DietOptions o) {
        new ActionImport(o, new DbStore().setDb(o.cnx).setOut(new File(o.file)))
                .run();
    }
}
