package net.thevpc.vio2.api;

import net.thevpc.vio2.model.StoreDataType;
import net.thevpc.vio2.model.StoreDocument;
import net.thevpc.vio2.model.StoreDocumentEntry;
import net.thevpc.vio2.model.StoreValue;

import java.io.Closeable;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;

public interface StoreOutputStream extends Closeable {
    int WRITE_BUFFER_SIZE = 1024 * 10;

    void writeNullableDocument(StoreDocument value);

    void writeNonNullableDocumentEntry(StoreDocumentEntry value);

    void writeNonNullableDocument(StoreDocument value);

    <T> void writeNonNullableStruct(Class<T> clazz, T value);

    void writeNonNullableCharArray(char[] str);

    void writeNullableCharArray(char[] str);

    void writeNonNullChars(String s);

    void writeNonNullableByte(int i);

    void writeNonNullableShort(short i);

    void writeNonNullableInt(int i);

    void writeUTF(String i);

    <T> void writeNullableStruct(Class<T> clazz, T value);

    void writeNullPrefix();

    void writeNonNullPrefix();

    void writeNonNullableString(String str);

    void writeNullableString(String str);

    long getUTFLength(String s);

    void writeNull();

    void writeNullableBoolean(Boolean v);

    void writeStoreValue(StoreValue v);

    void writeAny(StoreDataType storeDataType, Object v);

    void writeNonNullableBoolean(boolean v);

    void writeNullableCharacter(Character v);

    void writeNullableShort(Short v);

    void writeNullableLong(Long v);

    void writeNonNullableLong(long v);

    void writeNonNullableFloat(float v);

    void writeNonNullableDouble(double v);

    void writeNullableFloat(Float v);

    void writeNullableDouble(Double v);

    void writeNullableInt(Integer v);

    void writeNullableByte(Byte v);

    void writeNullableBigInt(BigInteger v);

    void writeNonNullableBigInt(BigInteger v);

    void writeNullableBigDecimal(BigDecimal v);

    void writeNonNullableBigDecimal(BigDecimal v);

    void writeNullableSqlDate(java.sql.Date v);

    void writeNonNullableSqlDate(java.sql.Date v);

    void writeNullableSqlTime(java.sql.Time v);

    void writeNonNullableSqlTime(java.sql.Time v);

    void writeNonNullableObject(Object v);

    void writeNullableObject(Object v);

    void writeNonNullableReader(Reader v);

    void writeNullableReader(Reader v);

    void writeRawBytes(byte[] b);

    void writeRawBytes(byte b[], int off, int len);

    void writeNonNullableInputStream(InputStream v);

    void writeNullableInputStream(InputStream v);

    void writeNonNullableBytes(byte[] v);

    void writeNullableBytes(byte[] v);

    void writeNullableSqlTimestamp(Timestamp v);

    void writeNonNullableSqlTimestamp(Timestamp v);

    void close();

    void flush();
}
