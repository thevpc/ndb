package net.thevpc.nsql.util;

import java.lang.reflect.InvocationTargetException;

public class ReflectionHelper {
    public static RuntimeException asRuntimeException(Throwable e) {
        if(e==null){
            return null;
        }
        if(e instanceof RuntimeException){
            return (RuntimeException)e;
        }
        if(e instanceof InvocationTargetException){
            InvocationTargetException ee = (InvocationTargetException) e;
            Throwable t = ee.getTargetException();
            if(t!=null){
                return asRuntimeException(t);
            }
            return new RuntimeException(e);
        }
        return new RuntimeException(e);
    }
}
