package net.thevpc.nsql.dump.store;

import net.thevpc.nsql.dump.DumpProgressEventImpl;
import net.thevpc.nsql.dump.DumpProgressEventType;
import net.thevpc.nsql.dump.DumpProgressMonitor;
import net.thevpc.nsql.dump.DumpProgressMonitors;
import net.thevpc.nsql.dump.api.NSqlDump;
import net.thevpc.nsql.dump.model.TableDefinitionAsStoreStructDefinition;
import net.thevpc.nsql.dump.options.DumpToDbOptions;
import net.thevpc.nsql.dump.options.TableRestoreOptions;
import net.thevpc.nsql.dump.util.DbrIoHelper;
import net.thevpc.nsql.dump.util.FileUtils;
import net.thevpc.nsql.NSqlColumn;
import net.thevpc.nsql.NSqlColumnType;
import net.thevpc.nsql.NSqlConnection;
import net.thevpc.nsql.model.NSqlSchemaId;
import net.thevpc.nsql.model.NSqlTableDefinition;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.lib.nserializer.impl.StoreReader;
import net.thevpc.lib.nserializer.api.StoreRows;
import net.thevpc.lib.nserializer.api.StoreVisitor;
import net.thevpc.lib.nserializer.model.StoreStructDefinition;
import net.thevpc.nuts.util.NIntRef;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NRef;

import java.io.File;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ActionImportDumpHelper {

    public void run(DumpToDbOptions o, NSqlDump driver) {
        if (o.getSchemaMode() == null) {
            o.setSchemaMode(new TableRestoreOptions());
        }
        TableRestoreOptions schemaMode = o.getSchemaMode();
        NSqlConnection connection = driver.getConnection();
        if (schemaMode.isDropDatabase()) {
            if (NBlankable.isBlank(schemaMode.getDatabase())) {
                throw new IllegalArgumentException("missing database");
            }
            if (connection.databaseExists(schemaMode.getDatabase())) {
                connection.unuseDatabase();
                connection.createDatabase(schemaMode.getDatabase());
                connection.useDatabase(schemaMode.getDatabase());
            }
        } else {
            if (!NBlankable.isBlank(schemaMode.getDatabase())) {
                if (!connection.databaseExists(schemaMode.getDatabase())) {
                    connection.createDatabase(schemaMode.getDatabase());
                    connection.useDatabase(schemaMode.getDatabase());
                }
            }
        }
        DbrIoHelper.check(o.getIn());
        if (o.getIn().getInputStream() != null) {
            throw new IllegalArgumentException("not supported yet : import from InputStream");
        }
        for (File f : FileUtils.expandExistingFiles(o.getIn().getFile().toString(), ".dump")) {
            importDumpFile(f, o, driver);
        }
    }

    private void importDumpFile(File file, DumpToDbOptions o, NSqlDump driver) {
        DumpProgressMonitor monitor = o.getMonitor()==null?DumpProgressMonitors.SILENT:o.getMonitor();
        NIntRef tableIndexRef = NRef.ofInt(0);
        NIntRef tableCountRef = NRef.ofInt(0);
//        s.setOut(file);
        try (StoreReader w = new StoreReader(file)) {
            Predicate<String> t = o.getTableNameFilter().asPredicate();
            Predicate<NSqlTableDefinition> td = x -> t.test(x.getTableName());
            if (o.getSchemaMode() == null) {
                o.setSchemaMode(new TableRestoreOptions());
            }
            w.visit(new StoreVisitor() {
                private final TableRestoreOptions schemaMode = o.getSchemaMode();
                List<NSqlTableDefinition> md;

                @Override
                public void visitSchema(List<StoreStructDefinition> md) {
                    NSqlSchemaId ss = driver.getConnection().getSchemaId();
                    for (StoreStructDefinition dd : md) {
                        TableDefinitionAsStoreStructDefinition ddd=(TableDefinitionAsStoreStructDefinition) dd;
                        prepareTableDefinition(ddd.getTableDefinition(), schemaMode);
                    }
                    this.md = ((List<?>) md).stream().map(x -> ((TableDefinitionAsStoreStructDefinition) x).getTableDefinition().copy().setSchemaId(ss)).collect(Collectors.toList());

                    for (NSqlTableDefinition d : this.md) {
                        if (td.test(d)) {
                            driver.getConnection().patchTable(d, schemaMode.toSqlPatchTableOptions());
                        }
                    }

                    for (NSqlTableDefinition d : this.md) {
                        if (td.test(d)) {
                            driver.getConnection().disableConstraints(d, schemaMode.toSqlPatchTableOptions());
                        }
                    }
                }

                @Override
                public void visitData(StoreRows md) {
                    tableIndexRef.inc();
                    int tableIndex = tableIndexRef.get();
                    StoreStructDefinition ssd = md.getDefinition();
                    NSqlTableDefinition definition = ((TableDefinitionAsStoreStructDefinition) ssd).getTableDefinition();
                    prepareTableDefinition(definition, schemaMode);
                    NSqlTableDefinition d = definition.copy().setSchemaId(driver.getConnection().getSchemaId());
                    try {
                        if (o.isData()) {
                            StoreRows md2 = new StoreRowsAdapter(d, md);
                            if (td.test(d)) {
                                tableCountRef.inc();
                                int tableCount = tableCountRef.get();
                                schemaMode.setTableIndex(tableIndex);
                                schemaMode.setTableCount(tableCount);
                                driver.importData(md2, schemaMode);
                                return;
                            }else{
                                String tableName = ((TableDefinitionAsStoreStructDefinition) ssd).getTableDefinition().getTableName();
                                monitor.onEvent(new DumpProgressEventImpl().setTableIndex(tableIndex)
                                        .setTableName(tableName).setEventType(DumpProgressEventType.SKIPPED_TABLE)
                                        .setProgress(Double.NaN).setMessage(NMsg.ofC("[%s] skipped table %s", tableName, tableIndex)));
                            }
                        }
                    } catch (UncheckedIOException ex) {
                        throw new UncheckedIOException(
                                "Error loading " + definition.getTableId().getFullName() + " -> " + d.getTableId().getFullName()
                                        + ": " + ex.getMessage(),
                                ex.getCause()
                        );
                    }
                }

                @Override
                public void visitEnd() {
                    for (NSqlTableDefinition d : md) {
                        if (td.test(d)) {
                            driver.getConnection().enableConstraints(d, schemaMode.toSqlPatchTableOptions());
                        }
                    }
                }
            });
        }
    }

    private void prepareTableDefinition(NSqlTableDefinition d, TableRestoreOptions schemaMode) {
        //Workaround
        for (NSqlColumn column : d.getColumns()) {
            int precision = column.getPrecision();
            switch (column.getColumnType()) {
                case INT:
                case LONG:
                case BIGINT: {
                    if (precision > 0) {
                        column.setColumnType(NSqlColumnType.BIGDECIMAL);
                    }
                    break;
                }
                case BLOB:
                case CLOB:{
                    if (schemaMode.getLobFolder() != null) {
                        column.setColumnType(NSqlColumnType.STRING);
                        column.setScale(2000);
                        break;
                    }
                    break;
                }
            }

        }
    }
}
