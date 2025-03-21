package net.thevpc.nsql;

import net.thevpc.nuts.io.*;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.io.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Types;
import java.util.UUID;

public class NLobUtils {

    public static byte[] toLobByteArray(Object value) {
        if (value == null) {
            return null;
        } else {
            if (value instanceof InputStream) {
                return NIOUtils.readBytes(new AutoCloseableInputStream((InputStream) value));
            } else if (value instanceof Reader) {
                return NIOUtils.readBytes(new ReaderInputStream((Reader) value, StandardCharsets.UTF_8.name()));
            } else if (value instanceof byte[]) {
                return ((byte[]) value);
            } else if (value instanceof char[]) {
                return new String((char[]) value).getBytes(StandardCharsets.UTF_8);
            } else if (value instanceof String) {
                return (value.toString().getBytes(StandardCharsets.UTF_8));
            } else if (value instanceof File) {
                return NIOUtils.readBytes((((File) value)));
            } else if (value instanceof Path) {
                return NIOUtils.readBytes((((Path) value)));
            } else {
                throw new IllegalArgumentException("unsupported BLOB type: " + value.getClass());
            }
        }
    }

    public static InputStream toLobInputStream(Object value) {
        if (value == null) {
            return null;
        } else {
            if (value instanceof InputStream) {
                return new AutoCloseableInputStream((InputStream) value);
            } else if (value instanceof Reader) {
                return new ReaderInputStream((Reader) value, StandardCharsets.UTF_8.name());
            } else if (value instanceof byte[]) {
                return new ByteArrayInputStream((byte[]) value);
            } else if (value instanceof char[]) {
                return new ReaderInputStream(new CharArrayReader((char[]) value), StandardCharsets.UTF_8.name());
            } else if (value instanceof String) {
                return new ByteArrayInputStream(value.toString().getBytes(StandardCharsets.UTF_8));
            } else if (value instanceof File) {
                try {
                    return new AutoCloseableInputStream(Files.newInputStream(((File) value).toPath()));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else if (value instanceof Path) {
                try {
                    return new AutoCloseableInputStream(Files.newInputStream((Path) value));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else {
                throw new IllegalArgumentException("unsupported BLOB type: " + value.getClass());
            }
        }
    }

    public static Reader toLobReader(Object value) {
        if (value == null) {
            return null;
        } else {
            if (value instanceof InputStream) {
                return new InputStreamReader(new AutoCloseableInputStream((InputStream) value));
            } else if (value instanceof Reader) {
                return new AutoCloseableReader((Reader) value);
            } else if (value instanceof byte[]) {
                return new InputStreamReader(new ByteArrayInputStream((byte[]) value));
            } else if (value instanceof char[]) {
                return new CharArrayReader((char[]) value);
            } else if (value instanceof String) {
                return new StringReader((String) value);
            } else if (value instanceof File) {
                try {
                    return new AutoCloseableReader(Files.newBufferedReader(((File) value).toPath()));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else if (value instanceof Path) {
                try {
                    return new AutoCloseableReader(Files.newBufferedReader((Path) value));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else {
                throw new IllegalArgumentException("unsupported BLOB type: " + value.getClass());
            }
        }
    }

    public static Object toLobFile(Object object, Path parentFile) {
        if (NLobUtils.isLobObject(object)) {
            if (parentFile == null) {
                parentFile = new File(".").toPath();
            } else {
                parentFile.toFile().mkdirs();
            }
            if (object instanceof InputStream) {
                String id = UUID.randomUUID().toString();
                String path = new File(parentFile.toFile(), id).toString();
                NCp.of().from((InputStream) object).to(new File(path)).run();
                return renameExtensionByMimetype(new File(path), true,false).toPath();
            } else if (object instanceof Reader) {
                String id = UUID.randomUUID().toString();
                String path = new File(parentFile.toFile(), id).toString();
                NCp.of().from((Reader) object).to(new File(path)).run();
                return renameExtensionByMimetype(new File(path), true,false).toPath();
            } else {
                return object;
            }
        }
        return object;
    }

    //    public static Object toLobFile(Object object, File parentFile) {
//        if (NLobUtils.isLobObject(object)) {
//            if (parentFile == null) {
//                parentFile = new File(".");
//            } else {
//                parentFile.mkdirs();
//            }
//            if (object instanceof InputStream) {
//                String id = UUID.randomUUID().toString();
//                String path = new File(parentFile, id).toString();
//                NIOUtils.copy((InputStream) object, new File(path).toPath());
//                return new File(path);
//            } else if (object instanceof Reader) {
//                String id = UUID.randomUUID().toString();
//                String path = new File(parentFile, id).toString();
//                NIOUtils.copy((Reader) object, new File(path).toPath());
//                return new File(path);
//            } else {
//                return object;
//            }
//        }
//        return object;
//    }
    public static boolean isLobPointer(Object object) {
        return object instanceof File || object instanceof Path;
    }

    public static Object toLobFile(Object object) {
        return toLobFile(object, new File(".").toPath());
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

    public static Object repeatable(Object value) {
        return toLobFile(value);
    }


    private static String resolveByContent(byte[] b) {
        if (b.length > 4) {
            if (b[0] == 'L' && b[1] == 'E' && b[2] == 'A' && b[3] == 'D') {
                return "image/lead";
            }
        }
        if (b.length > 27) {
            if (
                    new String(b, 0, 9).equals("LTOCXBND\u0018")
                            && new String(b, 24, 4).equals("LEAD")
            ) {
                return "image/lead";
            }
        }
        return null;
    }

    public static String fileMimeByName(File file) {
        String probed = null;
        try {
            String u = Files.probeContentType(file.toPath());
            if (u != null) {
                switch (u) {
                    case "application/data":
                    case "application/octet-stream": {
                        probed = u;
                        break;
                    }
                    case "text/x-devicetree-source": {
                        try {
                            byte[] b = NIOUtils.readBytes(file);
                            String s = resolveByContent(b);
                            if (s != null) {
                                return s;
                            }
                        } catch (Exception ex) {
                            //
                        }
                        return u;
                    }
                    default: {
                        return u;
                    }
                }
            }
        } catch (IOException e) {
            //
        }
        try {
            byte[] b = NIOUtils.readBytes(file);
            String s = resolveByContent(b);
            if (s != null) {
                return s;
            }
        } catch (Exception ex) {
            //
        }
        switch (fileExtension(file.getName())) {
            case ".pdf":
                return "application/pdf";
            case ".png":
                return "image/png";
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            default: {
            }
        }
        return "application/data";
    }

    public static File renameExtensionByMimetype(File file, boolean doIt, boolean onlyIfNoExtension) {
        String extension = fileExtensionByMimetype(file);
        String fileName = file.getName();
        if (extension.length() > 0) {
            int i = fileName.lastIndexOf('.');
            File f2;
            if (i >= 0) {
                f2 = new File(file.getParentFile(), fileName.substring(0, i + 1/*include '.'*/) + extension);
            } else {
                f2 = new File(file.getParentFile(), fileName + "." + extension);
            }
            if (doIt) {
                if(i < 0 || !onlyIfNoExtension) {
                    if (!f2.getName().equals(file.getName())) {
                        file.renameTo(f2);
                        return f2;
                    }
                }
            }
            return file;
        }
        return file;
    }

    public static String fileExtensionByMimetype(File file) {
        String s = fileMimeByName(file);
        switch (s) {
            case "application/pdf":
                return "pdf";
            case "image/png":
                return "png";
            case "image/jpeg":
                return "jpg";
            case "image/lead":
                return "lead";
            case "plain/text":
                return "txt";
            case "application/rtf":
                return "rtf";
            default:
                return "";
        }
    }

    public static String fileMimeByName(String fileName, File file) {
        switch (fileExtension(fileName)) {
            case ".pdf":
                return "application/pdf";
            case ".png":
                return "image/png";
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            default: {
                try {
                    String m = fileMimeByName(file);
                    if (m != null) {
                        return m;
                    }
                } catch (Exception ex) {
                    //
                }
                return "application/data";
            }
        }
    }

    public static String fileExtension(String fileName) {
        if (fileName != null) {
            int i = fileName.lastIndexOf('.');
            if (i >= 0) {
                return fileName.substring(i).toLowerCase();
            }
        }
        return "";
    }

}
