package net.thevpc.dbrman.store;

import net.thevpc.dbrman.api.DatabaseDriver;
import net.thevpc.dbrman.model.*;
import net.thevpc.dbrman.options.DumpToDbOptions;
import net.thevpc.dbrman.options.TableRestoreOptions;
import net.thevpc.dbrman.util.DbrIoHelper;
import net.thevpc.vio2.impl.StoreReader;
import net.thevpc.vio2.api.StoreRows;
import net.thevpc.vio2.api.StoreVisitor;
import net.thevpc.vio2.model.StoreDataType;
import net.thevpc.vio2.model.StoreStructDefinition;
import net.thevpc.vio2.util.FileUtils;
import net.thevpc.vio2.util.StringUtils;

import java.io.File;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ActionImportDumpHelper {

    public void run(DumpToDbOptions o, DatabaseDriver driver) {
        if (o.getSchemaMode() == null) {
            o.setSchemaMode(new TableRestoreOptions());
        }
        TableRestoreOptions schemaMode = o.getSchemaMode();
        if (schemaMode.isDropDatabase()) {
            if (StringUtils.isBlank(schemaMode.getDatabase())) {
                throw new IllegalArgumentException("missing database");
            }
            if (driver.databaseExists(schemaMode.getDatabase())) {
                driver.unuseDatabase();
                driver.createDatabase(schemaMode.getDatabase());
                driver.useDatabase(schemaMode.getDatabase());
            }
        } else {
            if (!StringUtils.isBlank(schemaMode.getDatabase())) {
                if (!driver.databaseExists(schemaMode.getDatabase())) {
                    driver.createDatabase(schemaMode.getDatabase());
                    driver.useDatabase(schemaMode.getDatabase());
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

    private void importDumpFile(File file, DumpToDbOptions o, DatabaseDriver driver) {
//        s.setOut(file);
        try (StoreReader w = new StoreReader(file)) {
            Predicate<String> t = o.getTableNameFilter().asPredicate();
            Predicate<TableDefinition> td = x -> t.test(x.getTableName());
            if (o.getSchemaMode() == null) {
                o.setSchemaMode(new TableRestoreOptions());
            }
            w.visit(new StoreVisitor() {
                private final TableRestoreOptions schemaMode = o.getSchemaMode();
                List<TableDefinition> md;

                @Override
                public void visitSchema(List<StoreStructDefinition> md) {
                    SchemaId ss = driver.getSchemaId();
                    for (StoreStructDefinition dd : md) {
                        prepareTableDefinition((TableDefinition) dd, schemaMode);
                    }
                    this.md = ((List<?>) md).stream().map(x -> ((TableDefinition) x).copy().setSchemaId(ss)).collect(Collectors.toList());

                    for (TableDefinition d : this.md) {
                        if (td.test(d)) {
                            driver.patchTable(d, schemaMode);
                        }
                    }

                    for (TableDefinition d : this.md) {
                        if (td.test(d)) {
                            driver.disableConstraints(d, schemaMode);
                        }
                    }
                }

                @Override
                public void visitData(StoreRows md) {
                    TableDefinition definition = (TableDefinition) md.getDefinition();
                    prepareTableDefinition(definition, schemaMode);
                    TableDefinition d = definition.copy().setSchemaId(driver.getSchemaId());
                    try {
                        if (o.isData()) {
                            StoreRows md2 = new StoreRowsAdapter(d, md);
                            if (td.test(d)) {
                                driver.importData(md2, schemaMode);
                                return;
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
                    for (TableDefinition d : md) {
                        if (td.test(d)) {
                            driver.enableConstraints(d, schemaMode);
                        }
                    }
                }
            });
        }
    }

    private void prepareTableDefinition(TableDefinition d, TableRestoreOptions schemaMode) {
        //Workaround
        for (ColumnDefinition column : d.getColumns()) {
            int precision = column.getPrecision();
            switch (column.getStoreType()) {
                case INT:
                case LONG:
                case BIG_INT: {
                    if (precision > 0) {
                        column.setStoreType(StoreDataType.BIG_DECIMAL);
                    }
                    break;
                }
                case NINT:
                case NLONG:
                case NBIG_INT:
                case NBIG_DECIMAL: {
                    if (precision > 0) {
                        column.setStoreType(StoreDataType.NBIG_DECIMAL);
                    }
                    break;
                }
                case BYTES:
                case NBYTES:
                case NBYTE_STREAM:
                case BYTE_STREAM:
                case CHAR_STREAM:
                case NCHAR_STREAM: {
                    if (schemaMode.getLobFolder() != null) {
                        column.setStoreType(StoreDataType.STRING);
                        column.setScale(2000);
                        break;
                    }
                    break;
                }
            }

        }
    }
}
