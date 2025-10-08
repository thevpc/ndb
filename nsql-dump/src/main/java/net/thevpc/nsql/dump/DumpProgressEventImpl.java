package net.thevpc.nsql.dump;

import net.thevpc.nuts.text.NMsg;

public class DumpProgressEventImpl implements DumpProgressEvent, Cloneable{
    private double progress;
    private NMsg message;
    private String tableName;
    private long rowIndex;
    private long rowCount;
    private int tableIndex;
    private DumpProgressEventType eventType;

    @Override
    public long getRowCount() {
        return rowCount;
    }

    public DumpProgressEventImpl setRowCount(long rowCount) {
        this.rowCount = rowCount;
        return this;
    }

    @Override
    public int getTableIndex() {
        return tableIndex;
    }

    public DumpProgressEventImpl setTableIndex(int tableIndex) {
        this.tableIndex = tableIndex;
        return this;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    public DumpProgressEventImpl setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    @Override
    public long getRowIndex() {
        return rowIndex;
    }

    public DumpProgressEventImpl setRowIndex(long rowIndex) {
        this.rowIndex = rowIndex;
        return this;
    }

    @Override
    public DumpProgressEventType getEventType() {
        return eventType;
    }

    public DumpProgressEventImpl setEventType(DumpProgressEventType eventType) {
        this.eventType = eventType;
        return this;
    }

    @Override
    public double getProgress() {
        return progress;
    }

    public DumpProgressEventImpl setProgress(double progress) {
        this.progress = progress;
        return this;
    }

    @Override
    public NMsg getMessage() {
        return message;
    }

    public DumpProgressEventImpl setMessage(NMsg message) {
        this.message = message;
        return this;
    }

    @Override
    protected DumpProgressEventImpl clone() {
        try {
            return (DumpProgressEventImpl) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
    public DumpProgressEventImpl copy(){
        return clone();
    }
}
