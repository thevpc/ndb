package net.thevpc.nsql.dump.model;

import net.thevpc.nsql.dump.api.NSqlDump;
import net.thevpc.nsql.dump.store.DatabaseDriverAsModel;
import net.thevpc.nsql.dump.util.NSqlDumpModuleInstaller;
import net.thevpc.lib.nserializer.impl.StoreWriterImpl;

import java.io.File;
import java.io.OutputStream;

public class DbStoreWriter extends StoreWriterImpl {
    static {
        NSqlDumpModuleInstaller.init();
    }
    public DbStoreWriter(OutputStream out, NSqlDump h, long version) {
        super(out, new DatabaseDriverAsModel(h), version);
    }

    public DbStoreWriter(File out, NSqlDump h, long version) {
        super(out, new DatabaseDriverAsModel(h), version);
    }
    public DbStoreWriter(OutputStream out, NSqlDump h) {
        this(out, h,DbrmanStoreVersions.V1);
    }

    public DbStoreWriter(File out, NSqlDump h) {
        this(out, h,DbrmanStoreVersions.V1);
    }
}
