package net.thevpc.nsql.impl;

import net.thevpc.nsql.NSqlRowConversionContext;

import java.io.File;

public class DefaultSqlRowConversionContext implements NSqlRowConversionContext {
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
