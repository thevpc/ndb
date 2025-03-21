package net.thevpc.nsql.dump.store.ser1;

import net.thevpc.nsql.dump.model.TableDefinitionAsStoreStructDefinition;
import net.thevpc.nsql.model.NSqlTableDefinition;
import net.thevpc.nsql.NSqlColumn;
import net.thevpc.lib.nserializer.api.StoreInputStream;
import net.thevpc.lib.nserializer.api.StoreOutputStream;
import net.thevpc.lib.nserializer.api.ObjectSerializer;

import java.util.List;
import java.util.logging.Logger;

public class TableDefinitionSer implements ObjectSerializer<TableDefinitionAsStoreStructDefinition> {
    public static Logger LOG = Logger.getLogger(TableDefinitionSer.class.getName());
    @Override
    public TableDefinitionAsStoreStructDefinition read(StoreInputStream dis) {
        NSqlTableDefinition m = new NSqlTableDefinition();
        m.setCatalogName(dis.readNullableString());
        m.setSchemaName(dis.readNullableString());
        m.setTableName(dis.readNullableString());
        m.setTableType(dis.readNullableString());
        m.setRefGeneration(dis.readNullableString());
        m.setSelfReferencingColName(dis.readNullableString());
        m.setColumns(dis.readNonNullableStruct(NSqlColumn[].class));
        return new TableDefinitionAsStoreStructDefinition(m);
    }

    @Override
    public void write(TableDefinitionAsStoreStructDefinition value, StoreOutputStream dos) {
        NSqlTableDefinition td = value.getTableDefinition();
        dos.writeNullableString(td.getCatalogName());
        dos.writeNullableString(td.getSchemaName());
        dos.writeNullableString(td.getTableName());
        dos.writeNullableString(td.getTableType());
        dos.writeNullableString(td.getRefGeneration());
        dos.writeNullableString(td.getSelfReferencingColName());
        List<NSqlColumn> columns = td.getColumns();
        dos.writeNonNullableStruct(NSqlColumn[].class, columns.toArray(new NSqlColumn[0]));
    }
}
