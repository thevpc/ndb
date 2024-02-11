package net.thevpc.dbrman.store.ser1;

import net.thevpc.dbrman.model.TableRowsDefinition;
import net.thevpc.vio2.api.StoreInputStream;
import net.thevpc.vio2.api.StoreOutputStream;
import net.thevpc.vio2.api.ObjectSerializer;
import net.thevpc.dbrman.model.ColumnDefinition;

public class TableRowsDefinitionSer implements ObjectSerializer<TableRowsDefinition> {
    @Override
    public TableRowsDefinition read(StoreInputStream dis) {
        TableRowsDefinition m = new TableRowsDefinition();
        m.setResultName(dis.readNullableString());
        m.setCatalogName(dis.readNullableString());
        m.setSchemaName(dis.readNullableString());
        m.setTableName(dis.readNullableString());
        m.setTableType(dis.readNullableString());
        m.setColumns(dis.readNonNullableStruct(ColumnDefinition[].class));
        return m;
    }

    @Override
    public void write(TableRowsDefinition rs, StoreOutputStream dos) {
        dos.writeNullableString(rs.getResultName());
        dos.writeNullableString(rs.getCatalogName());
        dos.writeNullableString(rs.getSchemaName());
        dos.writeNullableString(rs.getTableName());
        dos.writeNullableString(rs.getTableType());
        dos.writeNonNullableStruct(ColumnDefinition[].class, rs.getColumns().toArray(new ColumnDefinition[0]));
    }
}
