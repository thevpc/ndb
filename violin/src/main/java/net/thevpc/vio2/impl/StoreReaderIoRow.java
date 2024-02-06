package net.thevpc.vio2.impl;

import net.thevpc.vio2.api.IoCell;
import net.thevpc.vio2.api.IoRow;
import net.thevpc.vio2.api.StoreInputStream;
import net.thevpc.vio2.model.StoreFieldDefinition;
import net.thevpc.vio2.model.StoreStructDefinition;

import java.util.List;

class StoreReaderIoRow implements IoRow {

    private final StoreStructDefinition md;
    private final StoreInputStream dis;
    private final List<? extends StoreFieldDefinition> columns;
    private int index;

    public StoreReaderIoRow(StoreStructDefinition md, StoreInputStream dis) {
        this.md = md;
        this.dis = dis;
        this.columns = md.getColumns();
    }

    public StoreStructDefinition getDefinition() {
        return md;
    }

    @Override
    public IoCell nextColumn() {
        if (index < md.getColumns().size()) {
            return new StoreReaderIoCell(columns.get(index++), dis);
        } else {
            return null;
        }
    }
}
