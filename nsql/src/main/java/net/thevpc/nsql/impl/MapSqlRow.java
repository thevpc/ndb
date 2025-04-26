package net.thevpc.nsql.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class MapSqlRow extends AbstractSqlRow {
    private Map.Entry<String, Object>[] entries;
    private Map<String, Object> map;

    public MapSqlRow(Map<String, Object> arr) {
        this.map=arr;
        entries = arr.entrySet().toArray(new Map.Entry[0]);
    }

    @Override
    public Long asLong() {
        return getLong(1);
    }

    @Override
    public Map<String, Object> asMap() {
        LinkedHashMap<String, Object> m = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : entries) {
            m.put(e.getKey(), e.getValue());
        }
        return m;
    }

    @Override
    public Object getObject(String name) {
        return map.get(name);
    }

    @Override
    public Object getObject(int index) {
        return entries[index - 1].getValue();
    }

    @Override
    public int getColumnsCount() {
        return entries.length;
    }

    public int indexOfColumn(String name) {
        for (int i = 0; i < entries.length; i++) {
            Map.Entry<String, Object> e = entries[i];
            if (Objects.equals(e.getKey(), name)) {
                return i + 1;
            }
        }
        return -1;
    }
}
