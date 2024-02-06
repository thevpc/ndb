package net.thevpc.dbinfo.json;

import net.thevpc.dbinfo.model.TableHeader;
import net.thevpc.dbinfo.model.TableId;
import net.thevpc.dbinfo.options.DbToJsonOptions;
import net.thevpc.dbinfo.util.DbrIoHelper;
import net.thevpc.vio2.api.StoreWriter;
import net.thevpc.vio2.model.StoreStructHeader;
import net.thevpc.vio2.model.StoreStructId;
import net.thevpc.dbinfo.api.DatabaseDriver;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DbJsonDumper {

    public void run(DbToJsonOptions o, DatabaseDriver driver) {
        DbrIoHelper.check(o.pout);
        if (o.exploded) {
            OutputStream out = o.pout.getOutputStream();
            if (out != null) {
                throw new IllegalArgumentException("Unsupported exploded with output");
            }
            File file = o.pout.getFile();
            if (file == null) {
                file = new File(o.cnx.getDbName() + ".json");
            }
            Predicate<String> pred2 = o.tableNameFilter.asPredicate();
            Predicate<StoreStructHeader> pred = ss -> (pred2.test(((TableHeader) ss).getTableName()));
            List<StoreStructId> tables = new ArrayList<>();
            for (StoreStructHeader table : driver.getTableHeaders().stream()
                    .filter(x -> {
                        if (pred == null) {
                            return true;
                        }
                        return pred.test(x);
                    })
                    .collect(Collectors.toList())
            ) {
                tables.add(table.toTableId());
            }
            for (StoreStructId table : tables) {
                File file2 = file;
                if (file2.isDirectory()) {
                    file2 = new File(file2, table.getFullName() + ".json");
                } else {
                    String n = file2.getName();
                    int i = n.lastIndexOf('.');
                    if (i >= 0) {
                        n = n.substring(0, i) + "-" + table.getFullName() + n.substring(i);
                    } else {
                        n = n + "-" + table.getFullName();
                    }
                    file2 = file2.getParentFile() == null ? new File(n) : new File(file2.getParentFile(), n);
                }
                try (StoreWriter w = new JsonStoreWriter(file2, driver)) {
                    w.setData(o.data);
                    w.setMaxRows(o.maxRows);
                    w.addStructs(table);
                    w.write();
                    w.flush();
                }
            }
        } else {
            File file = o.pout.getFile();
            if (file == null) {
                file = new File(o.cnx.getDbName() + ".json");
            }
            if (file.isDirectory()) {
                file = new File(file, o.cnx.getDbName() + ".json");
            }
            try (StoreWriter w = new JsonStoreWriter(file, driver)) {
                w.setData(o.data);
                w.setMaxRows(o.maxRows);
                Predicate<String> predicate = o.tableNameFilter.asPredicate();
                w.addStructs(driver.getTableHeaders().stream().filter(ss -> predicate.test(((TableHeader) ss).getTableName()))
                        .map(TableHeader::toTableId)
                        .toArray(TableId[]::new));
                w.write();
                w.flush();
            }
        }
    }
}
