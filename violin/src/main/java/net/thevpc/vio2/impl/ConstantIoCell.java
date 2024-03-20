package net.thevpc.vio2.impl;

import net.thevpc.vio2.api.IoCell;
import net.thevpc.vio2.model.StoreFieldDefinition;

public class ConstantIoCell extends AbstractIoCell {
    int i;
    StoreFieldDefinition d;
    Object val;
    boolean lob;

    public ConstantIoCell(StoreFieldDefinition d, Object val) {
        this.d = d;
        this.val = val;
        this.lob = isLobObject(val);
    }

    @Override
    public boolean isLob() {
        return lob;
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
