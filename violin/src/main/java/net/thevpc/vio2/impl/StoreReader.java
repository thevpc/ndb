package net.thevpc.vio2.impl;

import net.thevpc.vio2.api.StoreInputStream;
import net.thevpc.vio2.api.StoreRows;
import net.thevpc.vio2.api.StoreVisitor;
import net.thevpc.vio2.model.StoreStructDefinition;

import java.io.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * @author vpc
 */
public class StoreReader implements Closeable {
    public static Logger LOG = Logger.getLogger(StoreReader.class.getName());

    private InputStream in;
    private boolean closeIn;
    private Sers sers;
    private StoreInputStream dis;

    public StoreReader(File in) {
        closeIn = true;
        try {
            LOG.log(Level.FINE, "reading from {0} ...",in);
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
        StoreInputStream his = new StoreInputStreamImpl(in, new Sers());
        long burst = his.readNonNullableLong();
        if (burst != DietProtocol.BURST) {
            throw new UncheckedIOException(new IOException("invalid format"));
        }
        long version = his.readNonNullableLong();
        Sers sers = StoreReaderConf.get(version);
        try {
            long time = his.readNonNullableLong();
            boolean compress = his.readNonNullableBoolean();
            String comProtocol = null;
            if (compress) {
                comProtocol = his.readUTF();
                if (!"gzip".equals(comProtocol)) {
                    throw new IllegalArgumentException("invalid protocol");
                }
            }
            if (compress) {
                this.dis = new StoreInputStreamImpl(new GZIPInputStream(in), sers);
            } else {
                this.dis = new StoreInputStreamImpl(in, sers);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void visit(StoreVisitor visitor) {
        while (true) {
            switch (dis.readNonNullableInt()) {
                case DietProtocol.SECTION_SCHEMA: {
                    StoreStructDefinition[] md = dis.readNonNullableStruct(StoreStructDefinition[].class);
                    visitor.visitSchema(md==null?null: Arrays.asList(md));
                    break;
                }
                case DietProtocol.SECTION_DATA: {
                    try(StoreRows md = dis.readNonNullableStruct(StoreRows.class)) {
                        visitor.visitData(md);
                    }catch (RuntimeException ex){
                        ex.printStackTrace();
                    }
                    break;
                }
                case DietProtocol.STORE_END: {
                    visitor.visitEnd();
                    return;
                }
                default:
                    throw new AssertionError();
            }
        }
    }

    @Override
    public void close() {
        try {
            this.dis.close();
            if (closeIn) {
                this.in.close();
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

}
