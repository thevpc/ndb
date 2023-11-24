package net.thevpc.diet.io.v1.s;

import net.thevpc.diet.io.*;
import net.thevpc.diet.model.DefaultStoreValue;
import net.thevpc.diet.model.StoreColumnDefinition;
import net.thevpc.diet.model.StoreTableDefinition;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StoreRowsSer implements ObjectSerializer<StoreRows> {
    public static Logger LOG = Logger.getLogger(StoreRowsSer.class.getName());

    @Override
    public StoreRows read(StoreInputStream dis) {
        return new StoreReaderVXIoRows(dis);
    }

    @Override
    public void write(StoreRows rs, StoreOutputStream dos) {
        StoreTableDefinition md = rs.getDefinition();
        long startTime = System.currentTimeMillis();

        LOG.log(Level.FINE, "[" + md.toTableId().toStringId() + "] start writing... ");
        dos.writeNonNullableStruct(StoreTableDefinition.class, md);
        IoRow r;
        StoreColumnDefinition[] columns = md.getColumns();
        int columnsCount = columns.length;
        long rowCount = 0;
        while ((r = rs.nextRow()) != null) {
            rowCount++;
            LOG.log(Level.FINEST, "[" + md.toTableId().toStringId() + "] writing row " + rowCount + "... ");
            dos.writeNonNullPrefix();
            for (int i = 0; i < columnsCount; i++) {
                IoCell c = r.nextColumn();
                dos.writeStoreValue(DefaultStoreValue.ofAny(c.getMetaData().getStoreType(), c.getObject()));
            }
        }
        dos.writeNullPrefix();
        long endTime = System.currentTimeMillis();
        LOG.log(Level.FINEST, "[" + md.toTableId().toStringId() + "] written " + rowCount + " rows, " + columns.length + " columns (" + (endTime - startTime) + "ms)");
    }
}
