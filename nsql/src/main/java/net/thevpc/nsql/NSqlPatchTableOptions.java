package net.thevpc.nsql;

public class NSqlPatchTableOptions {
    private boolean createColumn;
    private boolean dropColumn;
    private boolean dropTable;

    public boolean isCreateColumn() {
        return createColumn;
    }

    public NSqlPatchTableOptions setCreateColumn(boolean createColumn) {
        this.createColumn = createColumn;
        return this;
    }

    public boolean isDropColumn() {
        return dropColumn;
    }

    public NSqlPatchTableOptions setDropColumn(boolean dropColumn) {
        this.dropColumn = dropColumn;
        return this;
    }

    public boolean isDropTable() {
        return dropTable;
    }

    public NSqlPatchTableOptions setDropTable(boolean dropTable) {
        this.dropTable = dropTable;
        return this;
    }
}
