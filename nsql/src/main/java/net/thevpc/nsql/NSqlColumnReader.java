package net.thevpc.nsql;

import java.sql.ResultSet;

public interface NSqlColumnReader<T> {
    T read(ResultSet resultSet, int columnIndex);

    T read(ResultSet resultSet, String columnName);
}
