package net.thevpc.nsql.dump.util;

import com.google.gson.Gson;
import net.thevpc.nsql.dump.io.Out;
import net.thevpc.nsql.NLobUtils;
import net.thevpc.nsql.NSqlRow;
import net.thevpc.nsql.NSqlRowConversionContext;
import net.thevpc.nsql.impl.DefaultSqlRowConversionContext;
import net.thevpc.nsql.impl.ResultSetQueryResult;
import net.thevpc.nuts.util.NCreated;

import java.io.File;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExportHelper {

    public static String asJsonObject(NSqlRow row, NSqlRowConversionContext context) {
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
        Map<String, Object> array = row.asMap();
        Map<String, Object> array2 = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : array.entrySet()) {
            String k = e.getKey();
            Object object = e.getValue();
            if (object == null) {
                if (!context.isIgnoreNulls()) {
                    array2.put(k, null);
                }
            } else {
                Object u = NLobUtils.toLobFile(object, parentFile == null ? null : parentFile.toPath());
                if (NLobUtils.isLobPointer(u)) {
                    array2.put(k, u.toString());
                } else {
                    if (u instanceof Timestamp) {
                        u = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Timestamp) u);
                    } else if (u instanceof java.sql.Date) {
                        u = new SimpleDateFormat("yyyy-MM-dd").format((java.sql.Date) u);
                    } else if (u instanceof java.util.Date) {
                        u = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((java.util.Date) u);
                    }
                    array2.put(k, u);
                }
            }
        }
        return gson.toJson(array2);
    }

    public static String asJsonArray(NSqlRow row, NSqlRowConversionContext context) {
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
        Object[] array = row.asArray();
        Object[] array2 = new Object[array.length];
        for (int i = 0; i < array.length; i++) {
            Object object = array[i];
            Object u = NLobUtils.toLobFile(object, parentFile == null ? null : parentFile.toPath());
            if (NLobUtils.isLobPointer(u)) {
                array2[i] = u.toString();
            } else {
                array2[i] = u;
            }
        }
        return gson.toJson(array2);
    }

    public static String asCsv(NSqlRow row, NSqlRowConversionContext context) {
        if (context == null) {
            context = new DefaultSqlRowConversionContext();
        }
        File parentFile = context.getLobFolder();
        if (parentFile != null) {
            parentFile.mkdirs();
        } else {
            parentFile = new File(".");
        }
        Object[] array = row.asArray();
        String[] array2 = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            Object object = array[i];
            Object u = NLobUtils.toLobFile(object, parentFile == null ? null : parentFile.toPath());
            if (NLobUtils.isLobPointer(u)) {
                array2[i] = escapeCsv(u.toString());
            } else {
                array2[i] = escapeCsv(String.valueOf(object));
            }
        }
        return String.join(",", array2);
    }

    private static String escapeCsv(String s) {
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

    public static void writeCsv(ResultSetQueryResult result, Out file, NSqlRowConversionContext ccontext) {
        NCreated<PrintStream> ps = file.toPrintStream();
        if (ps == null) {
            ps = NCreated.ofExisting(System.out);
        }
        ps.get().println(asCsv(result.columnsRow(), ccontext));
        for (NSqlRow sqlRow : result.iterable()) {
            ps.get().println(asCsv(sqlRow, ccontext));
        }
    }

    public static void writeJsonArrays(ResultSetQueryResult result, Out file, NSqlRowConversionContext ccontext) {
        NCreated<PrintStream> ps = file.toPrintStream();
        if (ps == null) {
            ps = NCreated.ofExisting(System.out);
        }
        ps.get().println(asJsonArray(result.columnsRow(), ccontext));
        for (NSqlRow sqlRow : result.iterable()) {
            ps.get().println(asJsonArray(sqlRow, ccontext));
        }
    }

    public static void writeJsonObjects(ResultSetQueryResult result, Out file, NSqlRowConversionContext ccontext) {
        NCreated<PrintStream> ps = file.toPrintStream();
        if (ps == null) {
            ps = NCreated.ofExisting(System.out);
        }
        for (NSqlRow sqlRow : result.iterable()) {
            ps.get().println(asJsonObject(sqlRow, ccontext));
        }
    }

}
