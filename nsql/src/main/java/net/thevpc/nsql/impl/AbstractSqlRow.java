package net.thevpc.nsql.impl;

import net.thevpc.nsql.NSqlRow;

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



}
