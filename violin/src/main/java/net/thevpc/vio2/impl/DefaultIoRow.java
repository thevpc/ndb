package net.thevpc.vio2.impl;

import net.thevpc.vio2.api.IoCell;
import net.thevpc.vio2.api.IoRow;
import net.thevpc.vio2.model.StoreFieldDefinition;
import net.thevpc.vio2.model.StoreStructDefinition;

import java.util.List;

public class DefaultIoRow implements IoRow {
    private StoreStructDefinition def;
    private Object[] values;

    public DefaultIoRow(StoreStructDefinition def, Object[] values) {
        this.def = def;
        this.values = values;
    }

    private int index;
    @Override
    public IoCell nextColumn() {
        List<? extends StoreFieldDefinition> columns = def.getColumns();
        if (index < columns.size()) {
            ConstantIoCell y = new ConstantIoCell(
                    columns.get(index),
                    values[index]
            );
            index++;
            return y;
        }
        return null;
    }

    @Override
    public StoreStructDefinition getDefinition() {
        return def;
    }

}
