package net.thevpc.diet.sql;

import net.thevpc.diet.cmd.options.TableRestoreOptions;
import net.thevpc.diet.io.StoreRows;
import net.thevpc.diet.model.StoreTableDefinition;

public interface TableImporter {
    void importData(StoreRows md, TableRestoreOptions schemaMode);

    void updateSchema(StoreTableDefinition definition, TableRestoreOptions schemaMode);


    void enableConstraints(StoreTableDefinition d, TableRestoreOptions schemaMode);

    void disableConstraints(StoreTableDefinition d, TableRestoreOptions schemaMode);
}
