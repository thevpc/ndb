package net.thevpc.nsql.impl;

import net.thevpc.nsql.NSqlRow;
import net.thevpc.nuts.util.NLiteral;

import java.time.Instant;

public abstract class AbstractSqlRow implements NSqlRow {

    public AbstractSqlRow() {
    }

    @Override
    public Long asLong() {
        return getLong(1);
    }

    @Override
    public String asString() {
        return getString(1);
    }

    @Override
    public Object[] asArray() {
        Object[] row = new Object[getColumnsCount()];
        for (int i = 0; i < row.length; i++) {
            row[i] = getObject(i + 1);
        }
        return row;
    }

    @Override
    public Integer getInt(int index) {
        Number n = (Number) getObject(index);
        if (n instanceof Integer) {
            return (Integer) n;
        }
        return n == null ? null : n.intValue();
    }

    @Override
    public Boolean getBoolean(int index) {
        Object n = getObject(index);
        if (n instanceof Boolean) {
            return (Boolean) n;
        }
        return n == null ? null : NLiteral.of(n).asBoolean().orElseThrow(() -> new IllegalArgumentException("unsupported boolean type from " + n.getClass()));
    }

    @Override
    public Boolean getBoolean(String name) {
        Object n = getObject(name);
        if (n instanceof Boolean) {
            return (Boolean) n;
        }
        return n == null ? null : NLiteral.of(n).asBoolean().orElseThrow(() -> new IllegalArgumentException("unsupported boolean type from " + n.getClass()));
    }

    @Override
    public Instant getInstant(int index) {
        Object n = getObject(index);
        if (n == null) {
            return null;
        }
        return NLiteral.of(n).asInstant().orElseThrow(() -> new IllegalArgumentException("unsupported instant type from " + n.getClass()));
    }

    @Override
    public Long getLong(int index) {
        Number n = (Number) getObject(index);
        if (n instanceof Long) {
            return (Long) n;
        }
        return n == null ? null : n.longValue();
    }

    @Override
    public Double getDouble(int index) {
        Number n = (Number) getObject(index);
        if (n instanceof Double) {
            return (Double) n;
        }
        return n == null ? null : n.doubleValue();
    }

    @Override
    public String getString(int index) {
        return (String) getObject(index);
    }

    @Override
    public Integer getInt(String name) {
        Number n = (Number) getObject(name);
        if (n instanceof Integer) {
            return (Integer) n;
        }
        return n == null ? null : n.intValue();
    }

    @Override
    public Instant getInstant(String name) {
        Object n = (Object) getObject(name);
        if (n == null) {
            return null;
        }
        return NLiteral.of(n).asInstant().orElseThrow(() -> new IllegalArgumentException("unsupported instant type from " + n.getClass()));
    }

    @Override
    public Long getLong(String name) {
        Number n = (Number) getObject(name);
        if (n instanceof Long) {
            return (Long) n;
        }
        return n == null ? null : n.longValue();
    }

    @Override
    public Double getDouble(String name) {
        Number n = (Number) getObject(name);
        if (n instanceof Double) {
            return (Double) n;
        }
        return n == null ? null : n.doubleValue();
    }

    @Override
    public String getString(String name) {
        return (String) getObject(name);
    }


}
