package net.thevpc.vio2.api;

public interface ObjectReader<T> {
    T read(StoreInputStream dos);
}
