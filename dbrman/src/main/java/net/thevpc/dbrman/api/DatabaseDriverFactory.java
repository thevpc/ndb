package net.thevpc.dbrman.api;

import net.thevpc.dbrman.model.CnxInfo;
import net.thevpc.dbrman.model.DbType;
import net.thevpc.dbrman.util.DatabaseDriverFactories;

import java.sql.Connection;

public interface DatabaseDriverFactory {
    static DatabaseDriverFactory of(CnxInfo cnx){
        return () -> DatabaseDriverFactories.createDatabaseDriver(cnx);
    }

    static DatabaseDriverFactory of(String cnx){
        return () -> DatabaseDriverFactories.createDatabaseDriver(CnxInfo.parse(cnx));
    }
    static DatabaseDriverFactory of(Connection db, DbType type) {
        return () -> DatabaseDriverFactories.createDatabaseDriver(db, type);
    }

    static DatabaseDriverFactory of(String url, String login, String pwd, DbType type) {
        return () -> DatabaseDriverFactories.createDatabaseDriver(url, login, pwd, type);
    }

    DatabaseDriver createDatabaseDriver();
}
