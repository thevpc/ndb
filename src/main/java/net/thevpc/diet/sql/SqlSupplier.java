package net.thevpc.diet.sql;

import java.sql.SQLException;

interface SqlSupplier<A> {
    A get() throws SQLException;
}
