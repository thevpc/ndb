package net.thevpc.vio2.impl;

import net.thevpc.vio2.api.IoCell;
import net.thevpc.vio2.api.IoRow;
import net.thevpc.vio2.model.StoreFieldDefinition;
import net.thevpc.vio2.model.StoreStructDefinition;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

public class RepeatableReadIoCellArr implements Closeable {
    private RepeatableReadIoCell[] cells;
    private StoreStructDefinition metaData;

    public RepeatableReadIoCellArr(IoRow row) {
        metaData = row.getDefinition();
        List<? extends StoreFieldDefinition> columns = metaData.getColumns();
        cells = new RepeatableReadIoCell[columns.size()];
        for (int i = 0; i < cells.length; i++) {
            cells[i] = new RepeatableReadIoCell(row.nextColumn());
        }
        //this will consume null
        IoCell z = row.nextColumn();
        if (z != null) {
            throw new UncheckedIOException(new IOException("expected null"));
        }
    }

    public int size() {
        return cells.length;
    }

    public RepeatableReadIoCell[] getCells() {
        return cells;
    }

    @Override
    public void close() {
        for (RepeatableReadIoCell cell : cells) {
            cell.close();
        }
    }
}
