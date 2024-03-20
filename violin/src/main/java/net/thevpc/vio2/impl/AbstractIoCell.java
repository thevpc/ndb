package net.thevpc.vio2.impl;

import net.thevpc.vio2.api.IoCell;
import net.thevpc.vio2.util.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;
import java.util.UUID;

public abstract class AbstractIoCell implements IoCell {
    @Override
    public void close() {

    }

    @Override
    public IoCell repeatable() {
        return new RepeatableReadIoCell(this);
    }

    public void writeLob(File file) {
        throw new IllegalArgumentException("not supported yet");
    }

    @Override
    public void writeLob(Path file) {
        throw new IllegalArgumentException("not supported yet");
    }

    public static Object toLobFile(Object object, Path parentFile) {
        if (AbstractIoCell.isLobObject(object)) {
            if (parentFile == null) {
                parentFile = new File(".").toPath();
            } else {
                parentFile.toFile().mkdirs();
            }
            if (object instanceof InputStream) {
                String id = UUID.randomUUID().toString();
                String path = new File(parentFile.toFile(), id).toString();
                IOUtils.copy((InputStream) object, new File(path).toPath());
                return new File(path).toPath();
            } else if (object instanceof Reader) {
                String id = UUID.randomUUID().toString();
                String path = new File(parentFile.toFile(), id).toString();
                IOUtils.copy((Reader) object, new File(path).toPath());
                return new File(path).toPath();
            } else {
                return object;
            }
        }
        return object;
    }
    public static boolean isLobPointer(Object object) {
        return object instanceof File || object instanceof Path;
    }

    public static Object toLobFile(Object object, File parentFile) {
        if (AbstractIoCell.isLobObject(object)) {
            if (parentFile == null) {
                parentFile = new File(".");
            } else {
                parentFile.mkdirs();
            }
            if (object instanceof InputStream) {
                String id = UUID.randomUUID().toString();
                String path = new File(parentFile, id).toString();
                IOUtils.copy((InputStream) object, new File(path).toPath());
                return new File(path);
            } else if (object instanceof Reader) {
                String id = UUID.randomUUID().toString();
                String path = new File(parentFile, id).toString();
                IOUtils.copy((Reader) object, new File(path).toPath());
                return new File(path);
            } else {
                return object;
            }
        }
        return object;
    }

    public static boolean isLobObject(Object o) {
        if (o != null) {
            if (o instanceof InputStream) {
                return true;
            }
            if (o instanceof Reader) {
                return true;
            }
        }
        return false;
    }
}
