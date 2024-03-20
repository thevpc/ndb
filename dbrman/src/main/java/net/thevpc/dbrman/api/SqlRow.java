package net.thevpc.dbrman.api;

import java.util.Map;

public interface SqlRow {
    Long asLong();
    String asString();

    Map<String, Object> asMap();

    Long getLong(int index);

    String getString(int index);

    Object getObject(int index);

    int getColumnsCount();

    Object[] asArray();

    String asJsonArray(SqlRowConversionContext context);
    String asJsonObject(SqlRowConversionContext context);
    String asCsv(SqlRowConversionContext context);
}
