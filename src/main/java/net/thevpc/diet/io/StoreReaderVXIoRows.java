package net.thevpc.diet.io;

import net.thevpc.diet.model.StoreTableDefinition;

public class StoreReaderVXIoRows implements StoreRows {

    private final StoreTableDefinition md;
    private final StoreInputStream dis;

    public StoreReaderVXIoRows(StoreInputStream dis) {
        this.dis = dis;
        this.md = dis.readNonNullableStruct(StoreTableDefinition.class);
    }

    @Override
    public StoreTableDefinition getDefinition() {
        return md;
    }

    @Override
    public IoRow nextRow() {
        int b = dis.readNonNullableByte();
        if (b == 0) {
            return null;
        }
        return new StoreReaderVXIoRow(md, dis);
    }

    @Override
    public void close() {
        //sr.close();
    }

}
