package net.thevpc.dbrman.api;

public interface DbItem {
    Type getType() ;

    Object getValue() ;

    Object getSource() ;
    Object[] getParents();

    enum Type {
        CATALOG
        ,SCHEMA
        ,TABLE
        ,TABLE_TYPE
        ,COLUMN
        ,COLUMN_VALUE
        ,COLUMN_SQL_TYPE_CODE
        ,COLUMN_STORE_TYPE
    }
}
