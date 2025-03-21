package net.thevpc.nsql;

import java.sql.SQLException;

public interface NSqlQueryCaller<V> {
    void eachRow(NSqlQueryCallerContext<V> context) throws SQLException;
}
