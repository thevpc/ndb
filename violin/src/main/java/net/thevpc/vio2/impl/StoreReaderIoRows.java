package net.thevpc.vio2.impl;

import net.thevpc.vio2.api.IoRow;
import net.thevpc.vio2.api.StoreInputStream;
import net.thevpc.vio2.api.StoreRows;
import net.thevpc.vio2.model.StoreStructDefinition;

public class StoreReaderIoRows implements StoreRows {

    private final StoreStructDefinition md;
    private final StoreInputStream dis;

    public StoreReaderIoRows(StoreInputStream dis) {
        this.dis = dis;
        this.md = dis.readNonNullableStruct(StoreStructDefinition.class);
    }

    @Override
    public StoreStructDefinition getDefinition() {
        return md;
    }

    @Override
    public IoRow nextRow() {
        int b = dis.readNonNullableByte();
        if (b == 0) {
            return null;
        }
        return new StoreReaderIoRow(md, dis);
    }

    @Override
    public void close() {
        //sr.close();
    }

}
