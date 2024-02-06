package net.thevpc.dbinfo.model;

import net.thevpc.dbinfo.api.DatabaseDriver;
import net.thevpc.dbinfo.util.DatabaseDriverFactories;
import net.thevpc.dbinfo.util.DbInfoModuleInstaller;
import net.thevpc.vio2.impl.StoreReader;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;

public class DbStore {
    static {
        DbInfoModuleInstaller.init();
    }

    private DatabaseDriver db;
    private InputStream in;
    private OutputStream out;
    private File file;

    public DbStore() {
    }

    public DatabaseDriver getDb() {
        return db;
    }

    public DbStore setDb(CnxInfo cnx) {
        this.db = DatabaseDriverFactories.createDatabaseDriver(cnx);
        return this;
    }

    public DbStore setDb(Connection db, DbType type) {
        this.db = DatabaseDriverFactories.createDatabaseDriver(db, type);
        return this;
    }

    public DbStore setDb(String url, String login, String pwd, DbType type) {
        this.db = DatabaseDriverFactories.createDatabaseDriver(url, login, pwd, type);
        return this;
    }

    public DbStore setDb(DatabaseDriver db) {
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
