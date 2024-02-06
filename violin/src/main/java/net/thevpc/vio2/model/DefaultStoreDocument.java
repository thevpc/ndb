package net.thevpc.vio2.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public class DefaultStoreDocument implements StoreDocument {
    private LinkedHashMap<String, StoreDocumentEntry> entries = new LinkedHashMap<>();

    @Override
    public void put(StoreDocumentEntry entry) {
        entries.put(entry.getName(), entry);
    }

    @Override
    public Optional<StoreValue> get(String name) {
        StoreDocumentEntry u = entries.get(name);
        if(u!=null){
            return Optional.of(u.getValue());
        }
        return Optional.empty();
    }

    @Override
    public void put(String name, StoreValue value) {
        entries.put(name, new DefaultStoreDocumentEntry(name, value));
    }

    @Override
    public List<StoreDocumentEntry> entries() {
        return new ArrayList<>(entries.values());
    }

    @Override
    public int size() {
        return entries.size();
    }
}
