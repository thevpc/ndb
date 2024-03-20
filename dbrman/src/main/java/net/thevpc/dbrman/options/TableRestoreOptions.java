package net.thevpc.dbrman.options;

import net.thevpc.vio2.api.StoreProgressMonitor;
import net.thevpc.vio2.api.StoreRowFilter;

import java.io.File;

public class TableRestoreOptions {
    private boolean dropDatabase;
    private boolean createDatabase;
    private boolean dropTable;
    private boolean createTable;
    private boolean createColumn;
    private boolean dropColumn;
    private boolean insertRow;
    private boolean updateRow;
    private boolean clearTable;
    private String database;
    private File lobFolder;

    public File getLobFolder() {
        return lobFolder;
    }

    public TableRestoreOptions setLobFolder(File lobFolder) {
        this.lobFolder = lobFolder;
        return this;
    }

    private StoreProgressMonitor monitor;
    private StoreRowFilter filter;

    public StoreRowFilter getFilter() {
        return filter;
    }

    public TableRestoreOptions setFilter(StoreRowFilter filter) {
        this.filter = filter;
        return this;
    }

    public StoreProgressMonitor getMonitor() {
        return monitor;
    }

    public TableRestoreOptions setMonitor(StoreProgressMonitor monitor) {
        this.monitor = monitor;
        return this;
    }

    public String getDatabase() {
        return database;
    }

    public TableRestoreOptions setDatabase(String database) {
        this.database = database;
        return this;
    }

    public boolean isDropTable() {
        return dropTable;
    }

    public TableRestoreOptions setDropTable(boolean dropTable) {
        this.dropTable = dropTable;
        return this;
    }

    public boolean isCreateTable() {
        return createTable;
    }

    public TableRestoreOptions setCreateTable(boolean createTable) {
        this.createTable = createTable;
        return this;
    }

    public boolean isCreateColumn() {
        return createColumn;
    }

    public TableRestoreOptions setCreateColumn(boolean createColumn) {
        this.createColumn = createColumn;
        return this;
    }

    public boolean isDropColumn() {
        return dropColumn;
    }

    public TableRestoreOptions setDropColumn(boolean dropColumn) {
        this.dropColumn = dropColumn;
        return this;
    }

    public boolean isInsertRow() {
        return insertRow;
    }

    public TableRestoreOptions setInsertRow(boolean insertRow) {
        this.insertRow = insertRow;
        return this;
    }

    public boolean isUpdateRow() {
        return updateRow;
    }

    public TableRestoreOptions setUpdateRow(boolean updateRow) {
        this.updateRow = updateRow;
        return this;
    }

    public boolean isClearTable() {
        return clearTable;
    }

    public TableRestoreOptions setClearTable(boolean clearTable) {
        this.clearTable = clearTable;
        return this;
    }

    public boolean isDropDatabase() {
        return dropDatabase;
    }

    public TableRestoreOptions setDropDatabase(boolean dropDatabase) {
        this.dropDatabase = dropDatabase;
        return this;
    }

    public boolean isCreateDatabase() {
        return createDatabase;
    }

    public TableRestoreOptions setCreateDatabase(boolean createDatabase) {
        this.createDatabase = createDatabase;
        return this;
    }
}
