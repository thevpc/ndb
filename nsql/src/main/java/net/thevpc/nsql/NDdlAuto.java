package net.thevpc.nsql;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NNameFormat;

public enum NDdlAuto {
    CREATE, UPDATE, DROP_CREATE, NONE;

    public static NDdlAuto parse(String any) {
        if (NBlankable.isBlank(any)) {
            return null;
        }
        switch (NNameFormat.LOWER_KEBAB_CASE.format(any.trim())) {
            case "create":
                return CREATE;
            case "update":
                return UPDATE;
            case "none":
                return NONE;
            case "drop-create":
            case "drop":
            case "recreate":
                return DROP_CREATE;
        }
        throw new IllegalArgumentException("unsupported DdlMode: " + any);
    }
}
