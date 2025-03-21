package net.thevpc.nsql.impl;

import java.util.Map;

public class ArraySqlRow extends AbstractSqlRow {
    private Object[] arr;

    public ArraySqlRow(Object[] arr) {
        this.arr = arr;
    }

    @Override
    public Long asLong() {
        return getLong(1);
    }

    @Override
    public Map<String, Object> asMap() {
        return null;
    }

    @Override
    public Long getLong(int index) {
        Number n = (Number) getObject(index);
        if (n instanceof Long) {
            return (Long) n;
        }
        return n == null ? null : n.longValue();
    }

    @Override
    public String getString(int index) {
        return(String) getObject(index);
    }

    @Override
    public Object getObject(int index) {
        return arr[index-1];
    }

    @Override
    public int getColumnsCount() {
        return arr.length;
    }
}
