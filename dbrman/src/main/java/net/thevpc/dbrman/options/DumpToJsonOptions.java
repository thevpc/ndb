package net.thevpc.dbrman.options;

import net.thevpc.dbrman.io.In;

import java.io.PrintStream;

public class DumpToJsonOptions {
    private In in;
    private PrintStream out;
    private boolean data = true;
    private boolean pretty = false;
    private boolean printSeparators = false;

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

    public PrintStream getOut() {
        return out;
    }

    public DumpToJsonOptions setOut(PrintStream out) {
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
