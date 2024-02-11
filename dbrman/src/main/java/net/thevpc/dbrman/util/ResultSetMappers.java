package net.thevpc.dbrman.util;

import net.thevpc.dbrman.api.ResultSetMapper;

public class ResultSetMappers {
    public static ResultSetMapper<String> ofString(String col){
        return x->x.getString(col);
    }
}
