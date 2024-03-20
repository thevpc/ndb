/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.vio2.api;

import net.thevpc.vio2.model.DefaultStoreValue;
import net.thevpc.vio2.model.StoreFieldDefinition;
import net.thevpc.vio2.model.StoreValue;

import java.io.*;
import java.nio.file.Path;

/**
 * @author vpc
 */
public interface IoCell extends Closeable {
    boolean isLob();

    void writeLob(File file);

    void writeLob(Path file);

    Object getObject();

    StoreFieldDefinition getDefinition();

    IoCell repeatable();

    default StoreValue getValue() {
        Object o = getObject();
        return DefaultStoreValue.ofAny(getDefinition().getStoreType(), o);
    }

    @Override
    void close();

    default void consume() {
        Object o = getObject();
        if (o != null) {
            if (o instanceof InputStream) {
                try (InputStream is = (InputStream) o) {
                    byte[] buf = new byte[2048];
                    int c;
                    while (true) {
                        c = is.read(buf);
                        if (c <= 0) {
                            break;
                        }
                    }
                    return;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            if (o instanceof Reader) {
                try (Reader is = (Reader) o) {
                    char[] buf = new char[2048];
                    int c;
                    while ((c = is.read(buf)) > 0) {
                        //
                    }
                    return;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            if (o instanceof Closeable) {
                try {
                    ((Closeable) o).close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
    }
}
