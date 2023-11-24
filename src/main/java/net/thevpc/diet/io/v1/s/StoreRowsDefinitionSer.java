package net.thevpc.diet.io.v1.s;

import net.thevpc.diet.io.StoreInputStream;
import net.thevpc.diet.io.StoreOutputStream;
import net.thevpc.diet.io.ObjectSerializer;
import net.thevpc.diet.model.StoreColumnDefinition;
import net.thevpc.diet.model.StoreRowsDefinition;

public class StoreRowsDefinitionSer implements ObjectSerializer<StoreRowsDefinition> {
    @Override
    public StoreRowsDefinition read(StoreInputStream dis) {
        StoreRowsDefinition m = new StoreRowsDefinition();
        m.setResultName(dis.readNullableString());
        m.setCatalogName(dis.readNullableString());
        m.setSchemaName(dis.readNullableString());
        m.setTableName(dis.readNullableString());
        m.setTableType(dis.readNullableString());
        m.setColumns(dis.readNonNullableStruct(StoreColumnDefinition[].class));
        return m;
    }

    @Override
    public void write(StoreRowsDefinition rs, StoreOutputStream dos) {
        dos.writeNullableString(rs.getResultName());
        dos.writeNullableString(rs.getCatalogName());
        dos.writeNullableString(rs.getSchemaName());
        dos.writeNullableString(rs.getTableName());
        dos.writeNullableString(rs.getTableType());
        dos.writeNonNullableStruct(StoreColumnDefinition[].class, rs.getColumns());
    }
}
