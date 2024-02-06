package net.thevpc.dbinfo.util;

import net.thevpc.dbinfo.io.In;
import net.thevpc.dbinfo.io.OutputProvider;

public class DbrIoHelper {
    public static In check(In in){
        if(in==null){
            throw new IllegalArgumentException("missing InputProvider");
        }
        if(in.getFile()!=null && in.getInputStream()!=null){
            throw new IllegalArgumentException("ambiguous InputProvider");
        }
        return in;
    }
    public static OutputProvider check(OutputProvider out){
        if(out==null){
            throw new IllegalArgumentException("missing InputProvider");
        }
        if(out.getFile()!=null && out.getOutputStream()!=null){
            throw new IllegalArgumentException("ambiguous OutputProvider");
        }
        return out;
    }
}
