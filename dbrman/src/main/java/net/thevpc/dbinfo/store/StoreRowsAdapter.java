package net.thevpc.dbinfo.store;

import net.thevpc.dbinfo.model.TableDefinition;
import net.thevpc.vio2.api.IoRow;
import net.thevpc.vio2.api.StoreRows;
import net.thevpc.vio2.model.StoreStructDefinition;

public class StoreRowsAdapter implements StoreRows {
    private final TableDefinition d;
    private final StoreRows md;

    public StoreRowsAdapter(TableDefinition d, StoreRows md) {
        this.d = d;
        this.md = md;
    }

    @Override
    public StoreStructDefinition getDefinition() {
        return d;
    }

    @Override
    public IoRow nextRow() {
        return md.nextRow();
    }
}
