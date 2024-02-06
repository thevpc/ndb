package net.thevpc.vio2.model;

import java.util.List;
import java.util.Optional;

public interface StoreDocument {
    void put(StoreDocumentEntry entry);

    Optional<StoreValue> get(String name);

    void put(String name, StoreValue value);

    List<StoreDocumentEntry> entries();

    int size();
}
