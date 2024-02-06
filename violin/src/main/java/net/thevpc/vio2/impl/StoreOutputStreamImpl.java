/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.vio2.impl;

import net.thevpc.vio2.api.ObjectWriter;
import net.thevpc.vio2.api.StoreOutputStream;
import net.thevpc.vio2.model.StoreDataType;
import net.thevpc.vio2.model.StoreDocument;
import net.thevpc.vio2.model.StoreDocumentEntry;
import net.thevpc.vio2.model.StoreValue;

import java.io.*;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author vpc
 */
public class StoreOutputStreamImpl implements StoreOutputStream {
    private Sers sers;
    private DataOutputStream dos;

    public StoreOutputStreamImpl(OutputStream out, Sers sers) {
        dos = new DataOutputStream(out);
        this.sers = sers;
    }

    @Override
    public void writeNullableDocument(StoreDocument value) {
        if (value == null) {
            this.writeNullPrefix();
        } else {
            writeNonNullableDocument(value);
            this.writeNonNullPrefix();
        }
    }

    @Override
    public void writeNonNullableDocumentEntry(StoreDocumentEntry value) {
        this.writeNonNullableString(value.getName());
        this.writeNonNullableStruct(StoreValue.class, value.getValue());
    }

    @Override
    public void writeNonNullableDocument(StoreDocument value) {
        this.writeNonNullableInt(value.size());
        for (StoreDocumentEntry entry : value.entries()) {
            this.writeNonNullableDocumentEntry(entry);
        }
    }

    @Override
    public <T> void writeNonNullableStruct(Class<T> clazz, T value) {
        if (value == null) {
            throw new IllegalArgumentException("nul not accepted");
        } else {
            Optional<ObjectWriter<T>> oo = sers.getObjectWriter(clazz);

            if (oo.isPresent()) {
                ObjectWriter<T> u = (ObjectWriter<T>) oo.get();
                u.write(value, this);
            } else if (clazz.isArray()) {
                int len = Array.getLength(value);
                writeNonNullableInt(len);
                Class ct = clazz.getComponentType();
                boolean p = ct.isPrimitive();
                if (p) {
                    switch (ct.getName()) {
                        case "boolean": {
                            boolean[] zz = (boolean[]) value;
                            for (int i = 0; i < len; i++) {
                                writeNonNullableBoolean(zz[i]);
                            }
                            break;
                        }
                        case "short": {
                            short[] zz = (short[]) value;
                            for (int i = 0; i < len; i++) {
                                writeNonNullableShort(zz[i]);
                            }
                            break;
                        }
                        case "byte": {
                            byte[] zz = (byte[]) value;
                            writeRawBytes(zz);
                            break;
                        }
                        case "char": {
                            char[] zz = (char[]) value;
                            writeNullableCharArray(zz);
                            break;
                        }
                        case "int": {
                            int[] zz = (int[]) value;
                            for (int i = 0; i < len; i++) {
                                writeNonNullableInt(zz[i]);
                            }
                            break;
                        }
                        case "long": {
                            long[] zz = (long[]) value;
                            for (int i = 0; i < len; i++) {
                                writeNonNullableLong(zz[i]);
                            }
                            break;
                        }
                        case "float": {
                            float[] zz = (float[]) value;
                            for (int i = 0; i < len; i++) {
                                writeNonNullableFloat(zz[i]);
                            }
                            break;
                        }
                        case "double": {
                            double[] zz = (double[]) value;
                            for (int i = 0; i < len; i++) {
                                writeNonNullableDouble(zz[i]);
                            }
                            break;
                        }
                        default: {
                            throw new IllegalArgumentException("unsupported");
                        }
                    }
                } else {
                    for (int i = 0; i < len; i++) {
                        Object o = Array.get(value, i);
                        writeNullableStruct(ct, o);
                    }
                }

            } else {
                switch (clazz.getName()) {
                    case "java.lang.Boolean": {
                        writeNullableBoolean((Boolean) value);
                        break;
                    }
                    case "java.lang.Byte": {
                        writeNullableByte((Byte) value);
                        break;
                    }
                    case "java.lang.Character": {
                        writeNullableCharacter((Character) value);
                        break;
                    }
                    case "java.lang.Integer": {
                        writeNullableInt((Integer) value);
                        break;
                    }
                    case "java.lang.Long": {
                        writeNullableLong((Long) value);
                        break;
                    }
                    case "java.lang.Short": {
                        writeNullableShort((Short) value);
                        break;
                    }
                    case "java.lang.Float": {
                        writeNullableFloat((Float) value);
                        break;
                    }
                    case "java.lang.Double": {
                        writeNullableDouble((Double) value);
                        break;
                    }
                    case "java.math.BigInteger": {
                        writeNullableBigInt((BigInteger) value);
                        break;
                    }
                    case "java.math.BigDecimal": {
                        writeNullableBigDecimal((BigDecimal) value);
                        break;
                    }
                }
            }
        }
    }


    @Override
    public void writeNonNullableCharArray(char[] str) {
        if (str.length <= 0xFFFFL) {
            this.writeNonNullableByte(1);
            this.writeUTF(new String(str));
        } else {
            this.writeNonNullableByte(2);
            this.writeNonNullableInt(str.length);
            writeNonNullChars(new String(str));
        }
    }

    @Override
    public void writeNullableCharArray(char[] str) {
        if (str == null) {
            this.writeNullPrefix();
        } else {
            this.writeNonNullPrefix();
            writeNonNullableCharArray(str);
        }
    }

    @Override
    public void writeNonNullChars(String s) {
        try {
            dos.writeChars(s);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeNonNullableByte(int i) {
        try {
            dos.writeByte(i);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeNonNullableShort(short i) {
        try {
            dos.writeShort(i);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeNonNullableInt(int i) {
        try {
            dos.writeInt(i);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeUTF(String i) {
        try {
            dos.writeUTF(i);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public <T> void writeNullableStruct(Class<T> clazz, T value) {
        if (value == null) {
            writeNullPrefix();
        } else {
            writeNonNullPrefix();
            ObjectWriter<T> u = (ObjectWriter<T>) sers.getObjectWriter(clazz).get();
            u.write(value, this);
        }
    }

    @Override
    public void writeNullPrefix() {
        writeNonNullableByte(0);
    }

    @Override
    public void writeNonNullPrefix() {
        writeNonNullableByte(1);
    }

    @Override
    public void writeNonNullableString(String str) {
        if (getUTFLength(str) <= 0xFFFFL) {
            this.writeNonNullableByte(1);
            this.writeUTF(str);
        } else {
            this.writeNonNullableByte(2);
            this.writeNonNullableInt(str.length());
            writeNonNullChars(str);
        }
    }

    @Override
    public void writeNullableString(String str) {
        if (str == null) {
            this.writeNullPrefix();
        } else {
            writeNonNullPrefix();
            this.writeNonNullableString(str);
        }
    }

    /**
     * //FROM JDK!!! ObjectOutputStream
     *
     * @param s
     * @return
     */
    @Override
    public long getUTFLength(String s) {
        int CHAR_BUF_SIZE = 256;
        char[] cbuf = new char[CHAR_BUF_SIZE];
        int len = s.length();
        long utflen = 0;
        for (int off = 0; off < len; ) {
            int csize = Math.min(len - off, CHAR_BUF_SIZE);
            s.getChars(off, off + csize, cbuf, 0);
            for (int cpos = 0; cpos < csize; cpos++) {
                char c = cbuf[cpos];
                if (c >= 0x0001 && c <= 0x007F) {
                    utflen++;
                } else if (c > 0x07FF) {
                    utflen += 3;
                } else {
                    utflen += 2;
                }
            }
            off += csize;
        }
        return utflen;
    }

    @Override
    public void writeNull() {
        this.writeNonNullableByte(0);
    }

    @Override
    public void writeNullableBoolean(Boolean v) {
        if (v == null) {
            this.writeNullPrefix();
        } else {
            this.writeNonNullPrefix();
            this.writeNonNullableBoolean(v);
        }
    }

    @Override
    public void writeStoreValue(StoreValue v) {
        Object value = v == null ? null : v.getValue();
        if (value == null) {
            writeNull();
        } else {
            writeNonNullableByte(v.getType().id());
            writeAny(v.getType(), value);
        }
    }

    @Override
    public void writeAny(StoreDataType storeDataType, Object v) {
        switch (storeDataType) {
            case NULL: {
                writeNull();
                break;
            }

            case BOOLEAN:
                this.writeNonNullableBoolean((Boolean) v);
                break;
            case NBOOLEAN:
                this.writeNullableBoolean((Boolean) v);
                break;

            case BYTE:
                this.writeNonNullableByte((Byte) v);
                break;
            case NBYTE:
                this.writeNullableByte((Byte) v);
                break;

            case SHORT:
                this.writeNonNullableShort((Short) v);
                break;
            case NSHORT:
                this.writeNullableShort((Short) v);
                break;

            case INT:
                this.writeNonNullableInt((Integer) v);
                break;
            case NINT:
                this.writeNullableInt((Integer) v);
                break;

            case LONG:
                this.writeNonNullableLong((Long) v);
                break;
            case NLONG:
                this.writeNullableLong((Long) v);
                break;

            case FLOAT:
                this.writeNonNullableFloat((Float) v);
                break;
            case NFLOAT:
                this.writeNullableFloat((Float) v);
                break;

            case DOUBLE:
                this.writeNonNullableDouble((Double) v);
                break;
            case NDOUBLE:
                this.writeNullableDouble((Double) v);
                break;

            case STRING:
                this.writeNonNullableString((String) v);
                break;
            case NSTRING:
                this.writeNullableString((String) v);
                break;

            case BIG_INT: {
                this.writeNonNullableBigInt((BigInteger) v);
                break;
            }
            case NBIG_INT: {
                this.writeNullableBigInt((BigInteger) v);
                break;
            }

            case BIG_DECIMAL: {
                this.writeNonNullableBigDecimal((BigDecimal) v);
                break;
            }
            case NBIG_DECIMAL: {
                this.writeNullableBigDecimal((BigDecimal) v);
                break;
            }

            case DATE: {
                this.writeNonNullableSqlDate((java.sql.Date) v);
                break;
            }
            case NDATE: {
                this.writeNullableSqlDate((java.sql.Date) v);
                break;
            }

            case TIME: {
                this.writeNonNullableSqlTime((java.sql.Time) v);
                break;
            }
            case NTIME: {
                this.writeNullableSqlTime((java.sql.Time) v);
                break;
            }

            case TIMESTAMP: {
                this.writeNonNullableSqlTimestamp((java.sql.Timestamp) v);
                break;
            }
            case NTIMESTAMP: {
                this.writeNullableSqlTimestamp((java.sql.Timestamp) v);
                break;
            }

            case BYTES: {
                this.writeNonNullableBytes((byte[]) v);
                break;
            }
            case NBYTES: {
                this.writeNullableBytes((byte[]) v);
                break;
            }

            case BYTE_STREAM: {
                this.writeNonNullableInputStream((InputStream) v);
                break;
            }
            case NBYTE_STREAM: {
                this.writeNullableInputStream((InputStream) v);
                break;
            }

            case CHAR_STREAM: {
                this.writeNonNullableReader((Reader) v);
                break;
            }
            case NCHAR_STREAM: {
                this.writeNullableReader((Reader) v);
                break;
            }

            case JAVA_OBJECT: {
                this.writeNonNullableObject(v);
                break;
            }
            case NJAVA_OBJECT: {
                this.writeNullableObject(v);
                break;
            }

            case DOCUMENT: {
                this.writeNonNullableDocument((StoreDocument) v);
                break;
            }
            case NDOCUMENT: {
                this.writeNullableDocument((StoreDocument) v);
                break;
            }
            default: {
                throw new IllegalArgumentException("unsupported yet " + storeDataType);
            }
        }
    }

    @Override
    public void writeNonNullableBoolean(boolean v) {
        try {
            dos.writeBoolean(v);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeNullableCharacter(Character v) {
        if (v == null) {
            this.writeNullPrefix();
        } else {
            this.writeNonNullPrefix();
            this.writeNonNullableInt(v);
        }
    }

    @Override
    public void writeNullableShort(Short v) {
        if (v == null) {
            this.writeNullPrefix();
        } else {
            this.writeNonNullPrefix();
            this.writeNonNullableShort(v);
        }
    }

    @Override
    public void writeNullableLong(Long v) {
        if (v == null) {
            this.writeNullPrefix();
        } else {
            this.writeNonNullPrefix();
            this.writeNonNullableLong(v);
        }
    }

    @Override
    public void writeNonNullableLong(long v) {
        try {
            dos.writeLong(v);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeNonNullableFloat(float v) {
        try {
            dos.writeFloat(v);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeNonNullableDouble(double v) {
        try {
            dos.writeDouble(v);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeNullableFloat(Float v) {
        if (v == null) {
            this.writeNullPrefix();
        } else {
            this.writeNonNullPrefix();
            this.writeNonNullableFloat(v);
        }
    }

    @Override
    public void writeNullableDouble(Double v) {
        if (v == null) {
            this.writeNullPrefix();
        } else {
            this.writeNonNullPrefix();
            this.writeNonNullableDouble(v);
        }
    }

    @Override
    public void writeNullableInt(Integer v) {
        if (v == null) {
            this.writeNullPrefix();
        } else {
            this.writeNonNullPrefix();
            this.writeNonNullableInt(v);
        }
    }

    @Override
    public void writeNullableByte(Byte v) {
        if (v == null) {
            this.writeNullPrefix();
        } else {
            this.writeNonNullPrefix();
            this.writeNonNullableByte(v);
        }
    }

    @Override
    public void writeNullableBigInt(BigInteger v) {
        if (v == null) {
            this.writeNullPrefix();
        } else {
            writeNonNullPrefix();
            this.writeNonNullableString(v.toString());
        }
    }

    @Override
    public void writeNonNullableBigInt(BigInteger v) {
        this.writeNonNullableString(v.toString());
    }

    @Override
    public void writeNullableBigDecimal(BigDecimal v) {
        if (v == null) {
            this.writeNullPrefix();
        } else {
            writeNonNullPrefix();
            this.writeNonNullableString(v.toString());
        }
    }

    @Override
    public void writeNonNullableBigDecimal(BigDecimal v) {
        this.writeNonNullableString(v.toString());
    }

    @Override
    public void writeNullableSqlDate(java.sql.Date v) {
        if (v == null) {
            this.writeNullPrefix();
        } else {
            this.writeNonNullPrefix();
            this.writeNonNullableLong(v.getTime());
        }
    }

    @Override
    public void writeNonNullableSqlDate(java.sql.Date v) {
        this.writeNonNullableLong(v.getTime());
    }

    @Override
    public void writeNullableSqlTime(java.sql.Time v) {
        if (v == null) {
            this.writeNullPrefix();
        } else {
            this.writeNonNullPrefix();
            this.writeNonNullableLong(v.getTime());
        }
    }

    @Override
    public void writeNonNullableSqlTime(java.sql.Time v) {
        this.writeNonNullableLong(v.getTime());
    }

    @Override
    public void writeNonNullableObject(Object v) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(b);
            oos.writeObject(v);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        this.writeNonNullableBytes(b.toByteArray());
    }

    @Override
    public void writeNullableObject(Object v) {
        if (v == null) {
            this.writeNullPrefix();
        } else {
            this.writeNonNullPrefix();
            writeNonNullableObject(v);
        }
    }

    @Override
    public void writeNonNullableReader(Reader v) {
        char[] buffer = new char[WRITE_BUFFER_SIZE];
        int c = 0;
        while ((c = _readAvailable(v, buffer, 0, buffer.length)) > 0) {
            CharBuffer charBuffer = CharBuffer.wrap(buffer, 0, c);
            ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
            byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                    byteBuffer.position(), byteBuffer.limit());

//            String s = new String(buffer, 0, c);
//            byte[] bytes = s.getBytes();

            this.writeNonNullableLong(bytes.length);
            this.writeRawBytes(bytes, 0, bytes.length);
        }
        this.writeNonNullableLong(-1);
    }

    @Override
    public void writeNullableReader(Reader v) {
        if (v == null) {
            this.writeNullPrefix();
        } else {
            this.writeNonNullPrefix();
            writeNonNullableReader(v);
        }
    }

    @Override
    public void writeRawBytes(byte[] b) {
        try {
            dos.write(b);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeRawBytes(byte b[], int off, int len) {
        try {
            dos.write(b, off, len);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private int _readAvailable(InputStream is, byte[] buffer, int offset, int len) {
        int l = len;
        int o = offset;
        int r = 0;
        while (l > 0) {
            int z = 0;
            try {
                z = is.read(buffer, o, l);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            if (z <= 0) {
                break;
            }
            o += z;
            l -= z;
            r += z;
        }
        return r;
    }

    private int _readAvailable(Reader is, char[] buffer, int offset, int len) {
        int l = len;
        int o = offset;
        int r = 0;
        while (l > 0) {
            int z = 0;
            try {
                z = is.read(buffer, o, l);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            if (z <= 0) {
                break;
            }
            o += z;
            l -= z;
            r += z;
        }
        return r;
    }

    @Override
    public void writeNonNullableInputStream(InputStream v) {
        byte[] buffer = new byte[WRITE_BUFFER_SIZE];
        int c = 0;
        while ((c = _readAvailable(v, buffer, 0, buffer.length)) > 0) {
            this.writeNonNullableLong(c);
            this.writeRawBytes(buffer, 0, c);
        }
        this.writeNonNullableLong(-1);
    }

    @Override
    public void writeNullableInputStream(InputStream v) {
        if (v == null) {
            this.writeNullPrefix();
        } else {
            this.writeNonNullPrefix();
            this.writeNonNullableInputStream(v);
        }
    }

    @Override
    public void writeNonNullableBytes(byte[] v) {
        this.writeNonNullableInt(v.length);
        this.writeRawBytes(v);
    }

    @Override
    public void writeNullableBytes(byte[] v) {
        if (v == null) {
            this.writeNullPrefix();
        } else {
            this.writeNonNullPrefix();
            writeNonNullableBytes(v);
        }
    }

    @Override
    public void writeNullableSqlTimestamp(Timestamp v) {
        if (v == null) {
            this.writeNullPrefix();
        } else {
            this.writeNonNullPrefix();
            this.writeNonNullableLong(v.getTime());
            this.writeNonNullableInt(v.getNanos());
        }
    }

    @Override
    public void writeNonNullableSqlTimestamp(Timestamp v) {
        this.writeNonNullableLong(v.getTime());
        this.writeNonNullableInt(v.getNanos());
    }

    @Override
    public void close() {
        try {
            dos.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void flush() {
        try {
            dos.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
