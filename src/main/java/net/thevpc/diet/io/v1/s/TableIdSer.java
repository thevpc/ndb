package net.thevpc.diet.io.v1.s;

import net.thevpc.diet.io.ObjectSerializer;
import net.thevpc.diet.io.StoreInputStream;
import net.thevpc.diet.io.StoreOutputStream;
import net.thevpc.diet.model.TableId;

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
