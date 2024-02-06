package net.thevpc.dbinfo.io;

import java.io.File;
import java.io.OutputStream;

public class OutputProvider {
    private File file;
    private OutputStream outputStream;

    public OutputProvider(File file) {
        this.file = file;
    }

    public OutputProvider(OutputStream inputStream) {
        this.outputStream = inputStream;
    }

    public File getFile() {
        return file;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }
}
