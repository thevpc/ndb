package net.thevpc.nsql.dump;

import net.thevpc.nuts.util.NMsg;

public interface DumpProgressEvent {
    long getRowCount();

    int getTableIndex();

    String getTableName();

    long getRowIndex();

    DumpProgressEventType getEventType();

    double getProgress();

    NMsg getMessage();
}
