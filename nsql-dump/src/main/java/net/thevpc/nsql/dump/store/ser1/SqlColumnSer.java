package net.thevpc.nsql.dump.store.ser1;

import net.thevpc.nsql.dump.util.SqlColumnTypeToStoreUtils;
import net.thevpc.nsql.NSqlColumn;
import net.thevpc.lib.nserializer.api.StoreInputStream;
import net.thevpc.lib.nserializer.api.StoreOutputStream;
import net.thevpc.lib.nserializer.api.ObjectSerializer;
import net.thevpc.lib.nserializer.model.StoreDataType;
import net.thevpc.nsql.model.YesNo;

import java.util.logging.Logger;

public class SqlColumnSer implements ObjectSerializer<NSqlColumn> {
    public static Logger LOG = Logger.getLogger(SqlColumnSer.class.getName());
    @Override
    public NSqlColumn read(StoreInputStream dis) {
        NSqlColumn c = new NSqlColumn();
        c.setIndex(dis.readNonNullableInt());
        c.setColumnName(dis.readNullableString());
        c.setLabel(dis.readNullableString());
        StoreDataType storeDataType = StoreDataType.ofId(dis.readNonNullableInt()).get();
        c.setColumnType(SqlColumnTypeToStoreUtils.toColumnType(storeDataType));
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
        c.setGeneratedColumn(YesNo.ofId(dis.readNonNullableInt()).get());
        c.setPk(dis.readNonNullableBoolean());
        c.setPkIndex(dis.readNonNullableInt());
        c.setFk(dis.readNonNullableBoolean());
        return c;
    }

    @Override
    public void write(NSqlColumn c, StoreOutputStream dos) {
        dos.writeNonNullableInt(c.getIndex());
        dos.writeNullableString(c.getColumnName());
        dos.writeNullableString(c.getLabel());
        dos.writeNonNullableInt(SqlColumnTypeToStoreUtils.toStoreDataType(c).id());
        dos.writeNonNullableInt(c.getSqlType());
        dos.writeNullableString(c.getSqlTypeCode());
        dos.writeNullableString(c.getSqlTypeName());
        dos.writeNonNullableInt(c.getDisplaySize());
        dos.writeNonNullableInt(c.getPrecision());
        dos.writeNonNullableInt(c.getScale());
        dos.writeNullableString(c.getJavaClassName());
        dos.writeNonNullableInt(c.getRadix());
        dos.writeNonNullableInt(c.getNullable().id());
        dos.writeNullableString(c.getColumnDef());
        dos.writeNullableString(c.getSchemaName());
        dos.writeNullableString(c.getCatalogName());
        dos.writeNullableString(c.getTableName());
        dos.writeNonNullableInt(c.getSourceDataType());
        dos.writeNullableString(c.getSourceDataTypeCode());
        dos.writeNonNullableInt(c.getAutoIncrement().id());
        dos.writeNonNullableInt(c.getGeneratedColumn().id());
        dos.writeNonNullableBoolean(c.isPk());
        dos.writeNonNullableInt(c.getPkIndex());
        dos.writeNonNullableBoolean(c.isFk());
    }
}
