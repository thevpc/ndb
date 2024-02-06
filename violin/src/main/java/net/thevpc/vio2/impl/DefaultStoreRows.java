package net.thevpc.vio2.impl;

import net.thevpc.vio2.api.IoRow;
import net.thevpc.vio2.api.StoreRows;
import net.thevpc.vio2.model.StoreStructDefinition;

public class DefaultStoreRows implements StoreRows {
    private StoreStructDefinition def;
    private Object[][] rows;
    private int index;

    public DefaultStoreRows(StoreStructDefinition def, Object[][] rows) {
        this.def = def;
        this.rows = rows;
    }

    @Override
    public StoreStructDefinition getDefinition() {
        return def;
    }

    @Override
    public IoRow nextRow() {
        if (index < rows.length) {
            IoRow c = new DefaultIoRow(def, rows[index]);
            index++;
            return c;
        }
        return null;
    }

    @Override
    public void close() {

    }
}
