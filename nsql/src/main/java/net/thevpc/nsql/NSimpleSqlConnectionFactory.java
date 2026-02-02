package net.thevpc.nsql;

import net.thevpc.nsql.db.MsSqlServerConnection;
import net.thevpc.nsql.db.PostgreSqlConnection;
import net.thevpc.nuts.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NSimpleSqlConnectionFactory implements NSqlConnectionFactory {
    private Boolean autoCommit = true;
    private NSqlConnectionString params;
    public static final Logger LOG = Logger.getLogger(NSimpleSqlConnectionFactory.class.getName());

    public NSimpleSqlConnectionFactory(NSqlConnectionString params) {
        this.params = params.autoResolve();
    }

    @Override
    public NSqlDialect dialect() {
        return params.getDialect();
    }


    public Boolean getAutoCommit() {
        return autoCommit;
    }

    public NSimpleSqlConnectionFactory setAutoCommit(Boolean autoCommit) {
        this.autoCommit = autoCommit;
        return this;
    }

    @Override
    public NSqlConnection create() {
        NAssert.requireNamedNonBlank(params.getUrl(), "url");
        NAssert.requireNamedNonBlank(params.getDialect(), "dialect");
        NAssert.requireNamedNonBlank(params.getDriverClass(), "driverClassName");
        Connection connection = null;
        LOG.log(Level.FINE, "create connection {0}", params);
        try {
            Class.forName(params.getDriverClass());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            connection = DriverManager.getConnection(params.getUrl(), params.getUsername(), params.getPassword());
            prepareConnection(connection);
            switch (params.getDialect()) {
                case MSSQLSERVER:
                case MSSQLSERVER_JTDS: {
                    return new MsSqlServerConnection(this, connection,false);
                }
                case POSTGRESQL: {
                    return new PostgreSqlConnection(this, connection,false);
                }
            }
            return new NSqlConnection(this, connection, false);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    protected void prepareConnection(Connection connection) {
        try {
            if (autoCommit != null) {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

}
