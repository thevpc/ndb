package net.thevpc.vio2.model;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Optional;

public class DefaultStoreValue implements StoreValue {
    private StoreDataType type;
    private Object value;

    public static StoreValue ofNull() {
        return new DefaultStoreValue(StoreDataType.NULL, null);
    }

    public static StoreValue ofDocument(StoreDocument value) {
        if(value==null){
            return ofNull();
        }
        return new DefaultStoreValue(StoreDataType.DOCUMENT, value);
    }

    public static StoreValue ofJavaObject(Object value) {
        if(value==null){
            return ofNull();
        }
        return new DefaultStoreValue(StoreDataType.JAVA_OBJECT, value);
    }

    public static StoreValue ofInputStream(InputStream value) {
        if(value==null){
            return ofNull();
        }
        return new DefaultStoreValue(StoreDataType.BYTE_STREAM, value);
    }

    public static StoreValue ofBytes(byte[] value) {
        if(value==null){
            return ofNull();
        }
        return new DefaultStoreValue(StoreDataType.BYTES, value);
    }

    public static StoreValue ofReader(Reader value) {
        if(value==null){
            return ofNull();
        }
        return new DefaultStoreValue(StoreDataType.CHAR_STREAM, value);
    }

    public static StoreValue ofString(String value) {
        if(value==null){
            return ofNull();
        }
        return new DefaultStoreValue(StoreDataType.STRING, value);
    }

    public static StoreValue ofBigInt(BigInteger value) {
        if(value==null){
            return ofNull();
        }
        return new DefaultStoreValue(StoreDataType.BIG_INT, value);
    }

    public static StoreValue ofBigDecimal(BigDecimal value) {
        if(value==null){
            return ofNull();
        }
        return new DefaultStoreValue(StoreDataType.BIG_DECIMAL, value);
    }

    public static StoreValue ofDouble(Double value) {
        if(value==null){
            return ofNull();
        }
        return new DefaultStoreValue(StoreDataType.DOUBLE, value);
    }

    public static StoreValue ofSqlTimestamp(Timestamp value) {
        if(value==null){
            return ofNull();
        }
        return new DefaultStoreValue(StoreDataType.TIMESTAMP, value);
    }

    public static StoreValue ofSqlDate(java.sql.Date value) {
        if(value==null){
            return ofNull();
        }
        return new DefaultStoreValue(StoreDataType.DATE, value);
    }

    public static StoreValue ofSqlTime(java.sql.Time value) {
        if(value==null){
            return ofNull();
        }
        return new DefaultStoreValue(StoreDataType.TIME, value);
    }

    public static StoreValue ofFloat(Float value) {
        if(value==null){
            return ofNull();
        }
        return new DefaultStoreValue(StoreDataType.FLOAT, value);
    }

    public static StoreValue ofLong(Long value) {
        if(value==null){
            return ofNull();
        }
        return new DefaultStoreValue(StoreDataType.LONG, value);
    }

    public static StoreValue ofByte(Byte value) {
        if(value==null){
            return ofNull();
        }
        return new DefaultStoreValue(StoreDataType.BYTE, value);
    }

    public static StoreValue ofInt(Integer value) {
        if(value==null){
            return ofNull();
        }
        return new DefaultStoreValue(StoreDataType.INT, value);
    }

    public static StoreValue ofBoolean(Boolean value) {
        if(value==null){
            return ofNull();
        }
        return new DefaultStoreValue(StoreDataType.BOOLEAN, value);
    }

    public static StoreValue ofShort(Short value) {
        if(value==null){
            return ofNull();
        }
        return new DefaultStoreValue(StoreDataType.SHORT, value);
    }

    public DefaultStoreValue(StoreDataType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public static StoreValue ofAny(StoreDataType fType, Object object) {
        if(object==null){
            return ofNull();
        }
        switch (fType) {
            case BOOLEAN:
            case NBOOLEAN:
                return ofBoolean((Boolean) object);
            case BYTE:
            case NBYTE:
                return ofByte((Byte) object);
            case SHORT:
            case NSHORT:
                return ofShort((Short) object);
            case INT:
            case NINT:
                return ofInt((Integer) object);
            case LONG:
            case NLONG:
                return ofLong((Long) object);
            case TIME:
            case NTIME:
                return ofSqlTime((Time) object);
            case DATE:
            case NDATE:
                return ofSqlDate((java.sql.Date) object);
            case TIMESTAMP:
            case NTIMESTAMP:
                return ofSqlTimestamp((java.sql.Timestamp) object);
            case JAVA_OBJECT:
            case NJAVA_OBJECT:
                return ofJavaObject(object);
            case DOUBLE:
            case NDOUBLE:
                return ofDouble((Double) object);
            case FLOAT:
            case NFLOAT:
                return ofFloat((Float) object);
            case STRING:
            case NSTRING:
                return ofString((String) object);
            case BYTE_STREAM:
            case NBYTE_STREAM:
                return ofInputStream((InputStream) object);
            case CHAR_STREAM:
            case NCHAR_STREAM:
                return ofReader((Reader) object);
            case DOCUMENT:
            case NDOCUMENT:
                return ofDocument((StoreDocument) object);
            case BYTES:
            case NBYTES:
                return ofBytes((byte[]) object);
            case BIG_DECIMAL:
            case NBIG_DECIMAL:
                return ofBigDecimal((BigDecimal) object);
            case BIG_INT:
            case NBIG_INT:
                return ofBigInt((BigInteger) object);
            case NULL:
                return ofNull();
        }
        throw new IllegalArgumentException("unsupported " + fType);
    }

    @Override
    public StoreDataType getType() {
        return type;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Optional<Integer> getInt() {
        if (value instanceof Number) {
            return Optional.of(((Number) value).intValue());
        }
        if (value instanceof String) {
            try {
                return Optional.of(Integer.parseInt((String) value));
            } catch (Exception ex) {
                //just ignore
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Short> getShort() {
        if (value instanceof Number) {
            return Optional.of(((Number) value).shortValue());
        }
        if (value instanceof String) {
            try {
                return Optional.of(Short.parseShort((String) value));
            } catch (Exception ex) {
                //just ignore
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> getBoolean() {
        if (value instanceof Boolean) {
            return Optional.of(((Boolean) value));
        }
        if (value instanceof String) {
            switch (((String) value).trim().toLowerCase()) {
                case "false":
                case "no": {
                    return Optional.of(false);
                }
                case "true":
                case "yes": {
                    return Optional.of(true);
                }
            }
        }
        return Optional.empty();
    }


    @Override
    public Optional<Long> getLong() {
        if (value instanceof Number) {
            return Optional.of(((Number) value).longValue());
        }
        if (value instanceof String) {
            try {
                return Optional.of(Long.parseLong((String) value));
            } catch (Exception ex) {
                //just ignore
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getString() {
        if (value instanceof String) {
            return Optional.of(((String) value));
        }
        if (value != null) {
            try {
                return Optional.of(String.valueOf(value));
            } catch (Exception ex) {
                //just ignore
            }
        }
        return Optional.empty();
    }
}
