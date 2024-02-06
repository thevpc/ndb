package net.thevpc.dbinfo.store.ser1;

import net.thevpc.dbinfo.model.TableDefinition;
import net.thevpc.vio2.api.StoreInputStream;
import net.thevpc.vio2.api.StoreOutputStream;
import net.thevpc.vio2.api.ObjectSerializer;
import net.thevpc.dbinfo.model.ColumnDefinition;

import java.util.List;
import java.util.logging.Logger;

public class TableDefinitionSer implements ObjectSerializer<TableDefinition> {
    public static Logger LOG = Logger.getLogger(TableDefinitionSer.class.getName());
    @Override
    public TableDefinition read(StoreInputStream dis) {
        TableDefinition m = new TableDefinition();
        m.setCatalogName(dis.readNullableString());
        m.setSchemaName(dis.readNullableString());
        m.setTableName(dis.readNullableString());
        m.setTableType(dis.readNullableString());
        m.setRefGeneration(dis.readNullableString());
        m.setSelfReferencingColName(dis.readNullableString());
        m.setColumns(dis.readNonNullableStruct(ColumnDefinition[].class));
        return m;
    }

    @Override
    public void write(TableDefinition value, StoreOutputStream dos) {
        dos.writeNullableString(value.getCatalogName());
        dos.writeNullableString(value.getSchemaName());
        dos.writeNullableString(value.getTableName());
        dos.writeNullableString(value.getTableType());
        dos.writeNullableString(value.getRefGeneration());
        dos.writeNullableString(value.getSelfReferencingColName());
        List<ColumnDefinition> columns = value.getColumns();
        dos.writeNonNullableStruct(ColumnDefinition[].class, columns.toArray(new ColumnDefinition[0]));
    }
}
