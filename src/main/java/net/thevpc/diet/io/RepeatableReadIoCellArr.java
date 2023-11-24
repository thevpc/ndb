package net.thevpc.diet.io;

import net.thevpc.diet.model.StoreColumnDefinition;
import net.thevpc.diet.model.StoreTableDefinition;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;

public class RepeatableReadIoCellArr implements Closeable {
    private RepeatableReadIoCell[] cells;
    private StoreTableDefinition metaData;

    public RepeatableReadIoCellArr(IoRow row) {
        metaData = row.getDefinition();
        StoreColumnDefinition[] columns = metaData.getColumns();
        cells = new RepeatableReadIoCell[columns.length];
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
