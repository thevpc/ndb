package net.thevpc.ndb.servers.sql.util;

import net.thevpc.nuts.util.NStringUtils;

public class SqlIndexColumn {

    public String columnName;
    public String type;
    public boolean nonUnique;
    public short ordinalPosition;
    public Boolean asc;
    public long pages;
    public String filterCondition;

    @Override
    public String toString() {
        return String.valueOf(columnName);
    }

    public SqlIndexColumn sort() {
        return this;
    }
}
