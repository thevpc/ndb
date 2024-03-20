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
//    private int index;
    private IoCell[] cells;

    public StoreReaderIoRow(StoreStructDefinition md, StoreInputStream dis) {
        this.md = md;
        this.dis = dis;
        this.columns = md.getColumns();
    }
    @Override
    public IoRow repeatable() {
        return new RepeatableReadIoCellArr(this);
    }

    public StoreStructDefinition getDefinition() {
        return md;
    }

    @Override
    public IoCell[] getColumns() {
        if(cells==null) {
            cells = new IoCell[md.getColumns().size()];
            for (int i = 0; i < cells.length; i++) {
                cells[i]=new StoreReaderIoCell(columns.get(i), dis).repeatable();
            }
        }
        return cells;
    }

//    @Override
//    public IoCell nextColumn() {
//        if (index < md.getColumns().size()) {
//            return new StoreReaderIoCell(columns.get(index++), dis);
//        } else {
//            return null;
//        }
//    }
}
