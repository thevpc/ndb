package net.thevpc.nsql;

import net.thevpc.nuts.io.NIOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NSqlClasspathLoader {
    private String prefix = "/sql/";
    private String suffix = ".sql";
    private ClassLoader classLoader;
    public Map<String, String> queries = new HashMap<>();

    public NSqlClasspathLoader() {
    }

    public NSqlClasspathLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public String getQuery(String query) {
        String queryPath = query;
        if (prefix != null && prefix.length() > 0) {
            String prefix1 = prefix;
            if (prefix1.startsWith("/")) {
                prefix1 = prefix1.substring(1);
            }
            if (prefix1.endsWith("/") && queryPath.startsWith("/")) {
                queryPath = prefix1.substring(0, prefix1.length() - 1) + queryPath;
            } else if (!prefix1.endsWith("/") && !queryPath.startsWith("/")) {
                queryPath = prefix1 + "/" + queryPath;
            } else if (!prefix1.endsWith("/") || !queryPath.startsWith("/")) {
                queryPath = prefix1 + queryPath;
            }
        } else {
            if (query.startsWith("/")) {
                query = query.substring(1);
            }
        }
        if (suffix != null && suffix.length() > 0 && !query.endsWith(suffix)) {
            queryPath = queryPath + suffix;
        }
        return queries.computeIfAbsent(
                queryPath, s -> {
                    ClassLoader cl = classLoader;
                    if (cl == null) {
                        cl = Thread.currentThread().getContextClassLoader();
                    }

                    URL u = cl.getResource(s);
                    if (u == null) {
                        throw new IllegalArgumentException("resource '" + s + "' not found");
                    }
                    try (InputStream is = u.openStream()) {
                        return new String(NIOUtils.readBytes(is));
                    } catch (IOException ex) {
                        throw new IllegalArgumentException("resource '" + s + "' could not be read");
                    }
                }
        );
    }

    public String getPrefix() {
        return prefix;
    }

    public NSqlClasspathLoader setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String getSuffix() {
        return suffix;
    }

    public NSqlClasspathLoader setSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }
}
