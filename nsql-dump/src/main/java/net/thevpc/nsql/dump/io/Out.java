package net.thevpc.nsql.dump.io;

import net.thevpc.nuts.util.NCreated;

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

    public NCreated<PrintStream> toPrintStream(){
        if (getFile() != null) {
            File mk = getFile().getParentFile();
            if (mk != null) {
                mk.mkdirs();
            }
            try {
                return NCreated.ofNew(
                        new PrintStream(getFile())
                );
            } catch (FileNotFoundException e) {
                throw new UncheckedIOException(e);
            }
        } else if (getOutputStream() != null) {
            if (getOutputStream() instanceof PrintStream) {
                return NCreated.ofExisting((PrintStream) getOutputStream());
            } else {
                return null;
            }
        }else{
            return null;
        }
    }
}
