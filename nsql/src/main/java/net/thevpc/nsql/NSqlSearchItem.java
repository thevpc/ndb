package net.thevpc.nsql;

public interface NSqlSearchItem {
    Type getType() ;

    Object getValue() ;

    Object getSource() ;

    Object[] getParents();

    NSqlSearchItem asNull();

    enum Type {
        CATALOG
        ,SCHEMA
        ,TABLE
        ,TABLE_TYPE
        , COLUMN_NAME
        ,COLUMN_VALUE
        ,COLUMN_TYPE
    }
}
