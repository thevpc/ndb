package net.thevpc.nsql.impl;

import net.thevpc.nsql.NSqlSearchItem;
import net.thevpc.nsql.model.NSqlTableDefinition;
import net.thevpc.nsql.model.NSqlTableRowIndex;
import net.thevpc.nsql.util.WithFullName;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NNameFormat;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class NSqlSearchItemImpl implements NSqlSearchItem {
    private Type type;
    private Object value;
    private Object source;
    private Object[] parents;

    public NSqlSearchItemImpl(Object value, Type type, Object source, Object... parents) {
        this.type = type;
        this.value = value;
        this.source = source;
        this.parents = parents == null ? new Object[0] : parents;
    }

    public Object[] getParents() {
        return parents;
    }

    public Type getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public Object getSource() {
        return source;
    }

    @Override
    public NSqlSearchItem asNull() {
        if (value == null) {
            return this;
        }
        return new NSqlSearchItemImpl(null, type, source, Arrays.stream(parents).map(x -> {
            if (x instanceof NSqlTableRowIndex) {
                return ((NSqlTableRowIndex) x).getDefinition();
            }
            return x;
        }).toArray());
    }

    @Override
    public String toString() {
        String ns = "";
        if (source instanceof WithFullName) {
            ns = ((WithFullName) source).getFullName();
        } else {
            ns = String.valueOf(source);
        }
        Object[] p = Arrays.stream(parents).filter(x -> {
            if (source instanceof WithFullName && x instanceof WithFullName) {
                return false;
            }
            return true;
        }).map(x -> {
            if (x instanceof WithFullName) {
                return ((WithFullName) x).getFullName();
            }
            return x;
        }).toArray();
        return (NNameFormat.CLASS_NAME.format(type.name())) + "(" + ns + (p.length == 0 ? "" : p.length == 1 ? ("/" + p[0]) : ("/" + Arrays.toString(p))) + ")"
                + (value == null ? "" :
                ("={" + NLiteral.of(value) + '}')
        );
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NSqlSearchItemImpl that = (NSqlSearchItemImpl) o;
        return type == that.type && Objects.equals(value, that.value) && Objects.equals(source, that.source) && Objects.deepEquals(parents, that.parents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value, source, Arrays.hashCode(parents));
    }
}
