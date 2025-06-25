package net.thevpc.nsql;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;

public class NSqlParam implements Cloneable {
    public String columnName;
    public Integer columnIndex;
    public NSqlColumnType columnType;
    public Integer rawType;
    public Mode mode;
    public Object value;

    public static NSqlParam ofString(String columnName, String value) {
        return of(columnName, NSqlColumnType.STRING, value);
    }

    public static NSqlParam ofString(int columnIndex, String value) {
        return of(columnIndex, NSqlColumnType.STRING, value);
    }

    public static NSqlParam ofInt(String columnName, Integer value) {
        return of(columnName, NSqlColumnType.INT, value);
    }

    public static NSqlParam ofInt(int columnIndex, Integer value) {
        return of(columnIndex, NSqlColumnType.INT, value);
    }

    public static NSqlParam ofDouble(String columnName, Double value) {
        return of(columnName, NSqlColumnType.DOUBLE, value);
    }

    public static NSqlParam ofDouble(int columnIndex, Double value) {
        return of(columnIndex, NSqlColumnType.DOUBLE, value);
    }

    public static NSqlParam ofBoolean(String columnName, Boolean value) {
        return of(columnName, NSqlColumnType.BOOLEAN, value);
    }

    public static NSqlParam ofBoolean(int columnIndex, Boolean value) {
        return of(columnIndex, NSqlColumnType.BOOLEAN, value);
    }

    public static NSqlParam ofTimestamp(String columnName, Instant value) {
        return of(columnName, NSqlColumnType.TIMESTAMP, value == null ? null : Timestamp.from(value));
    }

    public static NSqlParam ofTimestamp(int columnIndex, Instant value) {
        return of(columnIndex, NSqlColumnType.TIMESTAMP, value == null ? null : Timestamp.from(value));
    }

    public static NSqlParam ofTimestamp(String columnName, java.util.Date value) {
        return of(columnName, NSqlColumnType.TIMESTAMP, value == null ? null : (value instanceof Timestamp) ? (Timestamp) value : new Timestamp(value.getTime()));
    }

    public static NSqlParam ofTimestamp(int columnIndex, java.util.Date value) {
        return of(columnIndex, NSqlColumnType.TIMESTAMP, value == null ? null : (value instanceof Timestamp) ? (Timestamp) value : new Timestamp(value.getTime()));
    }

    public static NSqlParam ofDate(String columnName, java.util.Date value) {
        return of(columnName, NSqlColumnType.DATE, value == null ? null : (value instanceof java.sql.Date) ? (java.sql.Date) value : new java.sql.Date(value.getTime()));
    }

    public static NSqlParam ofDate(int columnIndex, java.util.Date value) {
        return of(columnIndex, NSqlColumnType.DATE, value == null ? null : (value instanceof java.sql.Date) ? (java.sql.Date) value : new java.sql.Date(value.getTime()));
    }

    public static NSqlParam ofDate(String columnName, Instant value) {
        return of(columnName, NSqlColumnType.DATE, value == null ? null : new java.sql.Date(value.toEpochMilli()));
    }

    public static NSqlParam ofDate(int columnIndex, Instant value) {
        return of(columnIndex, NSqlColumnType.DATE, value == null ? null : new java.sql.Date(value.toEpochMilli()));
    }

    public static NSqlParam ofDate(String columnName, LocalDate value) {
        return of(columnName, NSqlColumnType.DATE, value == null ? null : java.sql.Date.valueOf(value));
    }

    public static NSqlParam ofDate(int columnIndex, LocalDate value) {
        return of(columnIndex, NSqlColumnType.DATE, value == null ? null : java.sql.Date.valueOf(value));
    }

    public static NSqlParam ofTime(String columnName, java.sql.Time value) {
        return of(columnName, NSqlColumnType.TIME, value);
    }

    public static NSqlParam ofTime(int columnIndex, java.sql.Time value) {
        return of(columnIndex, NSqlColumnType.TIME, value);
    }

    public static NSqlParam of(String columnName, NSqlColumnType columnType, Object value) {
        return new NSqlParam(Mode.NAME, columnName, null, columnType, null, value);
    }

    public static NSqlParam of(int columnIndex, NSqlColumnType columnType, Object value) {
        return new NSqlParam(Mode.INDEX, null, columnIndex, columnType, null, value);
    }

    private NSqlParam(Mode mode, String columnName, Integer columnIndex, NSqlColumnType columnType, Integer rawType, Object value) {
        this.mode = mode;
        this.columnName = columnName;
        this.columnIndex = columnIndex;
        this.columnType = columnType;
        this.rawType = rawType;
        this.value = value;
    }

    public static enum Mode {
        NAME,
        INDEX,
    }

    public String getColumnName() {
        return columnName;
    }

    public Integer getColumnIndex() {
        return columnIndex;
    }

    public NSqlColumnType getColumnType() {
        return columnType;
    }

    public Integer getRawType() {
        return rawType;
    }

    public Mode getMode() {
        return mode;
    }

    public Object getValue() {
        return value;
    }
}
