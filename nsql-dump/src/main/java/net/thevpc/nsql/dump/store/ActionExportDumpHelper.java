package net.thevpc.nsql.dump.store;

import net.thevpc.lib.nserializer.api.StoreProgressMonitor;
import net.thevpc.nsql.dump.DumpProgressEventImpl;
import net.thevpc.nsql.dump.DumpProgressEventType;
import net.thevpc.nsql.dump.DumpProgressMonitor;
import net.thevpc.nsql.dump.DumpProgressMonitors;
import net.thevpc.nsql.dump.api.NSqlDump;
import net.thevpc.nsql.dump.model.DbStoreWriter;
import net.thevpc.nsql.dump.model.TableIdAsStoreStructId;
import net.thevpc.nsql.dump.options.DbToDumpOptions;
import net.thevpc.nsql.dump.util.DbrIoHelper;
import net.thevpc.nsql.model.NSqlTableHeader;
import net.thevpc.nsql.model.NSqlTableId;
import net.thevpc.lib.nserializer.api.StoreWriter;
import net.thevpc.nuts.util.NMsg;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ActionExportDumpHelper {
    public static Logger LOG = Logger.getLogger(ActionExportDumpHelper.class.getName());

    public void run(DbToDumpOptions o, NSqlDump driver) {
        DumpProgressMonitor monitor = o.getMonitor()==null? DumpProgressMonitors.SILENT:o.getMonitor();
        NSqlDump db = driver;
        DbrIoHelper.check(o.out);
        String databaseName = db.getConnection().getDatabaseName();
        if (o.exploded) {
            OutputStream out = o.out.getOutputStream();
            if (out != null) {
                throw new IllegalArgumentException("Unsupported exploded with output");
            }
            File file = o.out.getFile();
            if (file == null) {
                file = new File(databaseName + ".dump");
            }
            file = file.getAbsoluteFile();
            Predicate<String> pred = o.tableNameFilter.asPredicate();
            List<NSqlTableId> tables = new ArrayList<>();
            for (NSqlTableHeader table : db.getConnection().getAnyTables().stream()
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
            for (int j = 0; j < tables.size(); j++) {
                NSqlTableId table = tables.get(j);
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
                monitor.onEvent(new DumpProgressEventImpl().setEventType(DumpProgressEventType.START_TABLE).setTableIndex(j+1).setTableName(table.getTableName()).setProgress(Double.NaN)
                        .setMessage(NMsg.ofC("Exporting table [%s]", table.getTableName()))
                );
                try (StoreWriter w = new DbStoreWriter(file2, db)) {
                    w.addProgressMonitor(new StoreProgressMonitor() {
                        @Override
                        public void onProgress(double progress, NMsg message) {
                            monitor.onEvent(new DumpProgressEventImpl()
                                    .setProgress(progress)
                                    .setMessage(message)
                                    .setTableName(table.getTableName())
                                    .setEventType(DumpProgressEventType.PROCESSED_ROW)
                            );
                        }
                    });
                    w.setCompress(o.compress);
                    w.setData(o.data);
                    w.setMaxRows(o.maxRows);
                    w.addStructs(new TableIdAsStoreStructId(table));
                    w.write();
                    w.flush();
                }
                monitor.onEvent(new DumpProgressEventImpl().setEventType(DumpProgressEventType.START_TABLE).setTableIndex(j+1).setTableName(table.getTableName()).setProgress(Double.NaN)
                        .setMessage(NMsg.ofC("Exported table [%s]", table.getTableName()))
                );
            }
        } else {
            File file = o.out.getFile();
            if (file == null) {
                file = new File(databaseName + ".dump");
            }
            if (file.isDirectory()) {
                file = new File(file, databaseName + ".dump");
            }
            try (StoreWriter w = new DbStoreWriter(file, db)) {
                w.setCompress(o.compress);
                w.setData(o.data);
                w.setMaxRows(o.maxRows);
                w.addStructs(
                        db.getConnection().getTableIds().stream().filter(x -> o.tableNameFilter.asPredicate().test(x.getTableName())).map(TableIdAsStoreStructId::new).collect(Collectors.toList())
                );
                w.write();
                w.flush();
            }
        }
    }
}
