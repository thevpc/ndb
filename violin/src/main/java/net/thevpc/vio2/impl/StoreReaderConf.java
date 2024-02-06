package net.thevpc.vio2.impl;

import java.util.LinkedHashMap;
import java.util.Map;

public class StoreReaderConf {
    private static final Map<Long, Sers> verSer = new LinkedHashMap<>();

    public static Sers get(long versionNumber) {
        return verSer.computeIfAbsent(versionNumber, x -> new Sers());
    }
}
