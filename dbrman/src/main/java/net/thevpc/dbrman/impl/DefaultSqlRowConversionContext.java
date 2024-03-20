package net.thevpc.dbrman.impl;

import net.thevpc.dbrman.api.SqlRowConversionContext;

import java.io.File;

public class DefaultSqlRowConversionContext implements SqlRowConversionContext {
    private File lobFolder;
    private boolean ignoreNulls;

    public boolean isIgnoreNulls() {
        return ignoreNulls;
    }

    public DefaultSqlRowConversionContext setIgnoreNulls(boolean ignoreNulls) {
        this.ignoreNulls = ignoreNulls;
        return this;
    }

    public File getLobFolder() {
        return lobFolder;
    }

    public DefaultSqlRowConversionContext setLobFolder(File lobFolder) {
        this.lobFolder = lobFolder;
        return this;
    }
}
