package net.thevpc.nsql.impl;

import java.util.Map;
import java.util.Objects;

public class ArraySqlRow extends AbstractSqlRow {
    private String[] names;
    private Object[] arr;

    public ArraySqlRow(String[] names, Object[] arr) {
        this.names = names;
        this.arr = arr;
    }

    @Override
    public Object getObject(String name) {
        for (int i = 0; i < names.length; i++) {
            if (Objects.equals(names[i], name)) {
                return arr[i];
            }
        }
        throw new IllegalArgumentException("invalid column name " + name);
    }

    @Override
    public Map<String, Object> asMap() {
        return null;
    }

    @Override
    public Object getObject(int index) {
        return arr[index - 1];
    }

    @Override
    public int getColumnsCount() {
        return arr.length;
    }
}
