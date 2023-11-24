package net.thevpc.diet.io;

import net.thevpc.diet.model.StoreColumnDefinition;
import net.thevpc.diet.model.StoreDataType;

import java.io.IOException;
import java.io.UncheckedIOException;

public class StoreReaderVXIoCell implements IoCell {

    private final StoreColumnDefinition column;
    private final StoreInputStream dis;

    public StoreReaderVXIoCell(StoreColumnDefinition column, StoreInputStream dis) {
        this.column = column;
        this.dis = dis;
    }

    @Override
    public StoreColumnDefinition getMetaData() {
        return column;
    }

    @Override
    public Object getObject() {
        try {
            StoreDataType fType = column.getStoreType();
            return dis.readAnyExpected(fType).getValue();
        } catch (UncheckedIOException e) {
            throw new UncheckedIOException(new IOException(
                    "unable to read column "
                            + column.getTableName()
                            + "."
                            + column.getColumnName()
                    , e.getCause()
            ));
        } catch (Exception e) {
            throw new UncheckedIOException(new IOException(
                    "unable to read column "
                            + column.getTableName()
                            + "."
                            + column.getColumnName()
                    , e
            ));
        }
    }
}
