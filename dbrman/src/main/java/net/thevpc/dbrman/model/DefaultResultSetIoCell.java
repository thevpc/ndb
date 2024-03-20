package net.thevpc.dbrman.model;

import net.thevpc.dbrman.util.DbInfoModuleInstaller;
import net.thevpc.dbrman.util.UncheckedSQLException;
import net.thevpc.vio2.api.IoCell;
import net.thevpc.vio2.impl.AbstractIoCell;
import net.thevpc.vio2.impl.RepeatableReadIoCell;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class DefaultResultSetIoCell extends AbstractIoCell {
    static {
        DbInfoModuleInstaller.init();
    }
    private final ColumnDefinition column;
    private final ResultSet rs;

    public DefaultResultSetIoCell(ColumnDefinition column, ResultSet rs) {
        this.column = column;
        this.rs = rs;
    }

    @Override
    public ColumnDefinition getDefinition() {
        return column;
    }

    @Override
    public boolean isLob() {
        try {
            switch (column.getStoreType()) {
                case STRING:
                case NSTRING:
                case BIG_INT:
                case NBIG_INT:
                case BIG_DECIMAL:
                case NBIG_DECIMAL:
                case DATE:
                case NDATE:
                case TIME:
                case NTIME:
                case TIMESTAMP:
                case NTIMESTAMP:
                case DOUBLE:
                case NDOUBLE:
                case FLOAT:
                case NFLOAT:
                case LONG:
                case NLONG:
                case BYTE:
                case NBYTE:
                case INT:
                case NINT:
                case SHORT:
                case NSHORT:
                case BOOLEAN:
                case NBOOLEAN: {
                    return false;
                }
                case BYTES:
                case NBYTES:
                case CHAR_STREAM:
                case NCHAR_STREAM:
                case BYTE_STREAM:
                case NBYTE_STREAM: {
                    return true;
                }
                case JAVA_OBJECT:
                case NJAVA_OBJECT:
                {
                    switch (column.getSqlType()) {
                        case Types.OTHER:
                        case Types.JAVA_OBJECT:
                        case Types.STRUCT: {
                            return false;
                        }
                        case Types.ARRAY: {
                            Array a = rs.getArray(column.getIndex());
                            if (a == null) {
                                return false;
                            }
                            return false;
                        }
                        default:{
                            return false;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            //
        }
        return false;
    }

    @Override
    public Object getObject() {
        try {
            switch (column.getStoreType()) {
                case STRING:
                case NSTRING:
                {
                    return rs.getString(column.getIndex());
                }
                case BIG_INT:
                case NBIG_INT:
                {
                    BigDecimal bigDecimal = rs.getBigDecimal(column.getIndex());
                    return bigDecimal == null ? null : bigDecimal.toBigInteger();
                }
                case BIG_DECIMAL:
                case NBIG_DECIMAL:
                {
                    return rs.getBigDecimal(column.getIndex());
                }
                case DATE:
                case NDATE:
                {
                    return rs.getDate(column.getIndex());
                }
                case TIME:
                case NTIME:
                {
                    return rs.getTime(column.getIndex());
                }
                case TIMESTAMP:
                case NTIMESTAMP:
                {
                    return rs.getTimestamp(column.getIndex());
                }
                case DOUBLE:
                case NDOUBLE:
                {
                    double d = rs.getDouble(column.getIndex());
                    return rs.wasNull() ? null : d;
                }
                case FLOAT:
                case NFLOAT:
                {
                    float d = rs.getFloat(column.getIndex());
                    return rs.wasNull() ? null : d;
                }
                case LONG:
                case NLONG:
                {
                    long d = rs.getLong(column.getIndex());
                    return rs.wasNull() ? null : d;
                }
                case BYTE:
                case NBYTE:
                {
                    byte d = rs.getByte(column.getIndex());
                    return rs.wasNull() ? null : d;
                }
                case INT:
                case NINT:
                {
                    int d = rs.getInt(column.getIndex());
                    return rs.wasNull() ? null : d;
                }
                case SHORT:
                case NSHORT:
                {
                    short d = rs.getShort(column.getIndex());
                    return rs.wasNull() ? null : d;
                }
                case BOOLEAN:
                case NBOOLEAN:
                {
                    boolean d = rs.getBoolean(column.getIndex());
                    return rs.wasNull() ? null : d;
                }
                case BYTES:
                case NBYTES:
                {
                    byte[] d = rs.getBytes(column.getIndex());
                    return d;
                }
                case BYTE_STREAM:
                case NBYTE_STREAM:{
                    return rs.getBinaryStream(column.getIndex());
                }
                case CHAR_STREAM:
                case NCHAR_STREAM:
                {
                    return rs.getCharacterStream(column.getIndex());
                }
                case JAVA_OBJECT:
                case NJAVA_OBJECT:
                {
                    switch (column.getSqlType()) {
                        case Types.OTHER:
                        case Types.JAVA_OBJECT:
                        case Types.STRUCT: {
                            return rs.getObject(column.getIndex());
                        }
                        case Types.ARRAY: {
                            Array a = rs.getArray(column.getIndex());
                            if (a == null) {
                                return null;
                            }
                            return a.getArray();
                        }
                        default:{
                            throw new IllegalArgumentException("unsupported");
                        }
                    }
                }
//                case DOCUMENT:
//                case NDOCUMENT:{
//
//                }

            }
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }

        throw new RuntimeException("unsupported " + column.getStoreType());
    }

}
