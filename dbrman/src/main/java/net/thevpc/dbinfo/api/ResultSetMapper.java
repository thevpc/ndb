package net.thevpc.dbinfo.api;

import net.thevpc.dbinfo.util.SafeResultSet;

public interface ResultSetMapper<T> {
    T get(SafeResultSet rs);
}
