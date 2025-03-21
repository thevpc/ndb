package net.thevpc.ndb.servers.sql.util;

public class SqlPrimaryKey {

    public String columnName;
    public short keySeq;
    public String pkName;

    @Override
    public String toString() {
        return String.valueOf(columnName);
    }
}
