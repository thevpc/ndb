package net.thevpc.diet.io;

import net.thevpc.diet.model.StoreColumnDefinition;
import net.thevpc.diet.model.StoreTableDefinition;

public class DefaultIoRow implements IoRow {
    private StoreTableDefinition def;
    private Object[] values;

    public DefaultIoRow(StoreTableDefinition def, Object[] values) {
        this.def = def;
        this.values = values;
    }

    private int index;
    @Override
    public IoCell nextColumn() {
        if (index < def.getColumns().length) {
            MyIoCell y = new MyIoCell(
                    def.getColumns()[index],
                    values[index]
            );
            index++;
            return y;
        }
        return null;
    }

    @Override
    public StoreTableDefinition getDefinition() {
        return def;
    }

    private static class MyIoCell implements IoCell {
        int i;
        StoreColumnDefinition d;
        Object val;

        public MyIoCell(StoreColumnDefinition d, Object val) {
            this.d = d;
            this.val = val;
        }

        @Override
        public StoreColumnDefinition getMetaData() {
            return d;
        }

        @Override
        public Object getObject() {
            return val;
        }
    }
}
