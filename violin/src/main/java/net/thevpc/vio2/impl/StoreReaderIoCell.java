package net.thevpc.vio2.impl;

import net.thevpc.vio2.api.IoCell;
import net.thevpc.vio2.api.StoreInputStream;
import net.thevpc.vio2.model.StoreDataType;
import net.thevpc.vio2.model.StoreFieldDefinition;

import java.io.IOException;
import java.io.UncheckedIOException;

public class StoreReaderIoCell implements IoCell {

    private final StoreFieldDefinition column;
    private final StoreInputStream dis;

    public StoreReaderIoCell(StoreFieldDefinition column, StoreInputStream dis) {
        this.column = column;
        this.dis = dis;
    }

    @Override
    public StoreFieldDefinition getDefinition() {
        return column;
    }

    @Override
    public Object getObject() {
        try {
            StoreDataType fType = column.getStoreType();
            return dis.readAnyExpected(fType).getValue();
        } catch (UncheckedIOException e) {
            throw new UncheckedIOException(new IOException(
                    "unable to read column " + column.getFullName()
                    , e.getCause()
            ));
        } catch (Exception e) {
            throw new UncheckedIOException(new IOException(
                    "unable to read column " + column.getFullName(), e
            ));
        }
    }
}
