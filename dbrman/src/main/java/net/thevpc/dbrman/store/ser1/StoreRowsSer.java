package net.thevpc.dbrman.store.ser1;

import net.thevpc.vio2.api.*;
import net.thevpc.vio2.impl.IOLogger;
import net.thevpc.vio2.impl.StoreReaderIoRows;

import net.thevpc.vio2.model.DefaultStoreValue;
import net.thevpc.dbrman.model.ColumnDefinition;
import net.thevpc.vio2.model.StoreStructDefinition;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StoreRowsSer implements ObjectSerializer<StoreRows> {
    public static Logger LOG = Logger.getLogger(StoreRowsSer.class.getName());

    @Override
    public StoreRows read(StoreInputStream dis) {
        return new StoreReaderIoRows(dis);
    }

    @Override
    public void write(StoreRows rs, StoreOutputStream dos) {
        StoreStructDefinition md = rs.getDefinition();
        long startTime = System.currentTimeMillis();

        LOG.log(Level.FINE, "[" + md.toStoreStructId().getFullName() + "] start writing... ");
        IOLogger.current().log("[" + md.toStoreStructId().getFullName() + "] start writing... ");
        dos.writeNonNullableStruct(StoreStructDefinition.class, md);
        IoRow r;
        List<ColumnDefinition> columns = (List) md.getColumns();
        int columnsCount = columns.size();
        long rowCount = 0;
        while ((r = rs.nextRow()) != null) {
            rowCount++;
            LOG.log(Level.FINEST, "[" + md.toStoreStructId().getFullName() + "] writing row " + rowCount + "... ");
            IOLogger.current().log("[" + md.toStoreStructId().getFullName() + "] writing row " + rowCount + "... ");
            dos.writeNonNullPrefix();
            for (int i = 0; i < columnsCount; i++) {
                IoCell c = r.nextColumn();
                dos.writeStoreValue(DefaultStoreValue.ofAny(c.getDefinition().getStoreType(), c.getObject()));
            }
        }
        dos.writeNullPrefix();
        long endTime = System.currentTimeMillis();
        LOG.log(Level.FINEST, "[" + md.toStoreStructId().getFullName() + "] written " + rowCount + " rows, " + columns.size() + " columns (" + (endTime - startTime) + "ms)");
        IOLogger.current().log("[" + md.toStoreStructId().getFullName() + "] written " + rowCount + " rows, " + columns.size() + " columns (" + (endTime - startTime) + "ms)");
    }
}
