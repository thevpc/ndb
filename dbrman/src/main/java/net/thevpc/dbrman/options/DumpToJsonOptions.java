package net.thevpc.dbrman.options;

import net.thevpc.dbrman.io.In;
import net.thevpc.dbrman.io.Out;

import java.io.File;
import java.io.PrintStream;

public class DumpToJsonOptions {
    private In in;
    private Out out;
    private File lobFolder;
    private boolean data = true;
    private boolean pretty = false;
    private boolean printSeparators = false;

    public File getLobFolder() {
        return lobFolder;
    }

    public DumpToJsonOptions setLobFolder(File lobFolder) {
        this.lobFolder = lobFolder;
        return this;
    }

    public boolean isPrintSeparators() {
        return printSeparators;
    }

    public DumpToJsonOptions setPrintSeparators(boolean printSeparators) {
        this.printSeparators = printSeparators;
        return this;
    }

    public boolean isPretty() {
        return pretty;
    }

    public DumpToJsonOptions setPretty(boolean pretty) {
        this.pretty = pretty;
        return this;
    }

    public In getIn() {
        return in;
    }

    public DumpToJsonOptions setIn(In in) {
        this.in = in;
        return this;
    }

    public Out getOut() {
        return out;
    }

    public DumpToJsonOptions setOut(Out out) {
        this.out = out;
        return this;
    }

    public boolean isData() {
        return data;
    }

    public DumpToJsonOptions setData(boolean data) {
        this.data = data;
        return this;
    }
}
