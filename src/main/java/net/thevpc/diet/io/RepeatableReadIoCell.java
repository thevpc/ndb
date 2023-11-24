package net.thevpc.diet.io;

import net.thevpc.diet.model.StoreColumnDefinition;

import java.io.*;
import java.util.function.Supplier;

public class RepeatableReadIoCell implements IoCell, Closeable {
    private StoreColumnDefinition md;
    private Supplier<Object> supplier;
    private File tempFile;

    public RepeatableReadIoCell(IoCell cell) {
        md = cell.getMetaData();
        Object o = cell.getObject();
        if (o instanceof InputStream) {
            InputStream is = (InputStream) o;
            try {
                tempFile = File.createTempFile("temp", "temp");
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[4092];
                    int c;
                    while ((c = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, c);
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            supplier = () -> {
                try {
                    return new FileInputStream(tempFile);
                } catch (FileNotFoundException e) {
                    throw new UncheckedIOException(e);
                }
            };
        } else if (o instanceof Reader) {
            Reader is = (Reader) o;
            try {
                tempFile = File.createTempFile("temp", "temp");
                try (Writer fos = new FileWriter(tempFile)) {
                    char[] buffer = new char[4092];
                    int c;
                    while ((c = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, c);
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            supplier = () -> {
                try {
                    return new FileInputStream(tempFile);
                } catch (FileNotFoundException e) {
                    throw new UncheckedIOException(e);
                }
            };
        } else {
            supplier = () -> o;
        }
    }

    @Override
    public StoreColumnDefinition getMetaData() {
        return md;
    }

    @Override
    public Object getObject() {
        return supplier.get();
    }

    @Override
    public void close() {
        if (tempFile != null) {
            tempFile.delete();
            tempFile = null;
        }
    }
}
