package net.thevpc.nsql.dump.store;

import net.thevpc.nsql.dump.api.NSqlDump;
import net.thevpc.nsql.dump.model.TableDefinitionAsStoreStructDefinition;
import net.thevpc.nsql.dump.model.TableIdAsStoreStructId;
import net.thevpc.nsql.model.NSqlTableDefinition;
import net.thevpc.lib.nserializer.api.StoreRows;
import net.thevpc.lib.nserializer.api.StoreWriterModel;
import net.thevpc.lib.nserializer.model.StoreStructDefinition;
import net.thevpc.lib.nserializer.model.StoreStructId;

public class DatabaseDriverAsModel implements StoreWriterModel {
    private NSqlDump db;

    public DatabaseDriverAsModel(NSqlDump db) {
        this.db = db;
    }

    @Override
    public StoreStructDefinition getDefinition(StoreStructId id) {
        TableIdAsStoreStructId dd=(TableIdAsStoreStructId) id;
        NSqlTableDefinition tableDefinition = db.getConnection().getTableDefinition(dd.getTableId());
        return tableDefinition==null?null:new TableDefinitionAsStoreStructDefinition(tableDefinition);
    }

    @Override
    public StoreRows getRows(StoreStructId id) {
        TableIdAsStoreStructId dd=(TableIdAsStoreStructId) id;
        return db.getStoreRows(dd.getTableId());
    }
}
