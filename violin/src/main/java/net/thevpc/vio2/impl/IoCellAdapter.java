package net.thevpc.vio2.impl;

import net.thevpc.vio2.api.IoCell;
import net.thevpc.vio2.model.DefaultStoreValue;
import net.thevpc.vio2.model.StoreFieldDefinition;
import net.thevpc.vio2.model.StoreValue;

public abstract class IoCellAdapter implements IoCell {
    protected StoreFieldDefinition d;
    protected IoCell other;

    public IoCellAdapter(StoreFieldDefinition d, IoCell other) {
        this.d = d;
        this.other = other;
    }

    @Override
    public StoreFieldDefinition getDefinition() {
        return d;
    }

    @Override
    public Object getObject() {
        return other.getObject();
    }

}
