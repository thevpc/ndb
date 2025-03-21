package net.thevpc.nsql.dump.util;

import net.thevpc.nsql.dump.io.In;
import net.thevpc.nsql.dump.io.OutputProvider;

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
