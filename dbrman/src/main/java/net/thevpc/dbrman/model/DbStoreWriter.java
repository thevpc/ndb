package net.thevpc.dbrman.model;

import net.thevpc.dbrman.api.DatabaseDriver;
import net.thevpc.dbrman.store.DatabaseDriverAsModel;
import net.thevpc.vio2.impl.StoreWriterImpl;

import java.io.File;
import java.io.OutputStream;

public class DbStoreWriter extends StoreWriterImpl {
    public DbStoreWriter(OutputStream out, DatabaseDriver h, long version) {
        super(out, new DatabaseDriverAsModel(h), version);
    }

    public DbStoreWriter(File out, DatabaseDriver h, long version) {
        super(out, new DatabaseDriverAsModel(h), version);
    }
    public DbStoreWriter(OutputStream out, DatabaseDriver h) {
        this(out, h,DbrmanStoreVersions.V1);
    }

    public DbStoreWriter(File out, DatabaseDriver h) {
        this(out, h,DbrmanStoreVersions.V1);
    }
}
