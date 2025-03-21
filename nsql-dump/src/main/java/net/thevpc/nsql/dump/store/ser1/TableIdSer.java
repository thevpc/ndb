package net.thevpc.nsql.dump.store.ser1;

import net.thevpc.nsql.dump.model.TableIdAsStoreStructId;
import net.thevpc.nsql.model.NSqlTableId;
import net.thevpc.lib.nserializer.api.ObjectSerializer;
import net.thevpc.lib.nserializer.api.StoreInputStream;
import net.thevpc.lib.nserializer.api.StoreOutputStream;

import java.util.logging.Logger;

public class TableIdSer implements ObjectSerializer<TableIdAsStoreStructId> {
    public static Logger LOG = Logger.getLogger(TableIdSer.class.getName());
    @Override
    public TableIdAsStoreStructId read(StoreInputStream dis) {
        NSqlTableId i = new NSqlTableId(
                dis.readNullableString(),
                dis.readNullableString(),
                dis.readNullableString()
        );
        return new TableIdAsStoreStructId(i);
    }

    @Override
    public void write(TableIdAsStoreStructId rs, StoreOutputStream dos) {
        NSqlTableId c = rs.getTableId();
        dos.writeNullableString(c.getCatalogName());
        dos.writeNullableString(c.getSchemaName());
        dos.writeNullableString(c.getTableName());
    }
}
