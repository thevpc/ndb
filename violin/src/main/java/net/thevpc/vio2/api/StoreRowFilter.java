package net.thevpc.vio2.api;

public interface StoreRowFilter {
    StoreRowAction accept(IoRow row, long index);
}
