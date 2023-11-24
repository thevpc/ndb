/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.diet.sql;

import net.thevpc.diet.io.IoCell;
import net.thevpc.diet.io.StoreRows;
import net.thevpc.diet.model.*;

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

    public StoreRowsDefinition createRowsDefinition(ResultSetMetaData rs);

    StoreTableDefinition getTableMetaData(TableId name);

    Connection getConnection();

    List<TableHeader> getTables();

    StoreRows getTableRows(TableId table);

    public IoCell createCell(ResultSet rs, StoreColumnDefinition column);

    void prepareStatement(PreparedStatement ps, int index, StoreDataType st, Object value);

    void close();

    TableImporter createTableImporter();

    String escapeIdentifier(TableId name);

    String escapeIdentifier(String name);

    void dropTable(TableId tableId);

    void createTableConstraints(StoreTableDefinition def);

    void dropTableConstraints(StoreTableDefinition def);

    void createTable(StoreTableDefinition def);

    void createColumn(StoreColumnDefinition col);

    boolean catalogExists(String c);
    boolean schemaExists(String c,String s);

    void dropColumn(StoreColumnDefinition col);

    void clearTable(TableId t);

    void unuseDatabase();

    void useDatabase(String s);

    boolean databaseExists(String s);

    void createDatabase(String s);

    void dropDatabase(String s);

    String getSqlTypeCode(int sid);
}
