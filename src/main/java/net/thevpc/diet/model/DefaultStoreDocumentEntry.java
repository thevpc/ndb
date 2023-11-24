package net.thevpc.diet.model;

public class DefaultStoreDocumentEntry implements StoreDocumentEntry{
    private String name;
    private StoreValue value;

    public DefaultStoreDocumentEntry(String name, StoreValue value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public StoreValue getValue() {
        return value;
    }
}
