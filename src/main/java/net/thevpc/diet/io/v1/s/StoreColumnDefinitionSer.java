package net.thevpc.diet.io.v1.s;

import net.thevpc.diet.io.StoreInputStream;
import net.thevpc.diet.io.StoreOutputStream;
import net.thevpc.diet.io.ObjectSerializer;
import net.thevpc.diet.model.StoreColumnDefinition;
import net.thevpc.diet.model.StoreDataType;
import net.thevpc.diet.model.YesNo;

import java.util.logging.Logger;

public class StoreColumnDefinitionSer implements ObjectSerializer<StoreColumnDefinition> {
    public static Logger LOG = Logger.getLogger(StoreColumnDefinitionSer.class.getName());
    @Override
    public StoreColumnDefinition read(StoreInputStream dis) {
        StoreColumnDefinition c = new StoreColumnDefinition();
        c.setIndex(dis.readNonNullableInt());
        c.setColumnName(dis.readNullableString());
        c.setLabel(dis.readNullableString());
        c.setStoreType(StoreDataType.ofId(dis.readNonNullableInt()).get());
        c.setSqlType(dis.readNonNullableInt());
        c.setSqlTypeCode(dis.readNullableString());
        c.setSqlTypeName(dis.readNullableString());
        c.setDisplaySize(dis.readNonNullableInt());
        c.setPrecision(dis.readNonNullableInt());
        c.setScale(dis.readNonNullableInt());
        c.setJavaClassName(dis.readNullableString());
        c.setRadix(dis.readNonNullableInt());
        c.setNullable(YesNo.ofId(dis.readNonNullableInt()).get());
        c.setColumnDef(dis.readNullableString());
        c.setSchemaName(dis.readNullableString());
        c.setCatalogName(dis.readNullableString());
        c.setTableName(dis.readNullableString());
        c.setSourceDataType(dis.readNonNullableInt());
        c.setSourceDataTypeCode(dis.readNullableString());
        c.setAutoIncrement(YesNo.ofId(dis.readNonNullableInt()).get());
        c.setGeneratedColumns(YesNo.ofId(dis.readNonNullableInt()).get());
        c.setPk(dis.readNonNullableBoolean());
        c.setPkIndex(dis.readNonNullableInt());
        c.setFk(dis.readNonNullableBoolean());
        return c;
    }

    @Override
    public void write(StoreColumnDefinition columnDef, StoreOutputStream dos) {
        dos.writeNonNullableInt(columnDef.getIndex());
        dos.writeNullableString(columnDef.getColumnName());
        dos.writeNullableString(columnDef.getLabel());
        dos.writeNonNullableInt(columnDef.getStoreType().id());
        dos.writeNonNullableInt(columnDef.getSqlType());
        dos.writeNullableString(columnDef.getSqlTypeCode());
        dos.writeNullableString(columnDef.getSqlTypeName());
        dos.writeNonNullableInt(columnDef.getDisplaySize());
        dos.writeNonNullableInt(columnDef.getPrecision());
        dos.writeNonNullableInt(columnDef.getScale());
        dos.writeNullableString(columnDef.getJavaClassName());
        dos.writeNonNullableInt(columnDef.getRadix());
        dos.writeNonNullableInt(columnDef.getNullable().id());
        dos.writeNullableString(columnDef.getColumnDef());
        dos.writeNullableString(columnDef.getSchemaName());
        dos.writeNullableString(columnDef.getCatalogName());
        dos.writeNullableString(columnDef.getTableName());
        dos.writeNonNullableInt(columnDef.getSourceDataType());
        dos.writeNullableString(columnDef.getSourceDataTypeCode());
        dos.writeNonNullableInt(columnDef.getAutoIncrement().id());
        dos.writeNonNullableInt(columnDef.getGeneratedColumns().id());
        dos.writeNonNullableBoolean(columnDef.isPk());
        dos.writeNonNullableInt(columnDef.getPkIndex());
        dos.writeNonNullableBoolean(columnDef.isFk());
    }
}
