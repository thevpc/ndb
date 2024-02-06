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
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public interface StoreInputStream extends Closeable {
    <T> T readNullableStruct(Class<T> clazz);

    byte readNonNullableByte();

    <T> T readNonNullableStruct(Class<T> clazz);

    Object readNullableObject();

    StoreDocument readNullableStoreDocument();

    StoreDocumentEntry readNonNullableStoreEntry();

    StoreDocument readNonNullableStoreDocument();

    Object readNonNullableObject();

    String readNullableString();

    String readNonNullableString();

    void readFully(byte[] cc);

    int readNonNullableInt();

    short readNonNullableShort();

    boolean readNonNullableBoolean();

    long readNonNullableLong();

    char readChar();

    float readNonNullableFloat();

    double readNonNullableDouble();

    String readUTF();

    Boolean readNullableBoolean();

    Byte readNullableByte();

    Short readNullableShort();

    Integer readNullableInt();

    Long readNullableLong();

    Float readNullableFloat();

    Double readNullableDouble();

    BigInteger readNullableBigInt();

    BigInteger readNonNullableBigInt();

    BigDecimal readNullableBigDecimal();

    BigDecimal readNonNullableBigDecimal();

    Object readNull();

    Date readNonNullableSqlDate();

    Date readNullableSqlDate();

    Time readNonNullableSqlTime();

    Time readNullableSqlTime();

    InputStream readNonNullableInputStream();

    Reader readNullableReader();

    Reader readNonNullableReader();

    InputStream readNullableInputStream();

    byte[] readNonNullableByteArray();

    byte[] readNullableByteArray();

    StoreValue readAny();

    StoreValue readAnyExpected(StoreDataType fType);

    StoreValue readAny(StoreDataType storeDataType);

    Timestamp readNonNullableSqlTimestamp();

    Timestamp readNullableSqlTimestamp();

    void close();

    int readAvailable(byte[] buffer);

    int readAvailable(byte[] buffer, int offset, int len);

    int read(byte[] buffer, int offset, int len);

    int read(byte[] buffer);
}
