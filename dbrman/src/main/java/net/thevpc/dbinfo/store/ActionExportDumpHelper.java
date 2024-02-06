package net.thevpc.dbinfo.store;

import net.thevpc.dbinfo.model.*;
import net.thevpc.dbinfo.api.DatabaseDriver;
import net.thevpc.dbinfo.options.DbToDumpOptions;
import net.thevpc.dbinfo.util.DbrIoHelper;
import net.thevpc.vio2.api.StoreWriter;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ActionExportDumpHelper {
    public static Logger LOG = Logger.getLogger(ActionExportDumpHelper.class.getName());

    public void run(DbToDumpOptions o, DatabaseDriver driver) {
        DatabaseDriver db = driver;
        DbrIoHelper.check(o.outp);
        if (o.exploded) {
            OutputStream out = o.outp.getOutputStream();
            if (out != null) {
                throw new IllegalArgumentException("Unsupported exploded with output");
            }
            File file = o.outp.getFile();
            if (file == null) {
                file = new File(o.cnx.getDbName() + ".dump");
            }
            file = file.getAbsoluteFile();
            Predicate<String> pred = o.tableNameFilter.asPredicate();
            List<TableId> tables = new ArrayList<>();
            for (TableHeader table : db.getAnyTables().stream()
                    .filter(x -> "TABLE".equals(x.getTableType()))
                    .filter(x -> {
                        if (pred == null) {
                            return true;
                        }
                        return pred.test(x.getTableName());
                    })
                    .collect(Collectors.toList())
            ) {
                tables.add(table.toTableId());
            }
            for (TableId table : tables) {
                File file2 = file;
                if (file2.isDirectory()) {
                    file2 = new File(file2, table.getFullName() + ".dump");
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
                try (StoreWriter w = new DbStoreWriter(file2, db)) {
                    w.setCompress(o.compress);
                    w.setData(o.data);
                    w.setMaxRows(o.maxRows);
                    w.addStructs(table);
                    w.write();
                    w.flush();
                }
            }
        } else {
            File file = o.outp.getFile();
            if (file == null) {
                file = new File(o.cnx.getDbName() + ".dump");
            }
            if (file.isDirectory()) {
                file = new File(file, o.cnx.getDbName() + ".dump");
            }
            try (StoreWriter w = new DbStoreWriter(file, db)) {
                w.setCompress(o.compress);
                w.setData(o.data);
                w.setMaxRows(o.maxRows);
                w.addStructs(
                        db.getTableIds().stream().filter(x -> o.tableNameFilter.asPredicate().test(x.getTableName())).collect(Collectors.toList())
                );
                w.write();
                w.flush();
            }
        }
    }
}
