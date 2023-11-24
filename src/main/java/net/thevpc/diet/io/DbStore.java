package net.thevpc.diet.io;

import net.thevpc.diet.io.v1.StoreWriterV1;
import net.thevpc.diet.sql.CnxInfo;
import net.thevpc.diet.sql.DatabaseDriver;
import net.thevpc.diet.sql.DbHelperFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;

public class DbStore {
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
        this.db = DbHelperFactory.create(cnx);
        return this;
    }

    public DbStore setDb(Connection db) {
        this.db = DbHelperFactory.create(db);
        return this;
    }

    public DbStore setDb(String url, String login, String pwd) {
        this.db = DbHelperFactory.create(url, login, pwd);
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
        }else if (in != null) {
            return new StoreReader(in);
        } else if (file != null) {
            return new StoreReader(file);
        } else {
            throw new IllegalArgumentException("missing input");
        }
    }

    public StoreWriter createWriter() {
        DatabaseDriver h = getDb();
        if (h == null) {
            throw new IllegalArgumentException("missing db");
        }
        if (out != null && file != null) {
            throw new IllegalArgumentException("ambiguous output");
        } else if (out != null) {
            return new StoreWriterV1(out, h);
        } else if (file != null) {
            return new StoreWriterV1(file, h);
        } else {
            throw new IllegalArgumentException("missing output");
        }
    }
}
