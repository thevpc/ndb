package net.thevpc.diet.io.v1.s;

import net.thevpc.diet.io.StoreInputStream;
import net.thevpc.diet.io.StoreOutputStream;
import net.thevpc.diet.io.ObjectSerializer;
import net.thevpc.diet.model.StoreColumnDefinition;
import net.thevpc.diet.model.StoreTableDefinition;

import java.util.logging.Logger;

public class StoreTableDefinitionSer implements ObjectSerializer<StoreTableDefinition> {
    public static Logger LOG = Logger.getLogger(StoreTableDefinitionSer.class.getName());
    @Override
    public StoreTableDefinition read(StoreInputStream dis) {
        StoreTableDefinition m = new StoreTableDefinition();
        m.setCatalogName(dis.readNullableString());
        m.setSchemaName(dis.readNullableString());
        m.setTableName(dis.readNullableString());
        m.setTableType(dis.readNullableString());
        m.setRefGeneration(dis.readNullableString());
        m.setSelfReferencingColName(dis.readNullableString());
        m.setColumns(dis.readNonNullableStruct(StoreColumnDefinition[].class));
        return m;
    }

    @Override
    public void write(StoreTableDefinition value, StoreOutputStream dos) {
        dos.writeNullableString(value.getCatalogName());
        dos.writeNullableString(value.getSchemaName());
        dos.writeNullableString(value.getTableName());
        dos.writeNullableString(value.getTableType());
        dos.writeNullableString(value.getRefGeneration());
        dos.writeNullableString(value.getSelfReferencingColName());
        StoreColumnDefinition[] columns = value.getColumns();
        dos.writeNonNullableStruct(StoreColumnDefinition[].class, columns);
    }
}
