package net.thevpc.diet.io;

public interface ObjectReader<T> {
    T read(StoreInputStream dos);
}
