package net.thevpc.diet.cmd;

import net.thevpc.diet.cmd.options.DietOptions;
import net.thevpc.diet.io.DbStore;
import net.thevpc.diet.io.StoreWriter;
import net.thevpc.diet.model.TableHeader;
import net.thevpc.diet.model.TableId;
import net.thevpc.diet.sql.DatabaseDriver;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ActionExport {
    public static Logger LOG = Logger.getLogger(ActionExport.class.getName());
    DietOptions o;
    DbStore s;

    public ActionExport(DietOptions o, DbStore s) {
        this.o = o;
        this.s = s;
    }

    public void run() {
        if (o.exploded) {
            DatabaseDriver db = s.getDb();
            OutputStream out = s.getOut();
            if (out != null) {
                throw new IllegalArgumentException("Unsupported exploded with output");
            }
            File file = s.getFile();
            if (file == null) {
                file = new File(o.cnx.getDbName() + ".dump");
            }
            Predicate<TableHeader> pred = o.tableNameFilter.asPredicate();
            List<TableId> tables = new ArrayList<>();
            for (TableHeader table : db.getTables().stream()
                    .filter(x -> "TABLE".equals(x.getTableType()))
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
            for (TableId table : tables) {
                File file2 = file;
                if (file2.isDirectory()) {
                    file2 = new File(file2, table.toStringId() + ".dump");
                } else {
                    String n = file2.getName();
                    int i = n.lastIndexOf('.');
                    if (i >= 0) {
                        n = n.substring(0, i) + "-" + table.toStringId() + n.substring(i);
                    } else {
                        n = n + "-" + table.toStringId();
                    }
                    file2 = file2.getParentFile() == null ? new File(n) : new File(file2.getParentFile(), n);
                }
                s.setOut(file2);
                try (StoreWriter w = s.createWriter()) {
                    w.setCompress(o.compress);
                    w.setData(o.data);
                    w.setMaxRows(o.maxRows);
                    w.addTables(x -> x.toTableId().equals(table));
                    w.write();
                    w.flush();
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        } else {
            File file = s.getFile();
            if (file == null) {
                file = new File(o.cnx.getDbName() + ".dump");
            }
            if (file.isDirectory()) {
                file = new File(file, o.cnx.getDbName() + ".dump");
            }
            s.setOut(file);
            try (StoreWriter w = s.createWriter()) {
                w.setCompress(o.compress);
                w.setData(o.data);
                w.setMaxRows(o.maxRows);
                w.addTables(o.tableNameFilter.asPredicate());
                w.write();
                w.flush();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }
}
