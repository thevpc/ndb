package net.thevpc.diet.io;

public interface ObjectWriter<T> {
    void write(T value, StoreOutputStream dos);
}
