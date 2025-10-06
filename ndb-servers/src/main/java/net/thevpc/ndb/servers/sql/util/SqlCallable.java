package net.thevpc.ndb.servers.sql.util;

import net.thevpc.nuts.core.NSession;

public interface SqlCallable<T> {
    T run(SqlHelper h, NSession session) throws Exception;
}
