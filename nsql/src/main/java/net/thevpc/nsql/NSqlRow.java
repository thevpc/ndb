package net.thevpc.nsql;

import java.util.Map;

public interface NSqlRow {
    Long asLong();

    String asString();

    Map<String, Object> asMap();

    Long getLong(int index);

    String getString(int index);

    Object getObject(int index);

    int getColumnsCount();

    Object[] asArray();
}
