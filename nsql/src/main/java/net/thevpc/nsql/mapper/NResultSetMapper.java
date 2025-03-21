package net.thevpc.nsql.mapper;

import java.sql.ResultSet;

public interface NResultSetMapper<T> {
    T get(ResultSet rs);
}
