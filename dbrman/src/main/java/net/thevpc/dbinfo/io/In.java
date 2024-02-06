package net.thevpc.dbinfo.io;

import java.io.File;
import java.io.InputStream;

public class In {
    private File file;
    private InputStream inputStream;

    public In(String file) {
        this(new File(file));
    }
    public In(File file) {
        this.file = file;
    }

    public In(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public File getFile() {
        return file;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
