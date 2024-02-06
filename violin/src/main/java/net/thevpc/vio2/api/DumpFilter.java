package net.thevpc.vio2.api;

import net.thevpc.vio2.model.StoreStructHeader;

public interface DumpFilter {
    boolean acceptTableHeader(StoreStructHeader h);
}
