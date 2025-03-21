package net.thevpc.nsql.dump.model;

import net.thevpc.nsql.dump.common.SqlColumnAsStoreFieldDefinition;
import net.thevpc.nsql.dump.util.NSqlDumpModuleInstaller;
import net.thevpc.nsql.dump.util.SqlColumnTypeToStoreUtils;
import net.thevpc.nsql.UncheckedSqlException;
import net.thevpc.nsql.NSqlColumn;
import net.thevpc.nuts.io.NullInputStream;
import net.thevpc.lib.nserializer.impl.AbstractIoCell;
import net.thevpc.lib.nserializer.model.StoreDataType;
import net.thevpc.lib.nserializer.model.StoreFieldDefinition;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

public class DefaultResultSetIoCell extends AbstractIoCell {
    static {
        NSqlDumpModuleInstaller.init();
    }

    private final NSqlColumn column;
    private final ResultSet rs;
    private final StoreDataType fileColType;
    private final StoreFieldDefinition storeFieldDefinition;

    public DefaultResultSetIoCell(NSqlColumn column, ResultSet rs) {
        this.column = column;
        this.rs = rs;
        fileColType = SqlColumnTypeToStoreUtils.toStoreDataType(column);
        storeFieldDefinition = new SqlColumnAsStoreFieldDefinition(fileColType, column);
    }

    @Override
    public StoreFieldDefinition getDefinition() {
        return storeFieldDefinition;
    }

    @Override
    public boolean isLob() {
        try {
            switch (fileColType.base()) {
                case STRING:
                case BIG_INT:
                case BIG_DECIMAL:
                case DATE:
                case TIME:
                case TIMESTAMP:
                case DOUBLE:
                case FLOAT:
                case LONG:
                case BYTE:
                case INT:
                case SHORT:
                case BOOLEAN: {
                    return false;
                }
                case BYTES:
                case CHAR_STREAM:
                case BYTE_STREAM: {
                    return true;
                }
                case JAVA_OBJECT: {
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
                        default: {
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
            switch (fileColType.base()) {
                case STRING: {
                    String v = rs.getString(column.getIndex());
                    return (v == null && !fileColType.isNullable()) ? "" : v;
                }
                case BIG_INT: {
                    BigDecimal v = rs.getBigDecimal(column.getIndex());
                    return (v == null && !fileColType.isNullable()) ? BigInteger.ZERO : v.toBigInteger();
                }
                case BIG_DECIMAL: {
                    BigDecimal v = rs.getBigDecimal(column.getIndex());
                    return (v == null && !fileColType.isNullable()) ? BigDecimal.ZERO : v;
                }
                case DATE: {
                    return rs.getDate(column.getIndex());
                }
                case TIME: {
                    return rs.getTime(column.getIndex());
                }
                case TIMESTAMP: {
                    return rs.getTimestamp(column.getIndex());
                }
                case DOUBLE: {
                    double d = rs.getDouble(column.getIndex());
                    return rs.wasNull() && fileColType.isNullable() ? null : d;
                }
                case FLOAT: {
                    float d = rs.getFloat(column.getIndex());
                    return rs.wasNull() && fileColType.isNullable()? null : d;
                }
                case LONG: {
                    long d = rs.getLong(column.getIndex());
                    return rs.wasNull() && fileColType.isNullable()? null : d;
                }
                case BYTE: {
                    byte d = rs.getByte(column.getIndex());
                    return rs.wasNull() && fileColType.isNullable()? null : d;
                }
                case INT: {
                    int d = rs.getInt(column.getIndex());
                    return rs.wasNull() && fileColType.isNullable()? null : d;
                }
                case SHORT: {
                    short d = rs.getShort(column.getIndex());
                    return rs.wasNull() && fileColType.isNullable()? null : d;
                }
                case BOOLEAN: {
                    boolean d = rs.getBoolean(column.getIndex());
                    return rs.wasNull() && fileColType.isNullable()? null : d;
                }
                case BYTES: {
                    byte[] v = rs.getBytes(column.getIndex());
                    return (v == null && !fileColType.isNullable()) ? new byte[0] : v;
                }
                case BYTE_STREAM: {
                    InputStream v = rs.getBinaryStream(column.getIndex());
                    return (v == null && !fileColType.isNullable()) ? NullInputStream.INSTANCE : v;
                }
                case CHAR_STREAM: {
                    Reader r = rs.getCharacterStream(column.getIndex());
                    return r;
                }
                case JAVA_OBJECT: {
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
                        default: {
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
            throw new UncheckedSqlException(e);
        }

        throw new RuntimeException("unsupported " + fileColType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultResultSetIoCell that = (DefaultResultSetIoCell) o;
        return Objects.equals(column, that.column) && Objects.equals(rs, that.rs) && fileColType == that.fileColType && Objects.equals(storeFieldDefinition, that.storeFieldDefinition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(column, rs, fileColType, storeFieldDefinition);
    }
}
