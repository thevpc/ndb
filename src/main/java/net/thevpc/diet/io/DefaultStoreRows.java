package net.thevpc.diet.io;

import net.thevpc.diet.model.StoreTableDefinition;

public class DefaultStoreRows implements StoreRows {
    private StoreTableDefinition def;
    private Object[][] rows;
    private int index;

    public DefaultStoreRows(StoreTableDefinition def, Object[][] rows) {
        this.def = def;
        this.rows = rows;
    }

    @Override
    public StoreTableDefinition getDefinition() {
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
