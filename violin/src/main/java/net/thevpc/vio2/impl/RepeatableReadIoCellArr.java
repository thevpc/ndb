package net.thevpc.vio2.impl;

import net.thevpc.vio2.api.IoCell;
import net.thevpc.vio2.api.IoRow;
import net.thevpc.vio2.model.StoreFieldDefinition;
import net.thevpc.vio2.model.StoreStructDefinition;

import java.util.List;

public class RepeatableReadIoCellArr implements IoRow {
    private IoCell[] cells;
    private StoreStructDefinition metaData;

    public RepeatableReadIoCellArr(IoRow row) {
        metaData = row.getDefinition();
        List<? extends StoreFieldDefinition> columns = metaData.getColumns();
        cells = new RepeatableReadIoCell[columns.size()];
        IoCell[] ccc = row.getColumns();
        for (int i = 0; i < cells.length; i++) {
            cells[i] = ccc[i].repeatable();
        }
        //this will consume null
//        IoCell z = row.nextColumn();
//        if (z != null) {
//            throw new UncheckedIOException(new IOException("expected null"));
//        }
    }

    @Override
    public IoRow repeatable() {
        return this;
    }

    @Override
    public StoreStructDefinition getDefinition() {
        return metaData;
    }

    public IoCell findColumn(String name) {
        for (IoCell cell : cells) {
            if(cell.getDefinition().getFieldName().equalsIgnoreCase(name)){
                return cell;
            }
        }
        return null;
    }
    public IoCell[] getColumns() {
        return cells;
    }

    @Override
    public void close() {
        for (IoCell cell : cells) {
            cell.close();
        }
    }
}
