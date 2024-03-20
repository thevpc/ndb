package net.thevpc.dbrman.impl;

import net.thevpc.dbrman.api.DbItem;
import net.thevpc.vio2.util.StringUtils;

import java.util.Arrays;

public class DefaultDbItem implements DbItem {
    private Type type;

    private Object value;

    private Object source;
    private Object[] parents;

    public DefaultDbItem(Object value, Type type, Object source, Object... parents) {
        this.type = type;
        this.value = value;
        this.source = source;
        this.parents = parents;
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
    public String toString() {
        return "DbItem<" + type + ">{" +
                "value=" + StringUtils.litString(value) +
                ", source=" + source +
                (parents.length == 0 ? "" : (", parents=" + Arrays.toString(parents))) +
                '}';
    }
}
