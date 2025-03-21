package net.thevpc.nsql.dump.api;

import net.thevpc.nsql.NSqlConnectionStringBuilder;
import net.thevpc.nsql.dump.util.DatabaseDriverFactories;
import net.thevpc.nsql.NSqlConnection;
import net.thevpc.nsql.NSqlConnectionString;
import net.thevpc.nsql.NSqlDialect;

public interface NSqlDumpFactory {
    static NSqlDumpFactory of(NSqlConnectionStringBuilder cnx){
        return () -> DatabaseDriverFactories.createSqlDump(cnx);
    }

    static NSqlDumpFactory of(String cnx){
        return () -> DatabaseDriverFactories.createSqlDump(NSqlConnectionStringBuilder.parse(cnx));
    }
    static NSqlDumpFactory of(NSqlConnection db) {
        return () -> DatabaseDriverFactories.createSqlDump(db);
    }

    static NSqlDumpFactory of(String url, String login, String pwd, NSqlDialect type) {
        return () -> DatabaseDriverFactories.createSqlDump(new NSqlConnectionString(url, login, pwd, type,null));
    }

    NSqlDump createDatabaseDriver();
}
