package net.thevpc.vio2.impl;

import net.thevpc.vio2.api.IoCell;
import net.thevpc.vio2.api.IoRow;
import net.thevpc.vio2.model.StoreFieldDefinition;
import net.thevpc.vio2.model.StoreStructDefinition;
import net.thevpc.vio2.model.StoreValue;

import java.util.List;

public class IoRowAdapter implements IoRow {
    private StoreStructDefinition def;
    private IoRow other;

    public IoRowAdapter(StoreStructDefinition def, IoRow other) {
        this.def = def;
        this.other = other;
    }

    private int index;
    @Override
    public IoCell nextColumn() {
        List<? extends StoreFieldDefinition> columns = def.getColumns();
        if (index < columns.size()) {
            IoCell b = other.nextColumn();
            IoCell n=new IoCellAdapter(columns.get(index),b) {
                @Override
                public StoreValue getValue() {
                    return b.getValue();
                }
            };
            index++;
            return n;
        }
        return null;
    }

    @Override
    public StoreStructDefinition getDefinition() {
        return def;
    }

}
