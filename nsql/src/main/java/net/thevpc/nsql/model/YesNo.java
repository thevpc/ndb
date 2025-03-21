package net.thevpc.nsql.model;

import java.util.Optional;

public enum YesNo {
    YES(1),
    NO(0),
    UNKNOWN(-1);
    private int id;

    YesNo(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public static Optional<YesNo> ofId(int i) {
        for (YesNo value : values()) {
            if (value.id == i) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
