package net.thevpc.nsql.dump.store;

import net.thevpc.nsql.dump.model.TableDefinitionAsStoreStructDefinition;
import net.thevpc.nsql.model.NSqlTableDefinition;
import net.thevpc.lib.nserializer.api.IoRow;
import net.thevpc.lib.nserializer.api.StoreRows;
import net.thevpc.lib.nserializer.impl.AbstractStoreRows;
import net.thevpc.lib.nserializer.model.StoreStructDefinition;

public class StoreRowsAdapter extends AbstractStoreRows {
    private final NSqlTableDefinition d;
    private final StoreRows md;

    public StoreRowsAdapter(NSqlTableDefinition d, StoreRows md) {
        this.d = d;
        this.md = md;
    }

    @Override
    public StoreStructDefinition getDefinition() {
        return new TableDefinitionAsStoreStructDefinition(d);
    }

    @Override
    public IoRow nextRow() {
        return md.nextRow();
    }
}
