package net.thevpc.nsql;

import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NStringUtils;
import net.thevpc.nuts.elem.*;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NSqlTsonUtils {
    public static Map<String, String> deserializeMapStringString(NElement e) {
        if (e == null) {
            return null;
        }
        Map<String, String> m = new LinkedHashMap<>();
        if (e.isListContainer()) {
            NListContainerElement r = e.asListContainer().get();
            List<NElement> body = r.children();
            if (body != null) {
                for (NElement bi : body) {
                    switch (bi.type()) {
                        case PAIR: {
                            NPairElement p = (NPairElement) e;
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

    public static Map<String, NElement> deserializeMapStringTson(NElement e) {
        if (e == null) {
            return null;
        }
        Map<String, NElement> m = new LinkedHashMap<>();
        if (e.isListContainer()) {
            NListContainerElement r = e.asListContainer().get();
            List<NElement> body = r.children();
            if (body != null) {
                for (NElement bi : body) {
                    switch (bi.type()) {
                        case PAIR: {
                            NPairElement p = (NPairElement) e;
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

    public static String nameOf(NElement e) {
        if (e.isPair() && e.asPair().get().key().isAnyString()) {
            return e.asPair().get().key().asStringValue().get();
        } else if (e.isNamedUplet()) {
            return e.asUplet().get().name().orNull();
        } else if (e.isNamedArray()) {
            return e.asArray().get().name().orNull();
        } else if (e.isNamedObject()) {
            return e.asObject().get().name().orNull();
        } else if (e.isAnyString()) {
            return e.asStringValue().get();
        }
        return null;
    }

    public static NElement fieldByNameOf(String name, NElement element) {
        if (element != null) {
            for (NElement e : element.toListContainer().get().children()) {
                if (e.isPair() && e.asPair().get().key().isAnyString()
                        && NNameFormat.equalsIgnoreFormat(name, e.asPair().get().key().asStringValue().get())
                ) {
                    return e.toNamedPair().get().value();
                } else if (e.isNamedUplet() && NNameFormat.equalsIgnoreFormat(name, e.asUplet().get().name().orNull())) {
                    return e;
                } else if (e.isNamedArray() && NNameFormat.equalsIgnoreFormat(name, e.asArray().get().name().orNull())) {
                    return e;
                } else if (e.isNamedObject() && NNameFormat.equalsIgnoreFormat(name, e.asObject().get().name().orNull())) {
                    return e;
                }
            }
        }
        return null;
    }

    public static Boolean booleanOfOwn(NElement e) {
        if (e == null || e.isNull()) {
            return null;
        }
        if (e.isBoolean()) {
            return e.asBooleanValue().isEmpty();
        }
        if (e.isListContainer()) {
            for (NElement rr : e.toListContainer().get().children()) {
                if (rr.isBoolean()) {
                    return rr.asBooleanValue().get();
                }
            }
        }
        return null;
    }

    public static String stringOfOwn(NElement e) {
        if (e == null || e.isNull()) {
            return null;
        }
        if (e.isAnyString()) {
            return e.asStringValue().get();
        }
        if (e.isListContainer()) {
            for (NElement rr : e.toListContainer().get().children()) {
                if (rr.isAnyString()) {
                    return rr.asStringValue().get();
                }
            }
        }
        return null;
    }

    public static Boolean booleanOfField(String name, NElement e, boolean expectOwn) {
        if (e != null) {
            NElement fn = fieldByNameOf(name, e);
            if (fn != null) {
                Boolean b = booleanOfOwn(fn);
                if (b != null) {
                    return b;
                }
            }
            for (NElement ee : e.toListContainer().get().children()) {
                if (ee.isAnyString() && ee.asStringValue().get().equals(name)) {
                    return true;
                }
            }
            if (expectOwn) {
                return booleanOfOwn(e);
            }
        }
        return null;
    }

    public static String stringOfField(String name, NElement e, boolean expectOwn) {
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

    public static Boolean booleanOf(NElement e) {
        if (e == null || e.isNull()) {
            return null;
        }
        return e.asBooleanValue().get();
    }

    public static String stringOf(NElement e) {
        if (e == null || e.isNull()) {
            return null;
        }
        return e.asStringValue().get();
    }

    public static Map.Entry<String, String> mapEntryStringStringOf(NElement e) {
        if (e instanceof NPairElement) {
            NPairElement p = (NPairElement) e;
            String k = stringOf(p.key());
            String v = stringOf(p.value());
            return new AbstractMap.SimpleEntry<>(k, v);
        }
        return null;
    }

    public static NListContainerElement containerOf(NElement e) {
        if (e == null) {
            return NElements.ofArray();
        }
        if (e.isListContainer()) {
            return ((NListContainerElement) e);
        }
        return NElements.ofArray(e);
    }

    public static Integer intOf(NElement e) {
        if (e == null) {
            return null;
        }
        if (e.isNumber()) {
            return e.asNumberValue().get().intValue();
        }
        if (e.type().isString()) {
            return NLiteral.of(e.asStringValue().get()).asInt().get();
        }
        throw new IllegalArgumentException("not integer type : " + e);
    }
}
