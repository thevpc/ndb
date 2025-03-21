package net.thevpc.nsql;

import java.sql.SQLException;

public interface NSqlUpdateCaller<T,V> {
    void run(NSqlUpdateCallerContext<T,V> context) throws SQLException;
}
