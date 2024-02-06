package net.thevpc.vio2.impl;

import net.thevpc.vio2.api.IoCell;
import net.thevpc.vio2.model.StoreFieldDefinition;

public class ConstantIoCell implements IoCell {
    int i;
    StoreFieldDefinition d;
    Object val;

    public ConstantIoCell(StoreFieldDefinition d, Object val) {
        this.d = d;
        this.val = val;
    }

    @Override
    public StoreFieldDefinition getDefinition() {
        return d;
    }

    @Override
    public Object getObject() {
        return val;
    }
}
