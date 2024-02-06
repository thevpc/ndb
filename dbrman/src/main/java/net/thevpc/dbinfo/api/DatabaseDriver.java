/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.dbinfo.api;


import net.thevpc.dbinfo.model.*;
import net.thevpc.dbinfo.options.TableRestoreOptions;
import net.thevpc.vio2.api.IoCell;
import net.thevpc.vio2.api.StoreRows;
import net.thevpc.vio2.model.StoreDataType;


import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;

/**
 * @author vpc
 */
public interface DatabaseDriver extends Closeable {
    static DatabaseDriver of(String cnxInfo) {
        return DatabaseDriverFactory.of(cnxInfo).createDatabaseDriver();
    }

    static DatabaseDriver of(CnxInfo cnxInfo) {
        return DatabaseDriverFactory.of(cnxInfo).createDatabaseDriver();
    }

    static DatabaseDriver of(Connection db, DbType type) {
        return DatabaseDriverFactory.of(db, type).createDatabaseDriver();
    }

    static DatabaseDriver of(String url, String login, String pwd, DbType type) {
        return DatabaseDriverFactory.of(url, login, pwd, type).createDatabaseDriver();
    }

    TableRowsDefinition createRowsDefinition(ResultSetMetaData rs);

    TableDefinition getTableDefinition(TableId name);

    public ColumnDefinition getColumnDefinition(ColumnId name);

    SchemaId getSchemaId();

    Connection getConnection();

    List<TableHeader> getAnyTables();

    List<TableHeader> getAnyTables(CatalogId catalogId);

    List<TableHeader> getAnyTables(SchemaId schemaId);

    boolean isSpecialTable(TableId tableId);

    List<TableHeader> getTableHeaders();

    List<TableHeader> getTableHeaders(SchemaId schemaId);

    List<TableHeader> getTableHeaders(CatalogId catalogId);

    List<TableId> getTableIds();

    List<TableId> getTableIds(SchemaId schemaId);

    List<TableId> getTableIds(CatalogId catalogId);

    List<SchemaHeader> getSchemas(String catalog);

    List<SchemaHeader> getSchemas();

    List<CatalogHeader> getCatalogs();

    StoreRows getTableRows(TableId table);

    IoCell createCell(ResultSet rs, ColumnDefinition column);

    void prepareStatement(PreparedStatement ps, int index, StoreDataType st, Object value);

    void close();

    String escapeIdentifier(TableId name);

    String escapeIdentifier(String name);

    void dropTable(TableId tableId);

    boolean dropTableIfExists(TableId tableId);

    boolean tableConstraintsExists(TableConstraintsId def);

    void createTableConstraints(TableConstraintsDefinition def);

    boolean createTableConstraintsIfNotExists(TableConstraintsDefinition def);

    void dropTableConstraints(TableConstraintsId def);

    boolean dropTableConstraintsIfExists(TableConstraintsId def);

    void createTable(TableDefinition def);

    boolean createTableIfNotExists(TableDefinition def);

    boolean patchTable(TableDefinition def, TableRestoreOptions options);

    boolean patchColumn(ColumnDefinition col);

    void createColumn(ColumnDefinition col);

    boolean addColumnIfNotExists(ColumnDefinition col);

    boolean catalogExists(CatalogId c);

    boolean tableExists(TableId c);

    boolean schemaExists(SchemaId s);

    boolean columnExists(ColumnId c);

    void dropColumn(ColumnId col);

    boolean dropColumnIfExists(ColumnId col);

    void clearTable(TableId t);

    void unuseDatabase();

    void useDatabase(String s);

    boolean databaseExists(String s);

    void createDatabase(String s);

    void dropDatabase(String s);

    boolean dropDatabaseIfExists(String s);

    void importData(StoreRows rows, TableRestoreOptions options);

    String getSqlTypeCode(int sid);

    void enableConstraints(TableDefinition d, TableRestoreOptions schemaMode);

    void disableConstraints(TableDefinition d, TableRestoreOptions schemaMode);

}
