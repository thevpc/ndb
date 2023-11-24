package net.thevpc.diet.io;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class Sers {
    private Map<Class, ObjectWriter> outputs = new LinkedHashMap<>();
    private Map<Class, ObjectReader> inputs = new LinkedHashMap<>();

    public Sers() {
    }

    public <T> Optional<ObjectWriter<T>> getObjectWriter(Class<T> clz) {
        ObjectWriter<T> o = outputs.get(clz);
        if (o == null) {
            return Optional.empty();
        }
        return Optional.of(o);
    }

    public <T> Optional<ObjectReader<T>> getObjectReader(Class<T> clz) {
        ObjectReader<T> o = inputs.get(clz);
        if (o == null) {
            return Optional.empty();
        }
        return Optional.of(o);
    }

    public <T> void addWriterAndReader(Class<T> clz, ObjectSerializer<T> w) {
        outputs.put(clz, w);
        inputs.put(clz, w);
    }

    public <T> void addWriter(Class<T> clz, ObjectWriter<T> w) {
        outputs.put(clz, w);
    }

    public <T> void addReader(Class<T> clz, ObjectReader<T> w) {
        inputs.put(clz, w);
    }
}
