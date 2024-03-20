package net.thevpc.vio2.impl;


import net.thevpc.vio2.api.IoCell;
import net.thevpc.vio2.model.StoreFieldDefinition;

import java.io.*;
import java.nio.file.*;
import java.util.function.Supplier;

public class RepeatableReadIoCell extends AbstractIoCell {
    private StoreFieldDefinition md;
    private Supplier<Object> supplier;
    private File tempFile;
    private boolean lob;
    private String strValue;

    public RepeatableReadIoCell(IoCell cell) {
        md = cell.getDefinition();
        Object o = cell.getObject();
        this.lob = isLobObject(o);
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
                strValue=tempFile.toString();
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
                strValue=tempFile.toString();
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
            strValue=String.valueOf(o);
            supplier = () -> o;
        }
    }

    @Override
    public StoreFieldDefinition getDefinition() {
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

    public void writeLob(File file) {
        writeLob(file.toPath());
    }

    public void writeLob(Path file) {
        if (!isLob()) {
            throw new IllegalArgumentException("Not a lob");
        }
        Path p = file.getParent();
        if (p != null) {
            p.toFile().mkdirs();
        }
        try {
            Files.copy(tempFile.toPath(), file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean isLob() {
        return lob;
    }

    @Override
    public IoCell repeatable() {
        return this;
    }

    @Override
    public String toString() {
        return String.valueOf(strValue);
    }
}
