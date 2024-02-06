package net.thevpc.dbinfo.store.ser1;

import net.thevpc.dbinfo.model.TableId;
import net.thevpc.vio2.api.ObjectSerializer;
import net.thevpc.vio2.api.StoreInputStream;
import net.thevpc.vio2.api.StoreOutputStream;

import java.util.logging.Logger;

public class TableIdSer implements ObjectSerializer<TableId> {
    public static Logger LOG = Logger.getLogger(TableIdSer.class.getName());
    @Override
    public TableId read(StoreInputStream dis) {
        return new TableId(
                dis.readNullableString(),
                dis.readNullableString(),
                dis.readNullableString()
        );
    }

    @Override
    public void write(TableId rs, StoreOutputStream dos) {
        dos.writeNullableString(rs.getCatalogName());
        dos.writeNullableString(rs.getSchemaName());
        dos.writeNullableString(rs.getTableName());
    }
}
