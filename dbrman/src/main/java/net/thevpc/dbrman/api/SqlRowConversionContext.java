package net.thevpc.dbrman.api;

import java.io.File;

public interface SqlRowConversionContext {
    File getLobFolder() ;
    boolean isIgnoreNulls() ;
}
