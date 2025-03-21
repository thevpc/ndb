package net.thevpc.nsql;

import java.nio.file.Path;

public interface NPrepareStatementContext {
    boolean isExternalLob();
    Path getExternalLobFolder();
}
