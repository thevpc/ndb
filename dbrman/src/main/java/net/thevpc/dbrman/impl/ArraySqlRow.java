package net.thevpc.dbrman.impl;

import com.google.gson.Gson;
import net.thevpc.dbrman.api.SqlRow;
import net.thevpc.dbrman.api.SqlRowConversionContext;
import net.thevpc.vio2.impl.AbstractIoCell;
import net.thevpc.vio2.util.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.UUID;

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
