package net.thevpc.dbinfo.store;

import net.thevpc.dbinfo.api.DatabaseDriver;
import net.thevpc.dbinfo.model.TableId;
import net.thevpc.vio2.api.StoreRows;
import net.thevpc.vio2.api.StoreWriterModel;
import net.thevpc.vio2.model.StoreStructDefinition;
import net.thevpc.vio2.model.StoreStructId;

public class DatabaseDriverAsModel implements StoreWriterModel {
    private DatabaseDriver db;

    public DatabaseDriverAsModel(DatabaseDriver db) {
        this.db = db;
    }

    @Override
    public StoreStructDefinition getDefinition(StoreStructId id) {
        return db.getTableDefinition((TableId) id);
    }

    @Override
    public StoreRows getRows(StoreStructId id) {
        return db.getTableRows((TableId) id);
    }
}
