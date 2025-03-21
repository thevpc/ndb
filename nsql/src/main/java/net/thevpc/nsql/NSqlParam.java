package net.thevpc.nsql;

public class NSqlParam implements Cloneable {
    public String columnName;
    public Integer columnIndex;
    public NSqlColumnType columnType;
    public Integer rawType;
    public Mode mode;
    public Object value;

    public static NSqlParam of(String columnName, NSqlColumnType columnType, Object value) {
        return new NSqlParam(Mode.NAME, columnName,null,columnType, null,value);
    }

    public static NSqlParam of(int columnIndex, NSqlColumnType columnType, Object value) {
        return new NSqlParam(Mode.INDEX, null,columnIndex,columnType, null,value);
    }

    private NSqlParam(Mode mode, String columnName, Integer columnIndex, NSqlColumnType columnType, Integer rawType, Object value) {
        this.mode = mode;
        this.columnName = columnName;
        this.columnIndex = columnIndex;
        this.columnType = columnType;
        this.rawType = rawType;
        this.value = value;
    }

    public static  enum Mode{
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
