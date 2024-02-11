package net.thevpc.vio2.impl;

import java.util.ArrayList;
import java.util.List;

public class IOLoggerHelper {
    private static ThreadLocal<IOLoggerHelper> current = new ThreadLocal<>();
    private List<IOLogger> all = new ArrayList<>();
    private IOLogger curr = IOLogger.NOP;

    public static IOLoggerHelper put(IOLogger e) {
        IOLoggerHelper a=new IOLoggerHelper();
        a.add(e);
        return put(a);
    }

    public static IOLoggerHelper put(IOLoggerHelper e) {
        if (e == null) {
            e = new IOLoggerHelper();
        }
        current.set(e);
        return e;
    }

    public static IOLoggerHelper get() {
        IOLoggerHelper ioLoggerHelper = current.get();
        if (ioLoggerHelper == null) {
            current.set(ioLoggerHelper = new IOLoggerHelper());
        }
        return ioLoggerHelper;
    }

    public IOLoggerHelper add(IOLogger a) {
        if (a != null) {
            all.add(a);
        }
        rebuild();
        return this;
    }

    public IOLoggerHelper clear() {
        all.clear();
        rebuild();
        return this;
    }

    public IOLoggerHelper remove(IOLogger a) {
        if (a != null) {
            all.remove(a);
        }
        rebuild();
        return this;
    }

    public IOLogger current() {
        return curr;
    }

    private void rebuild() {
        if (all.isEmpty()) {
            curr = IOLogger.NOP;
        }
        if (all.size() == 1) {
            curr = all.get(0);
        }
        IOLogger[] _all = all.toArray(new IOLogger[0]);
        curr = new IOLogger() {
            @Override
            public void log(String msg) {
                for (IOLogger i : _all) {
                    i.log(msg);
                }
            }
        };
    }


}
