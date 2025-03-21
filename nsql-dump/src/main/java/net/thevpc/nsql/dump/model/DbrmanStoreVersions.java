package net.thevpc.nsql.dump.model;

import net.thevpc.nsql.dump.util.NSqlDumpModuleInstaller;

public class DbrmanStoreVersions {
    public static final long V1=1;
    static {
        NSqlDumpModuleInstaller.init();
    }
}
