package net.thevpc.nsql;

import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NStringUtils;
import net.thevpc.tson.*;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class NSqlTsonUtils {
    public static Map<String, String> deserializeMapStringString(TsonElement e) {
        if (e == null) {
            return null;
        }
        Map<String, String> m = new LinkedHashMap<>();
        if (e.isListContainer()) {
            TsonListContainer r = e.toListContainer();
            TsonElementList body = r.body();
            if (body != null) {
                for (TsonElement bi : body) {
                    switch (bi.type()) {
                        case PAIR: {
                            TsonPair p = (TsonPair) e;
                            String k = stringOf(p.key());
                            m.put(NStringUtils.trim(k), stringOf(p.value()));
                            break;
                        }
                    }
                }
            }
        }
        return m;
    }

    public static Map<String, TsonElement> deserializeMapStringTson(TsonElement e) {
        if (e == null) {
            return null;
        }
        Map<String, TsonElement> m = new LinkedHashMap<>();
        if (e.isListContainer()) {
            TsonListContainer r = e.toListContainer();
            TsonElementList body = r.body();
            if (body != null) {
                for (TsonElement bi : body) {
                    switch (bi.type()) {
                        case PAIR: {
                            TsonPair p = (TsonPair) e;
                            String k = stringOf(p.key());
                            m.put(NStringUtils.trim(k), p.value());
                            break;
                        }
                    }
                }
            }
        }
        return m;
    }

    public static String nameOf(TsonElement e) {
        if (e.isPair() && e.toPair().key().isAnyString()) {
            return e.toPair().key().toStr().value();
        } else if (e.isNamedUplet()) {
            return e.toUplet().name();
        } else if (e.isNamedArray()) {
            return e.toArray().name();
        } else if (e.isNamedObject()) {
            return e.toObject().name();
        } else if (e.isAnyString()) {
            return e.toStr().value();
        }
        return null;
    }

    public static TsonElement fieldByNameOf(String name, TsonElement element) {
        if (element != null) {
            for (TsonElement e : element.toListContainer().body()) {
                if (e.isPair() && e.toPair().key().isAnyString()
                        && NNameFormat.equalsIgnoreFormat(name, e.toPair().key().toStr().value())
                ) {
                    return e.toPair().value();
                } else if (e.isNamedUplet() && NNameFormat.equalsIgnoreFormat(name, e.toUplet().name())) {
                    return e;
                } else if (e.isNamedArray() && NNameFormat.equalsIgnoreFormat(name, e.toArray().name())) {
                    return e;
                } else if (e.isNamedObject() && NNameFormat.equalsIgnoreFormat(name, e.toObject().name())) {
                    return e;
                }
            }
        }
        return null;
    }

    public static Boolean booleanOfOwn(TsonElement e) {
        if (e == null || e.isNull()) {
            return null;
        }
        if (e.isBoolean()) {
            return e.toBoolean().value();
        }
        if (e.isListContainer()) {
            for (TsonElement rr : e.toListContainer().body()) {
                if (rr.isBoolean()) {
                    return rr.toBoolean().value();
                }
            }
        }
        return null;
    }

    public static String stringOfOwn(TsonElement e) {
        if (e == null || e.isNull()) {
            return null;
        }
        if (e.isAnyString()) {
            return e.toStr().value();
        }
        if (e.isListContainer()) {
            for (TsonElement rr : e.toListContainer().body()) {
                if (rr.isAnyString()) {
                    return rr.toStr().value();
                }
            }
        }
        return null;
    }

    public static Boolean booleanOfField(String name, TsonElement e, boolean expectOwn) {
        if (e != null) {
            TsonElement fn = fieldByNameOf(name, e);
            if (fn != null) {
                Boolean b = booleanOfOwn(fn);
                if (b != null) {
                    return b;
                }
            }
            for (TsonElement ee : e.toListContainer().body()) {
                if (ee.isAnyString() && ee.toStr().value().equals(name)) {
                    return true;
                }
            }
            if (expectOwn) {
                return booleanOfOwn(e);
            }
        }
        return null;
    }

    public static String stringOfField(String name, TsonElement e, boolean expectOwn) {
        if (e != null) {
            String b = stringOfOwn(fieldByNameOf(name, e));
            if (b != null) {
                return b;
            }
            if (expectOwn) {
                return stringOfOwn(e);
            }
        }
        return null;
    }

    public static Boolean booleanOf(TsonElement e) {
        if (e == null || e.isNull()) {
            return null;
        }
        return e.toBoolean().value();
    }

    public static String stringOf(TsonElement e) {
        if (e == null || e.isNull()) {
            return null;
        }
        return e.toStr().value();
    }

    public static Map.Entry<String, String> mapEntryStringStringOf(TsonElement e) {
        if (e instanceof TsonPair) {
            TsonPair p = (TsonPair) e;
            String k = stringOf(p.key());
            String v = stringOf(p.value());
            return new AbstractMap.SimpleEntry<>(k, v);
        }
        return null;
    }

    public static TsonListContainer containerOf(TsonElement e) {
        if (e == null) {
            return ((TsonArray) Tson.ofArray().build());
        }
        if (e.isListContainer()) {
            return ((TsonListContainer) e);
        }
        return ((TsonArray) Tson.ofArrayBuilder().add(e).build());
    }

    public static Integer intOf(TsonElement e) {
        if (e == null) {
            return null;
        }
        if (e.isNumber()) {
            return e.toNumber().intValue();
        }
        if (e.type() == TsonElementType.STRING) {
            return NLiteral.of(e.toStr()).asInt().get();
        }
        throw new IllegalArgumentException("not integer type : " + e);
    }
}
