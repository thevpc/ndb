package net.thevpc.nsql.dump.model;

import net.thevpc.nsql.dump.api.NSqlDump;
import net.thevpc.nsql.dump.util.DatabaseDriverFactories;
import net.thevpc.nsql.dump.util.NSqlDumpModuleInstaller;
import net.thevpc.nsql.NSqlConnectionStringBuilder;
import net.thevpc.nsql.NSqlConnection;
import net.thevpc.lib.nserializer.impl.StoreReader;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class DbStore {
    static {
        NSqlDumpModuleInstaller.init();
    }

    private NSqlDump db;
    private InputStream in;
    private OutputStream out;
    private File file;

    public DbStore() {
    }

    public NSqlDump getDb() {
        return db;
    }

    public DbStore setDb(NSqlConnectionStringBuilder cnx) {
        this.db = DatabaseDriverFactories.createSqlDump(cnx);
        return this;
    }

    public DbStore setDb(NSqlConnection db) {
        this.db = DatabaseDriverFactories.createSqlDump(db);
        return this;
    }

//    public DbStore setDb(String url, String login, String pwd, SqlDialect type) {
//        this.db = DatabaseDriverFactories.createDatabaseDriver(url, login, pwd, type);
//        return this;
//    }

    public DbStore setDb(NSqlDump db) {
        this.db = db;
        return this;
    }

    public InputStream getIn() {
        return in;
    }

    public DbStore setIn(InputStream in) {
        this.in = in;
        return this;
    }

    public OutputStream getOut() {
        return out;
    }

    public DbStore setOut(OutputStream out) {
        this.out = out;
        return this;
    }

    public File getFile() {
        return file;
    }

    public DbStore setOut(File file) {
        this.file = file;
        return this;
    }

    public StoreReader createReader() {
        if (out != null && file != null) {
            throw new IllegalArgumentException("ambiguous input");
        } else if (in != null) {
            return new StoreReader(in);
        } else if (file != null) {
            return new StoreReader(file);
        } else {
            throw new IllegalArgumentException("missing input");
        }
    }


}
