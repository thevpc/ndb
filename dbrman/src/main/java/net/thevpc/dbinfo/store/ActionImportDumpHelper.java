package net.thevpc.dbinfo.store;

import net.thevpc.dbinfo.api.DatabaseDriver;
import net.thevpc.dbinfo.model.*;
import net.thevpc.dbinfo.options.DumpToDbOptions;
import net.thevpc.dbinfo.options.TableRestoreOptions;
import net.thevpc.dbinfo.util.DbrIoHelper;
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
        if(o.getIn().getInputStream()!=null){
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
                        prepareTableDefinition((TableDefinition) dd);
                    }
                    this.md = ((List<?>) md).stream().map(x->((TableDefinition)x).copy().setSchemaId(ss)).collect(Collectors.toList());

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
                    prepareTableDefinition(definition);
                    TableDefinition d = definition.copy().setSchemaId(driver.getSchemaId());
                    try {
                        if (o.isData()) {
                            StoreRows md2 = new StoreRowsAdapter(d, md);
                            if (td.test(d)) {
                                driver.importData(md2, schemaMode);
                                return;
                            }
                        }
                        md.consume();
                    }catch (UncheckedIOException ex){
                        throw new UncheckedIOException(
                                "Error loading "+definition.getTableId().getFullName()+" -> "+d.getTableId().getFullName()
                                        +": "+ex.getMessage(),
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

    private void prepareTableDefinition(TableDefinition d){
        //Workaround
        for (ColumnDefinition column : d.getColumns()) {
            int precision = column.getPrecision();
            if(precision>0){
                switch (column.getStoreType()){
                    case INT:
                    case LONG:
                    case BIG_INT:
                    {
                        column.setStoreType(StoreDataType.BIG_DECIMAL);
                        break;
                    }
                    case NINT:
                    case NLONG:
                    case NBIG_INT:
                    case NBIG_DECIMAL:
                    {
                        column.setStoreType(StoreDataType.NBIG_DECIMAL);
                        break;
                    }
                }
            }
        }
    }
}
