package net.thevpc.nsql;

import java.io.File;

public interface NSqlRowConversionContext {
    File getLobFolder() ;
    boolean isIgnoreNulls() ;
}
