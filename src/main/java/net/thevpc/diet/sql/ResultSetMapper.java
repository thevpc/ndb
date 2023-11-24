package net.thevpc.diet.sql;

public interface ResultSetMapper<T> {
    T get(SafeResultSet rs);
}
