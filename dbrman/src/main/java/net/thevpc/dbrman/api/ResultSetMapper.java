package net.thevpc.dbrman.api;

import net.thevpc.dbrman.util.SafeResultSet;

public interface ResultSetMapper<T> {
    T get(SafeResultSet rs);
}
