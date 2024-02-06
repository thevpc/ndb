package net.thevpc.dbinfo.util;

import net.thevpc.dbinfo.api.ResultSetMapper;

public class ResultSetMappers {
    public static ResultSetMapper<String> ofString(String col){
        return x->x.getString(col);
    }
}
