package net.thevpc.nsql.dump;

public interface DumpProgressMonitor {
    void onEvent(DumpProgressEvent event);
}
