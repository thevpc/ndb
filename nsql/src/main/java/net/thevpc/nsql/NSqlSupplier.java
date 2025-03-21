package net.thevpc.nsql;

import java.sql.SQLException;

public interface NSqlSupplier<A> {
    A get() throws SQLException;
}
