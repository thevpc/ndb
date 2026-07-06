package net.thevpc.nsql.ddl;

import net.thevpc.nsql.NSqlConnection;

import java.io.PrintStream;

public interface NDdlScriptGenerator {
    void generateScript(NSqlConnection conn, PrintStream out);
}
