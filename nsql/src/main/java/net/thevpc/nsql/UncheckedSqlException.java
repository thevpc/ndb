package net.thevpc.nsql;

import java.sql.SQLException;

public class UncheckedSqlException extends RuntimeException{
    public UncheckedSqlException(SQLException cause) {
        super(cause);
    }
}
