package net.thevpc.nsql.dump.util;

import net.thevpc.nsql.NSqlColumn;
import net.thevpc.nsql.NSqlColumnType;
import net.thevpc.nsql.model.YesNo;
import net.thevpc.lib.nserializer.model.StoreDataType;
import net.thevpc.lib.nserializer.model.StoreDataTypeBase;

public class SqlColumnTypeToStoreUtils {

    public static StoreDataType toStoreDataType(NSqlColumn c) {
        NSqlColumnType columnType = c.getColumnType();
        boolean nullable = c.getNullable() != YesNo.NO;
        switch (columnType) {
            case BIGDECIMAL: {
                return StoreDataType.of(StoreDataTypeBase.BIG_DECIMAL, nullable);
            }
            case INT: {
                return StoreDataType.of(StoreDataTypeBase.INT, nullable);
            }
            case DATE: {
                return StoreDataType.of(StoreDataTypeBase.DATE, nullable);
            }
            case DOUBLE: {
                return StoreDataType.of(StoreDataTypeBase.DOUBLE, nullable);
            }
            case JAVA_OBJECT: {
                return StoreDataType.of(StoreDataTypeBase.JAVA_OBJECT, nullable);
            }
            case LONG: {
                return StoreDataType.of(StoreDataTypeBase.LONG, nullable);
            }
            case FLOAT: {
                return StoreDataType.of(StoreDataTypeBase.FLOAT, nullable);
            }
            case BIGINT: {
                return StoreDataType.of(StoreDataTypeBase.BIG_INT, nullable);
            }
            case TIME: {
                return StoreDataType.of(StoreDataTypeBase.TIME, nullable);
            }
            case CLOB: {
                return StoreDataType.of(StoreDataTypeBase.CHAR_STREAM, nullable);
            }
            case BLOB: {
                return StoreDataType.of(StoreDataTypeBase.BYTE_STREAM, nullable);
            }
            case STRING: {
                return StoreDataType.of(StoreDataTypeBase.STRING, nullable);
            }
            case DECIMAL: {
                return StoreDataType.of(StoreDataTypeBase.BIG_DECIMAL, nullable);
            }
            case TIMESTAMP: {
                return StoreDataType.of(StoreDataTypeBase.TIMESTAMP, nullable);
            }
            case BOOLEAN: {
                return StoreDataType.of(StoreDataTypeBase.BOOLEAN, nullable);
            }
            case SHORT: {
                return StoreDataType.of(StoreDataTypeBase.SHORT, nullable);
            }
            case BYTE: {
                return StoreDataType.of(StoreDataTypeBase.BYTE, nullable);
            }
            case NULL: {
                return StoreDataType.NULL;
            }
        }
        throw new IllegalArgumentException("unsupported " + c);
    }

    public static NSqlColumnType toColumnType(StoreDataType c) {
        switch (c.base()) {
            case BIG_DECIMAL:
                return NSqlColumnType.BIGDECIMAL;
            case BIG_INT:
                return NSqlColumnType.BIGINT;
            case INT:
                return NSqlColumnType.INT;
            case BOOLEAN:
                return NSqlColumnType.BOOLEAN;
            case BYTE:
                return NSqlColumnType.BYTE;
            case SHORT:
                return NSqlColumnType.SHORT;
            case LONG:
                return NSqlColumnType.LONG;
            case FLOAT:
                return NSqlColumnType.FLOAT;
            case DATE:
                return NSqlColumnType.DATE;
            case TIME:
                return NSqlColumnType.TIME;
            case TIMESTAMP:
                return NSqlColumnType.TIMESTAMP;
            case STRING:
                return NSqlColumnType.STRING;
            case BYTE_STREAM:
                return NSqlColumnType.BLOB;
            case CHAR_STREAM:
                return NSqlColumnType.CLOB;
            case BYTES:
                return NSqlColumnType.BLOB;
            case JAVA_OBJECT:
                return NSqlColumnType.JAVA_OBJECT;
            case DOUBLE:
                return NSqlColumnType.DOUBLE;
            case DOCUMENT:
                return NSqlColumnType.JAVA_OBJECT;
            case NULL:
                return NSqlColumnType.NULL;
        }
        throw new IllegalArgumentException("unsupported " + c);
    }
}
