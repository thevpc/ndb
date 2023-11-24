package net.thevpc.diet.cmd;

import net.thevpc.diet.cmd.options.DietOptions;
import net.thevpc.diet.io.*;
import net.thevpc.diet.model.StoreTableDefinition;
import net.thevpc.diet.model.TableHeader;
import net.thevpc.diet.sql.DatabaseDriver;
import net.thevpc.diet.sql.TableRestore;
import net.thevpc.diet.cmd.options.TableRestoreOptions;
import net.thevpc.diet.util.FileUtils;
import net.thevpc.diet.util.StringUtils;

import java.io.*;
import java.util.function.Predicate;

public class ActionImport {
    DietOptions o;
    DbStore s;

    public ActionImport(DietOptions o, DbStore s) {
        this.o = o;
        if (o.schemaMode == null) {
            o.schemaMode = new TableRestoreOptions();
        }
        this.s = s;
    }

    public void run() {
        DatabaseDriver dbHelper = s.getDb();
        TableRestoreOptions schemaMode = o.schemaMode;
        if (schemaMode.isDropDatabase()) {
            if (StringUtils.isBlank(schemaMode.getDatabase())) {
                throw new IllegalArgumentException("missing database");
            }
            if (dbHelper.databaseExists(schemaMode.getDatabase())) {
                dbHelper.unuseDatabase();
                dbHelper.createDatabase(schemaMode.getDatabase());
                dbHelper.useDatabase(schemaMode.getDatabase());
            }
        } else {
            if (!StringUtils.isBlank(schemaMode.getDatabase())) {
                if (!dbHelper.databaseExists(schemaMode.getDatabase())) {
                    dbHelper.createDatabase(schemaMode.getDatabase());
                    dbHelper.useDatabase(schemaMode.getDatabase());
                }
            }
        }
        for (File f : FileUtils.expandFile(o.file)) {
            run0(f);
        }
    }

    public void run0(File file) {
        s.setOut(file);
        try (StoreReader w = s.createReader()) {
            Predicate<TableHeader> t = o.tableNameFilter.asPredicate();
            w.visit(new StoreVisitor() {
                private final TableRestoreOptions schemaMode = o.schemaMode;
                StoreTableDefinition[] md;

                @Override
                public void visitSchema(StoreTableDefinition[] md) {
                    this.md = md;
                    for (StoreTableDefinition d : md) {
                        TableRestore tr = s.getDb().createTableRestore();
                        if (t.test(d.toTableHeader())) {
                            tr.updateSchema(d, schemaMode);
                        }
                    }
                    for (StoreTableDefinition d : md) {
                        TableRestore tr = s.getDb().createTableRestore();
                        if (t.test(d.toTableHeader())) {
                            tr.disableConstraints(d, schemaMode);
                        }
                    }
                }

                @Override
                public void visitData(StoreRows md) {
                    if (o.data) {
                        if (t.test(md.getDefinition().toTableHeader())) {
                            TableRestore tr = s.getDb().createTableRestore();
                            tr.importData(md, schemaMode);
                            return;
                        }
                    }
                    md.consume();
                }

                @Override
                public void visitEnd() {
                    for (StoreTableDefinition d : md) {
                        TableRestore tr = s.getDb().createTableRestore();
                        if (t.test(d.toTableHeader())) {
                            tr.enableConstraints(d, schemaMode);
                        }
                    }
                }
            });
        }
    }
}
