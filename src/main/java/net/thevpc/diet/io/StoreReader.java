package net.thevpc.diet.io;

import net.thevpc.diet.io.v1.StoreReaderV1;

import java.io.*;

/**
 * @author vpc
 */
public class StoreReader implements Closeable {

    private StoreReaderVX vv;
    private InputStream in;
    private boolean closeIn;

    public StoreReader(File in) {
        closeIn = true;
        try {
            init(new FileInputStream(in));
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    public StoreReader(InputStream in) {
        init(in);
    }

    private void init(InputStream in) {
        this.in = in;
        StoreInputStream his = new StoreInputStream(in, new Sers());
        long burst = his.readNonNullableLong();
        if (burst != DietProtocol.BURST) {
            throw new UncheckedIOException(new IOException("invalid format"));
        }
        long version = his.readNonNullableLong();
        if (version == DietProtocol.V1) {
            vv = new StoreReaderV1(in, his);
        } else {
            throw new UncheckedIOException(new IOException("invalid format"));
        }
    }

    public void visit(StoreVisitor visitor) {
        vv.visit(visitor);
    }

    @Override
    public void close() {
        try {
            this.vv.close();
            if (closeIn) {
                this.in.close();
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

}
