package net.thevpc.vio2.api;

public interface ObjectWriter<T> {
    void write(T value, StoreOutputStream dos);
}
