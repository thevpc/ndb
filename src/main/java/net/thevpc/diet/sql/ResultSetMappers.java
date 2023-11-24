package net.thevpc.diet.sql;

public class ResultSetMappers {
    public static ResultSetMapper<String> ofString(String col){
        return x->x.getString(col);
    }
}
