package net.thevpc.diet.io;

import net.thevpc.diet.model.TableHeader;

public interface DumpFilter {
    boolean acceptTableHeader(TableHeader h);
}
