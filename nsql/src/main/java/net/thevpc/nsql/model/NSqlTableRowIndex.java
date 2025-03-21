package net.thevpc.nsql.model;

public class NSqlTableRowIndex {
    private long row;
    private NSqlTableDefinition definition;

    public NSqlTableRowIndex(long row, NSqlTableDefinition definition) {
        this.row = row;
        this.definition = definition;
    }

    public long getRow() {
        return row;
    }

    public NSqlTableDefinition getDefinition() {
        return definition;
    }

    @Override
    public String toString() {
        return definition.getTableId().getFullName() + "[row=" + row + "]";
    }
}
