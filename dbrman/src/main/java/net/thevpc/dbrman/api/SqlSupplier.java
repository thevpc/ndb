package net.thevpc.dbrman.api;

import java.sql.SQLException;

public interface SqlSupplier<A> {
    A get() throws SQLException;
}
