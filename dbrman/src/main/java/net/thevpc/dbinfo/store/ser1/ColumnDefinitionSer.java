package net.thevpc.dbinfo.store.ser1;

import net.thevpc.vio2.api.StoreInputStream;
import net.thevpc.vio2.api.StoreOutputStream;
import net.thevpc.vio2.api.ObjectSerializer;
import net.thevpc.dbinfo.model.ColumnDefinition;
import net.thevpc.vio2.model.StoreDataType;
import net.thevpc.vio2.model.YesNo;

import java.util.logging.Logger;

public class ColumnDefinitionSer implements ObjectSerializer<ColumnDefinition> {
    public static Logger LOG = Logger.getLogger(ColumnDefinitionSer.class.getName());
    @Override
    public ColumnDefinition read(StoreInputStream dis) {
        ColumnDefinition c = new ColumnDefinition();
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
    public void write(ColumnDefinition columnDef, StoreOutputStream dos) {
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
