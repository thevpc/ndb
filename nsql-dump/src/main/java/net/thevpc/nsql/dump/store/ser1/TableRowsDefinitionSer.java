package net.thevpc.nsql.dump.store.ser1;

import net.thevpc.nsql.dump.model.TableRowsDefinitionAsStoreRowsDefinition;
import net.thevpc.nsql.model.NSqlTableRowsDefinition;
import net.thevpc.nsql.NSqlColumn;
import net.thevpc.lib.nserializer.api.StoreInputStream;
import net.thevpc.lib.nserializer.api.StoreOutputStream;
import net.thevpc.lib.nserializer.api.ObjectSerializer;

public class TableRowsDefinitionSer implements ObjectSerializer<TableRowsDefinitionAsStoreRowsDefinition> {
    @Override
    public TableRowsDefinitionAsStoreRowsDefinition read(StoreInputStream dis) {
        NSqlTableRowsDefinition m = new NSqlTableRowsDefinition();
        m.setResultName(dis.readNullableString());
        m.setCatalogName(dis.readNullableString());
        m.setSchemaName(dis.readNullableString());
        m.setTableName(dis.readNullableString());
        m.setTableType(dis.readNullableString());
        m.setColumns(dis.readNonNullableStruct(NSqlColumn[].class));
        return new TableRowsDefinitionAsStoreRowsDefinition(m);
    }

    @Override
    public void write(TableRowsDefinitionAsStoreRowsDefinition dd, StoreOutputStream dos) {
        NSqlTableRowsDefinition rs = dd.getTableRowsDefinition();
        dos.writeNullableString(rs.getResultName());
        dos.writeNullableString(rs.getCatalogName());
        dos.writeNullableString(rs.getSchemaName());
        dos.writeNullableString(rs.getTableName());
        dos.writeNullableString(rs.getTableType());
        dos.writeNonNullableStruct(NSqlColumn[].class, rs.getColumns().toArray(new NSqlColumn[0]));
    }
}
