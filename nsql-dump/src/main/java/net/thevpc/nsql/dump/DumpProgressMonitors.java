package net.thevpc.nsql.dump;

public class DumpProgressMonitors {
    public static final DumpProgressMonitor SILENT=new DumpProgressMonitor() {
        @Override
        public void onEvent(DumpProgressEvent event) {
        }
    };
}
