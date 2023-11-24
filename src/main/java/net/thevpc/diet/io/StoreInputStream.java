/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.diet.io;

import net.thevpc.diet.model.*;

import java.io.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

/**
 * @author vpc
 */
public class StoreInputStream implements Closeable {

    private DataInputStream dis;
    Sers srs;

    public StoreInputStream(InputStream in, Sers srs) {
        dis = new DataInputStream(in);
        this.srs = srs;
    }


    public <T> T readNullableStruct(Class<T> clazz) {
        byte b = readNonNullableByte();
        if (b == 0) {
            return null;
        }
        return srs.getObjectReader(clazz).get().read(this);
    }

    public byte readNonNullableByte() {
        try {
            return dis.readByte();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public <T> T readNonNullableStruct(Class<T> clazz) {
        Optional<ObjectReader<T>> objectReader = srs.getObjectReader(clazz);
        if (!objectReader.isPresent()) {
            if (clazz.isArray()) {
                Class<?> c = clazz.getComponentType();
                int count = readNonNullableInt();
                switch (c.getName()) {
                    case "boolean": {
                        boolean[] all = new boolean[count];
                        for (int i = 0; i < count; i++) {
                            all[i] = readNonNullableBoolean();
                        }
                        return (T) all;
                    }
                    case "byte": {
                        byte[] all = new byte[count];
                        readFully(all);
                        return (T) all;
                    }
                    case "short": {
                        short[] all = new short[count];
                        for (int i = 0; i < count; i++) {
                            all[i] = readNonNullableShort();
                        }
                        return (T) all;
                    }
                    case "int": {
                        int[] all = new int[count];
                        for (int i = 0; i < count; i++) {
                            all[i] = readNonNullableInt();
                        }
                        return (T) all;
                    }
                    case "long": {
                        long[] all = new long[count];
                        for (int i = 0; i < count; i++) {
                            all[i] = readNonNullableLong();
                        }
                        return (T) all;
                    }
                    case "char": {
                        char[] all = new char[count];
                        for (int i = 0; i < count; i++) {
                            all[i] = readChar();
                        }
                        return (T) all;
                    }
                    case "float": {
                        float[] all = new float[count];
                        for (int i = 0; i < count; i++) {
                            all[i] = readNonNullableFloat();
                        }
                        return (T) all;
                    }
                    case "double": {
                        double[] all = new double[count];
                        for (int i = 0; i < count; i++) {
                            all[i] = readNonNullableFloat();
                        }
                        return (T) all;
                    }
                    case "java.lang.String": {
                        String[] all = new String[count];
                        for (int i = 0; i < count; i++) {
                            all[i] = readNullableString();
                        }
                        return (T) all;
                    }
                    case "java.math.BigInteger": {
                        BigInteger[] all = new BigInteger[count];
                        for (int i = 0; i < count; i++) {
                            all[i] = readNullableBigInt();
                        }
                        return (T) all;
                    }
                    case "java.math.BigDecimal": {
                        BigDecimal[] all = new BigDecimal[count];
                        for (int i = 0; i < count; i++) {
                            all[i] = readNullableBigDecimal();
                        }
                        return (T) all;
                    }
                    case "java.sql.Date": {
                        java.sql.Date[] all = new java.sql.Date[count];
                        for (int i = 0; i < count; i++) {
                            all[i] = readNullableSqlDate();
                        }
                        return (T) all;
                    }
                    case "java.sql.Time": {
                        java.sql.Time[] all = new java.sql.Time[count];
                        for (int i = 0; i < count; i++) {
                            all[i] = readNullableSqlTime();
                        }
                        return (T) all;
                    }
                    case "java.sql.Timestamp": {
                        java.sql.Timestamp[] all = new java.sql.Timestamp[count];
                        for (int i = 0; i < count; i++) {
                            all[i] = readNullableSqlTimestamp();
                        }
                        return (T) all;
                    }
                    default: {
                        Object all = Array.newInstance(c, count);
                        for (int i = 0; i < count; i++) {
                            Array.set(all, i, readNullableStruct(c));
                        }
                        return (T) all;
                    }
                }
            } else {
                switch (clazz.getName()) {
                    case "boolean":
                    case "java.lang.Boolean": {
                        return (T) (Object) readNonNullableBoolean();
                    }
                    case "byte":
                    case "java.lang.Byte": {
                        return (T) (Object) readNonNullableByte();
                    }
                    case "short":
                    case "java.lang.Short": {
                        return (T) (Object) readNonNullableShort();
                    }
                    case "int":
                    case "java.lang.Integer": {
                        return (T) (Object) readNonNullableInt();
                    }
                    case "long":
                    case "java.lang.Long": {
                        return (T) (Object) readNonNullableLong();
                    }
                    case "char":
                    case "java.lang.Charater": {
                        return (T) (Object) readChar();
                    }
                    case "float":
                    case "java.lang.Float": {
                        return (T) (Object) readNonNullableFloat();
                    }
                    case "double":
                    case "java.lang.Double": {
                        return (T) (Object) readNonNullableDouble();
                    }
                    case "java.lang.String": {
                        return (T) readNonNullableString();
                    }
                    case "java.math.BigInteger": {
                        return (T) readNonNullableBigInt();
                    }
                    case "java.math.BigDecimal": {
                        return (T) readNonNullableBigDecimal();
                    }
                    case "java.sql.Date": {
                        return (T) readNonNullableSqlDate();
                    }
                    case "java.sql.Time": {
                        return (T) readNonNullableSqlTime();
                    }
                    case "java.sql.Timestamp": {
                        return (T) readNonNullableSqlTimestamp();
                    }
                }
            }
            throw new IllegalArgumentException("not reader for " + clazz);
        }
        return objectReader.get().read(this);
    }

    public Object readNullableObject() {
        byte[] ba = readNullableByteArray();
        if (ba == null) {
            return null;
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(ba);
        try {
            ObjectInputStream ois = new ObjectInputStream(bis);
            return ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public StoreDocument readNullableStoreDocument() {
        byte b = readNonNullableByte();
        switch (b) {
            case 0:
                return null;
            case 1:
                return readNonNullableStoreDocument();
            default: {
                throw new UncheckedIOException(new IOException("invalid string format. unexpected " + b));
            }
        }
    }

    public StoreDocumentEntry readNonNullableStoreEntry() {
        String k = this.readNonNullableString();
        StoreValue v = this.readNonNullableStruct(StoreValue.class);
        return new DefaultStoreDocumentEntry(k, v);
    }

    public StoreDocument readNonNullableStoreDocument() {
        int count = readNonNullableInt();
        DefaultStoreDocument d = new DefaultStoreDocument();
        for (int i = 0; i < count; i++) {
            StoreDocumentEntry e = readNonNullableStoreEntry();
            d.put(e);
        }
        return d;
    }

    public Object readNonNullableObject() {
        byte[] ba = readNonNullableByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(ba);
        try {
            ObjectInputStream ois = new ObjectInputStream(bis);
            return ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String readNullableString() {
        byte b = readNonNullableByte();
        switch (b) {
            case 0:
                return null;
            case 1:
                return readNonNullableString();
            default: {
                throw new UncheckedIOException(new IOException("invalid string format. unexpected " + b));
            }
        }
    }

    public String readNonNullableString() {
        byte b = readNonNullableByte();
        switch (b) {
            case 1:
                return readUTF();
            case 2: {
                int len = readNonNullableInt();
                byte[] cc = new byte[len * 2];
                readFully(cc);
                return new String(cc);
            }
            default: {
                throw new UncheckedIOException(new IOException("invalid string format. unexpected " + b));
            }
        }
    }

    public void readFully(byte[] cc) {
        try {
            dis.readFully(cc);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public int readNonNullableInt() {
        try {
            return dis.readInt();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public short readNonNullableShort() {
        try {
            return dis.readShort();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean readNonNullableBoolean() {
        try {
            return dis.readBoolean();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public long readNonNullableLong() {
        try {
            return dis.readLong();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public char readChar() {
        try {
            return dis.readChar();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public float readNonNullableFloat() {
        try {
            return dis.readFloat();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public double readNonNullableDouble() {
        try {
            return dis.readDouble();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String readUTF() {
        try {
            return dis.readUTF();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Boolean readNullableBoolean() {
        byte v = readNonNullableByte();
        switch (v) {
            case 0:
                return null;
            default:
                return readNonNullableBoolean();
        }
    }

    public Byte readNullableByte() {
        byte v = readNonNullableByte();
        switch (v) {
            case 0:
                return null;
            default:
                return readNonNullableByte();
        }
    }

    public Short readNullableShort() {
        byte v = readNonNullableByte();
        switch (v) {
            case 0:
                return null;
            default:
                return readNonNullableShort();
        }
    }

    public Integer readNullableInt() {
        byte v = readNonNullableByte();
        switch (v) {
            case 0:
                return null;
            default:
                return readNonNullableInt();
        }
    }

    public Long readNullableLong() {
        byte v = readNonNullableByte();
        switch (v) {
            case 0:
                return null;
            default:
                return readNonNullableLong();
        }
    }

    public Float readNullableFloat() {
        byte v = readNonNullableByte();
        switch (v) {
            case 0:
                return null;
            default:
                return readNonNullableFloat();
        }
    }

    public Double readNullableDouble() {
        byte v = readNonNullableByte();
        switch (v) {
            case 0:
                return null;
            default:
                return readNonNullableDouble();
        }
    }

    public BigInteger readNullableBigInt() {
        String s = readNullableString();
        if (s == null) {
            return null;
        }
        return new BigInteger(s);
    }

    public BigInteger readNonNullableBigInt() {
        String s = readNonNullableString();
        if (s == null) {
            return null;
        }
        return new BigInteger(s);
    }

    public BigDecimal readNullableBigDecimal() {
        String s = readNullableString();
        return new BigDecimal(s);
    }

    public BigDecimal readNonNullableBigDecimal() {
        String s = readNonNullableString();
        return new BigDecimal(s);
    }

    public Object readNull() {
        byte v = readNonNullableByte();
        switch (v) {
            case 0:
                return null;
            default:
                throw new IllegalArgumentException("expected null");
        }
    }

    public Date readNonNullableSqlDate() {
        return new Date(readNonNullableLong());
    }

    public Date readNullableSqlDate() {
        byte v = readNonNullableByte();
        switch (v) {
            case 0:
                return null;
            default:
                return readNonNullableSqlDate();
        }
    }

    public Time readNonNullableSqlTime() {
        return new Time(readNonNullableLong());
    }

    public Time readNullableSqlTime() {
        byte v = readNonNullableByte();
        switch (v) {
            case 0:
                return null;
            default:
                return readNonNullableSqlTime();
        }
    }

    public InputStream readNonNullableInputStream() {
        return new SubInputStream(this);
    }

    public Reader readNullableReader() {
        InputStream is = readNullableInputStream();
        return is == null ? null : new InputStreamReader(is);
    }

    public Reader readNonNullableReader() {
        InputStream is = readNonNullableInputStream();
        return new InputStreamReader(is);
    }

    public InputStream readNullableInputStream() {
        byte v = readNonNullableByte();
        switch (v) {
            case 0:
                return null;
            default:
                return readNonNullableInputStream();
        }
    }

    public byte[] readNonNullableByteArray() {
        int c = readNonNullableInt();
        byte[] r = new byte[c];
        readFully(r);
        return r;
    }

    public byte[] readNullableByteArray() {
        byte v = readNonNullableByte();
        switch (v) {
            case 0:
                return null;
            default: {
                return readNonNullableByteArray();
            }
        }
    }

    public StoreValue readAny() {
        return readAnyExpected(null);
    }

    public StoreValue readAnyExpected(StoreDataType fType) {
        int i = this.readNonNullableByte();
        if (fType != null) {
            if (i != StoreDataType.NULL.id() && fType.id() != i) {
//                StoreDataType storeDataType0 = StoreDataType.ofId(i).orElse(null);
//                throw new IllegalArgumentException("expected " + fType + " but got " + (storeDataType0 == null ? String.valueOf(i) : storeDataType0.name()));
            }
        }
        if (!StoreDataType.ofId(i).isPresent()) {
            throw new IllegalArgumentException("not found StoreDataType " + i);
        }
        StoreDataType storeDataType = StoreDataType.ofId(i).get();
        if (storeDataType == StoreDataType.NULL) {
            return DefaultStoreValue.ofNull();
        }
        return readAny(storeDataType);
    }

    public StoreValue readAny(StoreDataType storeDataType) {
        switch (storeDataType) {
            case NULL: {
                int i = this.readNonNullableByte();
                if (i != 0) {
                    throw new IllegalArgumentException("expected null");
                }
                return DefaultStoreValue.ofNull();
            }
            case BOOLEAN:
                return DefaultStoreValue.ofBoolean(this.readNonNullableBoolean());
            case NBOOLEAN:
                return DefaultStoreValue.ofBoolean(this.readNullableBoolean());

            case BYTE:
                return DefaultStoreValue.ofByte(this.readNonNullableByte());
            case NBYTE:
                return DefaultStoreValue.ofByte(this.readNullableByte());

            case SHORT:
                return DefaultStoreValue.ofShort(this.readNonNullableShort());
            case NSHORT:
                return DefaultStoreValue.ofShort(this.readNullableShort());

            case INT:
                return DefaultStoreValue.ofInt(this.readNonNullableInt());
            case NINT:
                return DefaultStoreValue.ofInt(this.readNullableInt());

            case LONG:
                return DefaultStoreValue.ofLong(this.readNonNullableLong());
            case NLONG:
                return DefaultStoreValue.ofLong(this.readNullableLong());

            case FLOAT:
                return DefaultStoreValue.ofFloat(this.readNonNullableFloat());
            case NFLOAT:
                return DefaultStoreValue.ofFloat(this.readNullableFloat());

            case DOUBLE:
                return DefaultStoreValue.ofDouble(this.readNonNullableDouble());
            case NDOUBLE:
                return DefaultStoreValue.ofDouble(this.readNullableDouble());

            case STRING:
                return DefaultStoreValue.ofString(this.readNonNullableString());
            case NSTRING:
                return DefaultStoreValue.ofString(this.readNullableString());

            case BIG_INT: {
                return DefaultStoreValue.ofBigInt(this.readNonNullableBigInt());
            }
            case NBIG_INT: {
                return DefaultStoreValue.ofBigInt(this.readNullableBigInt());
            }

            case BIG_DECIMAL: {
                return DefaultStoreValue.ofBigDecimal(this.readNonNullableBigDecimal());
            }
            case NBIG_DECIMAL: {
                return DefaultStoreValue.ofBigDecimal(this.readNullableBigDecimal());
            }

            case DATE: {
                return DefaultStoreValue.ofSqlDate(this.readNonNullableSqlDate());
            }
            case NDATE: {
                return DefaultStoreValue.ofSqlDate(this.readNullableSqlDate());
            }

            case TIME: {
                return DefaultStoreValue.ofSqlTime(this.readNonNullableSqlTime());
            }
            case NTIME: {
                return DefaultStoreValue.ofSqlTime(this.readNullableSqlTime());
            }

            case TIMESTAMP: {
                return DefaultStoreValue.ofSqlTimestamp(this.readNonNullableSqlTimestamp());
            }
            case NTIMESTAMP: {
                return DefaultStoreValue.ofSqlTimestamp(this.readNullableSqlTimestamp());
            }

            case BYTES: {
                return DefaultStoreValue.ofBytes(this.readNonNullableByteArray());
            }

            case NBYTES: {
                return DefaultStoreValue.ofBytes(this.readNullableByteArray());
            }

            case BYTE_STREAM: {
                return DefaultStoreValue.ofInputStream(this.readNonNullableInputStream());
            }
            case NBYTE_STREAM: {
                return DefaultStoreValue.ofInputStream(this.readNullableInputStream());
            }

            case CHAR_STREAM: {
                return DefaultStoreValue.ofReader(this.readNonNullableReader());
            }
            case NCHAR_STREAM: {
                return DefaultStoreValue.ofReader(this.readNullableReader());
            }

            case JAVA_OBJECT: {
                return DefaultStoreValue.ofJavaObject(this.readNonNullableObject());
            }
            case NJAVA_OBJECT: {
                return DefaultStoreValue.ofJavaObject(this.readNullableObject());
            }
            case DOCUMENT: {
                return DefaultStoreValue.ofDocument(this.readNonNullableStoreDocument());
            }
            case NDOCUMENT: {
                return DefaultStoreValue.ofDocument(this.readNullableStoreDocument());
            }
            default: {
                throw new IllegalArgumentException("unsupported yet " + storeDataType);
            }
        }
    }

    public Timestamp readNonNullableSqlTimestamp() {
        long a = readNonNullableLong();
        int b = readNonNullableInt();
        Timestamp tt = new Timestamp(a);
        tt.setNanos(b);
        return tt;
    }

    public Timestamp readNullableSqlTimestamp() {
        byte v = readNonNullableByte();
        switch (v) {
            case 0:
                return null;
            default:
                return readNonNullableSqlTimestamp();
        }
    }


    public void close() {
        try {
            dis.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public int read(byte[] buffer, int offset, int len) {
        try {
            return dis.read(buffer, offset, len);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public int read(byte[] buffer) {
        try {
            return dis.read(buffer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
