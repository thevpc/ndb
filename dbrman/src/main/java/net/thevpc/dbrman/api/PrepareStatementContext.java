package net.thevpc.dbrman.api;

import java.nio.file.Path;

public interface PrepareStatementContext {
    boolean isExternalLob();
    Path getExternalLobFolder();
}
