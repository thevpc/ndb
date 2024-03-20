package net.thevpc.dbrman.io;

import net.thevpc.vio2.util.IOUtils;

import java.io.*;

public class Out {
    private File file;
    private OutputStream outputStream;

    public Out(String file) {
        this(new File(file));
    }
    public Out(File file) {
        this.file = file;
    }

    public Out(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public File getFile() {
        return file;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public IOUtils.Creatable<PrintStream> toPrintStream(){
        if (getFile() != null) {
            File mk = getFile().getParentFile();
            if (mk != null) {
                mk.mkdirs();
            }
            try {
                return new IOUtils.Creatable<>(
                        new PrintStream(getFile()),
                        true
                );
            } catch (FileNotFoundException e) {
                throw new UncheckedIOException(e);
            }
        } else if (getOutputStream() != null) {
            if (getOutputStream() instanceof PrintStream) {
                return new IOUtils.Creatable<>((PrintStream) getOutputStream(),false);
            } else {
                return null;
            }
        }else{
            return null;
        }
    }
}
