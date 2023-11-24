package net.thevpc.diet.io.v1;

import net.thevpc.diet.io.*;
import net.thevpc.diet.model.StoreTableDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.zip.GZIPInputStream;

/**
 * @author vpc
 */
public class StoreReaderV1 implements StoreReaderVX {

    private StoreInputStream dis;
    private Sers sers;

    public StoreReaderV1(InputStream in, StoreInputStream his) {
        sers = new SersV1();
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
                this.dis = new StoreInputStream(new GZIPInputStream(in), sers);
            } else {
                this.dis = new StoreInputStream(in, sers);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void visit(StoreVisitor visitor) {
        while (true) {
            switch (dis.readNonNullableInt()) {
                case DietProtocol.SECTION_SCHEMA: {
                    StoreTableDefinition[] md = dis.readNonNullableStruct(StoreTableDefinition[].class);
                    visitor.visitSchema(md);
                    break;
                }
                case DietProtocol.SECTION_DATA: {
                    StoreRows md = dis.readNonNullableStruct(StoreRows.class);
                    visitor.visitData(md);
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
        this.dis.close();
    }
}
