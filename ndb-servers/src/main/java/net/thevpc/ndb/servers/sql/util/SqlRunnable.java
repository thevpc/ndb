package net.thevpc.ndb.servers.sql.util;

import net.thevpc.nuts.NSession;

public interface SqlRunnable {
    void run(SqlHelper h, NSession session) throws Exception;
}
