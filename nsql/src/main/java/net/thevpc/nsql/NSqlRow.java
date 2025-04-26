package net.thevpc.nsql;

import java.time.Instant;
import java.util.Map;

public interface NSqlRow {
    Long asLong();

    String asString();

    Map<String, Object> asMap();

    Integer getInt(int index);

    Long getLong(int index);

    Double getDouble(int index);

    String getString(int index);

    Instant getInstant(int index);

    Boolean getBoolean(int index);

    Object getObject(int index);

    Integer getInt(String name);

    Long getLong(String name);

    String getString(String name);

    Instant getInstant(String name);

    Boolean getBoolean(String name);

    Double getDouble(String name);

    Object getObject(String name);

    int getColumnsCount();

    Object[] asArray();
}
