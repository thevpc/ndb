package net.thevpc.dbrman.impl;

import com.google.gson.Gson;
import net.thevpc.dbrman.api.SqlRow;
import net.thevpc.dbrman.api.SqlRowConversionContext;
import net.thevpc.vio2.impl.AbstractIoCell;
import net.thevpc.vio2.util.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

public abstract class AbstractSqlRow implements SqlRow {

    public AbstractSqlRow() {
    }

    @Override
    public Long asLong() {
        return getLong(1);
    }

    @Override
    public String asString() {
        return getString(1);
    }

    @Override
    public Object[] asArray() {
        Object[] row = new Object[getColumnsCount()];
        for (int i = 0; i < row.length; i++) {
            row[i] = getObject(i + 1);
        }
        return row;
    }


    @Override
    public String asJsonObject(SqlRowConversionContext context) {
        if (context == null) {
            context = new DefaultSqlRowConversionContext();
        }
        File parentFile = context.getLobFolder();
        if (parentFile != null) {
            parentFile.mkdirs();
        } else {
            parentFile = new File(".");
        }
        Gson gson = new Gson();
        Map<String,Object> array = asMap();
        Map<String,Object> array2 = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : array.entrySet()) {
            String k = e.getKey();
            Object object = e.getValue();
            if(object==null){
                if(!context.isIgnoreNulls()){
                    array2.put(k,null);
                }
            }else {
                Object u = AbstractIoCell.toLobFile(object, parentFile);
                if (AbstractIoCell.isLobPointer(u)) {
                    array2.put(k,u.toString());
                } else {
                    array2.put(k,u);
                }
            }
        }
        return gson.toJson(array2);
    }

    @Override
    public String asJsonArray(SqlRowConversionContext context) {
        if (context == null) {
            context = new DefaultSqlRowConversionContext();
        }
        File parentFile = context.getLobFolder();
        if (parentFile != null) {
            parentFile.mkdirs();
        } else {
            parentFile = new File(".");
        }
        Gson gson = new Gson();
        Object[] array = asArray();
        Object[] array2 = new Object[array.length];
        for (int i = 0; i < array.length; i++) {
            Object object = array[i];
            Object u = AbstractIoCell.toLobFile(object, parentFile);
            if (AbstractIoCell.isLobPointer(u)) {
                array2[i] = u.toString();
            } else {
                array2[i] = u;
            }
        }
        return gson.toJson(array2);
    }

    @Override
    public String asCsv(SqlRowConversionContext context) {
        if (context == null) {
            context = new DefaultSqlRowConversionContext();
        }
        File parentFile = context.getLobFolder();
        if (parentFile != null) {
            parentFile.mkdirs();
        } else {
            parentFile = new File(".");
        }
        Object[] array = asArray();
        String[] array2 = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            Object object = array[i];
            Object u = AbstractIoCell.toLobFile(object, parentFile);
            if (AbstractIoCell.isLobPointer(u)) {
                array2[i] = escapeCsv(u.toString());
            } else {
                array2[i] = escapeCsv(String.valueOf(object));
            }
        }
        return String.join(",",array2);
    }

    private String escapeCsv(String s) {
        boolean escape = false;
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\n': {
                    sb.append("\\n");
                    escape = true;
                    break;
                }
                case '\f': {
                    sb.append("\\f");
                    escape = true;
                    break;
                }
                case '\t': {
                    sb.append("\\t");
                    escape = true;
                    break;
                }
                case '\0': {
                    sb.append("\\0");
                    escape = true;
                    break;
                }
                case '\"': {
                    sb.append("\\\"");
                    escape = true;
                    break;
                }
                case '\'':
                case ',':
                case ';':
                case ' ': {
                    sb.append(c);
                    escape = true;
                    break;
                }
                default: {
                    if (c < 32) {
                        escape = true;
                        sb.append(c);
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        if (escape) {
            sb.insert(0, '\"');
        }
        sb.append('\"');
        return sb.toString();
    }
}
