package net.thevpc.diet.io;

import net.thevpc.diet.model.StoreColumnDefinition;
import net.thevpc.diet.model.StoreTableDefinition;

class StoreReaderVXIoRow implements IoRow {

    private final StoreTableDefinition md;
    private final StoreInputStream dis;
    private final StoreColumnDefinition[] columns;
    private int index;

    public StoreReaderVXIoRow(StoreTableDefinition md, StoreInputStream dis) {
        this.md = md;
        this.dis = dis;
        this.columns = md.getColumns();
    }

    public StoreTableDefinition getDefinition() {
        return md;
    }

    @Override
    public IoCell nextColumn() {
        if (index < md.getColumns().length) {
            return new StoreReaderVXIoCell(columns[index++], dis);
        } else {
            return null;
        }
    }
}
