package net.thevpc.nsql.dump.json;

import net.thevpc.nsql.dump.model.TableHeaderAsStoreStructHeader;
import net.thevpc.nsql.model.NSqlTableHeader;
import net.thevpc.nsql.dump.options.DbToJsonOptions;
import net.thevpc.nsql.dump.util.DbrIoHelper;
import net.thevpc.lib.nserializer.api.StoreWriter;
import net.thevpc.lib.nserializer.model.StoreStructHeader;
import net.thevpc.lib.nserializer.model.StoreStructId;
import net.thevpc.nsql.dump.api.NSqlDump;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DbJsonDumper {

    public void run(DbToJsonOptions o, NSqlDump driver) {
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
            Predicate<StoreStructHeader> pred = ss -> (pred2.test(((NSqlTableHeader) ss).getTableName()));
            List<StoreStructId> tables = new ArrayList<>();
            for (StoreStructHeader table : driver.getConnection().getTableHeaders().stream().map(x->new TableHeaderAsStoreStructHeader(x))
                    .filter(x -> {
                        if (pred == null) {
                            return true;
                        }
                        return pred.test(x);
                    })
                    .collect(Collectors.toList())
            ) {
                tables.add(table.toStructId());
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
                w.addStructs(driver.getConnection().getTableHeaders().stream().filter(ss -> predicate.test(ss.getTableName()))
                        .map(x->new TableHeaderAsStoreStructHeader(x))
                        .map(x->x.toStructId())
                        .toArray(StoreStructId[]::new));
                w.write();
                w.flush();
            }
        }
    }
}
