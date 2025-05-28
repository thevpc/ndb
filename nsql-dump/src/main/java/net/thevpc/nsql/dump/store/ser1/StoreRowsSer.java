package net.thevpc.nsql.dump.store.ser1;

import net.thevpc.lib.nserializer.api.*;
import net.thevpc.nsql.NSqlColumn;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.lib.nserializer.api.IOLogger;
import net.thevpc.lib.nserializer.impl.StoreReaderIoRows;

import net.thevpc.lib.nserializer.model.DefaultStoreValue;
import net.thevpc.lib.nserializer.model.StoreStructDefinition;

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

        doLog(NMsg.ofC("[%s] start writing... ", md.toStoreStructId().getFullName()));
        dos.writeNonNullableStruct(StoreStructDefinition.class, md);
        IoRow r;
        List<NSqlColumn> columns = (List) md.getColumns();
        int columnsCount = columns.size();
        long rowCount = 0;
        while ((r = rs.nextRow()) != null) {
            rowCount++;
            doLog(NMsg.ofC("[%s] writing row %s... ", md.toStoreStructId().getFullName(), rowCount));
            dos.writeNonNullPrefix();
            for (IoCell c : r.getColumns()) {
                dos.writeStoreValue(DefaultStoreValue.ofAny(c.getDefinition().getStoreType(), c.getObject()));
            }
        }
        dos.writeNullPrefix();
        long endTime = System.currentTimeMillis();
        doLog(NMsg.ofC("[%s] written %s rows, %s columns (%S ms)",
                md.toStoreStructId().getFullName(), rowCount, columns.size(), (endTime - startTime))
        );
    }

    protected void doLog(NMsg msg) {
        Level level = msg.getLevel();
        if (level == null) {
            level = Level.FINE;
        }
        LOG.log(level, msg::toString);
        IOLogger.get().log(msg);
    }
}
