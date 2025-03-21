package net.thevpc.nsql;

import java.sql.SQLException;

public interface NSqlQueryRunner<T> {
    void eachRow(NSqlQueryRunnerContext<T> context) throws SQLException;
}
