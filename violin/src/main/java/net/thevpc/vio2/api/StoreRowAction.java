package net.thevpc.vio2.api;

public enum StoreRowAction {
    SKIP,
    STOP,
    STOP_AFTER,
    ACCEPT;

    public boolean isAccept() {
        return this == ACCEPT || this == STOP_AFTER;
    }

    public boolean isStop() {
        return this == STOP || this == STOP_AFTER;
    }

}
