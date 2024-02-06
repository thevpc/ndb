/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.dbinfo.common;

import net.thevpc.dbinfo.options.TableRestoreOptions;
import net.thevpc.dbinfo.util.DbInfoModuleInstaller;
import net.thevpc.dbinfo.api.DatabaseDriver;
import net.thevpc.dbinfo.api.SqlSupplier;
import net.thevpc.dbinfo.model.*;
import net.thevpc.dbinfo.util.ResultSetMappers;
import net.thevpc.dbinfo.util.SafeResultSet;
import net.thevpc.dbinfo.util.UncheckedSQLException;
import net.thevpc.vio2.api.IoCell;
import net.thevpc.vio2.api.IoRow;
import net.thevpc.vio2.api.StoreRows;
import net.thevpc.vio2.impl.RepeatableReadIoCell;
import net.thevpc.vio2.impl.RepeatableReadIoCellArr;
import net.thevpc.vio2.model.StoreDataType;
import net.thevpc.vio2.model.StoreFieldDefinition;
import net.thevpc.vio2.model.YesNo;
import net.thevpc.vio2.util.NumberUtils;
import net.thevpc.vio2.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author vpc
 */
public abstract class AbstractDatabaseDriver implements DatabaseDriver {
    static {
        DbInfoModuleInstaller.init();
    }

    public static Logger LOG = Logger.getLogger(AbstractDatabaseDriver.class.getName());
    private Connection connection;
    private long maxVarcharLength;


    public AbstractDatabaseDriver(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public SchemaId getSchemaId() {
        try {
            return new SchemaId(
                    getConnection().getCatalog(),
                    getConnection().getSchema()
            );
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    public TableRowsDefinition createRowsDefinition(ResultSetMetaData rs) {
        try {
            TableRowsDefinition c = new TableRowsDefinition();
            int columnCount;
            columnCount = rs.getColumnCount();
            ColumnDefinition[] allCols = new ColumnDefinition[columnCount];
            for (int i = 0; i < allCols.length; i++) {
                allCols[i] = createColumnDefinition(rs, i + 1);
            }
            c.setColumns(allCols);
            return c;
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
    }

    public ColumnDefinition createColumnDefinition(ResultSetMetaData rs, int index) {
        ColumnDefinition c = resolveDefaultColumnDefinition(rs, index);
        c.setStoreType(createFileColType(c));
        return c;
    }

    protected StoreDataType createFileColType(ColumnDefinition c) {
        return createDefaultFileColType(c);
    }

    private int _getRsIntByName(ResultSet rs, String name) {
        try {
            return rs.getInt(name);
        } catch (SQLException e) {
            return 0;
        }
    }

    private short _getRsShortByName(ResultSet rs, String name) {
        try {
            return rs.getShort(name);
        } catch (SQLException e) {
            return 0;
        }
    }

    private String _getRsStringByName(ResultSet rs, String name) {
        try {
            return rs.getString(name);
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public ColumnDefinition getColumnDefinition(ColumnId name) {
        LOG.log(Level.FINEST, "[" + name.getFullName() + "] load column metadata");
        List<ColumnDefinition> a = loadColumDefinitions(
                name.getCatalogName(),
                name.getSchemaName(),
                name.getTableName(),
                name.getColumnName()
        );
        if(!a.isEmpty()){
            return a.get(0);
        }
        a= loadColumDefinitions(
                name.getCatalogName(),
                name.getSchemaName(),
                name.getTableName(),
                null
        ).stream().filter(x -> Objects.equals(x.getColumnName(), name.getColumnName())).collect(Collectors.toList());
        if(!a.isEmpty()){
            return a.get(0);
        }
        return null;
    }

    private List<ColumnDefinition> loadColumDefinitions(String catalog, String schemaPattern,
                                                        String tableNamePattern, String columnNamePattern) {
        List<ColumnDefinition> columns = new ArrayList<>();
        DatabaseMetaData md = null;
        try {
            md = connection.getMetaData();
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
        try (ResultSet rs = md.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern)) {
            while (rs.next()) {
                String tableName = _getRsStringByName(rs, "TABLE_NAME");
                String tableCat = _getRsStringByName(rs, "TABLE_CAT");
                String tableSchem = _getRsStringByName(rs, "TABLE_SCHEM");
                if (tableCat == null) {
                    tableCat = catalog;
                }
                if (tableSchem == null) {
                    tableSchem = schemaPattern;
                }
                TableId name = new TableId(catalog, schemaPattern, tableNamePattern);
                TableId table = new TableId(tableCat, tableSchem, tableName);
                if (table.equals(name)) {
                    ColumnDefinition cmd = loadColumDefinition(rs);
                    columns.add(cmd);
                }
            }
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
        return columns;
    }

    private ColumnDefinition loadColumDefinition(ResultSet rs) {
        String columnName = _getRsStringByName(rs, "COLUMN_NAME");
        ColumnDefinition cmd = new ColumnDefinition();
        cmd.setColumnName(columnName);
        cmd.setIndex(_getRsIntByName(rs, "ORDINAL_POSITION"));
        cmd.setSqlType(_getRsIntByName(rs, "DATA_TYPE"));
        cmd.setSqlTypeCode(getSqlTypeCode(cmd.getSqlType()));
        cmd.setSqlTypeName(_getRsStringByName(rs, "TYPE_NAME"));
        cmd.setScale(_getRsIntByName(rs, "COLUMN_SIZE"));
        cmd.setPrecision(_getRsIntByName(rs, "DECIMAL_DIGITS"));
        cmd.setRadix(_getRsIntByName(rs, "NUM_PREC_RADIX"));
        int NULLABLE = _getRsIntByName(rs, "NULLABLE");
        String IS_NULLABLE = _getRsStringByName(rs, "IS_NULLABLE");
        if (NULLABLE == DatabaseMetaData.columnNoNulls) {
            cmd.setNullable(YesNo.NO);
        } else if (NULLABLE == DatabaseMetaData.columnNullable) {
            cmd.setNullable(YesNo.YES);
        } else {
            cmd.setNullable(YesNo.UNKNOWN);
        }
        cmd.setColumnDef(_getRsStringByName(rs, "COLUMN_DEF"));
        cmd.setSchemaName(_getRsStringByName(rs, "SCOPE_SCHEMA"));
        cmd.setCatalogName(_getRsStringByName(rs, "SCOPE_CATALOG"));
        cmd.setTableName(_getRsStringByName(rs, "SCOPE_TABLE"));
        cmd.setSourceDataType(_getRsIntByName(rs, "SOURCE_DATA_TYPE"));
        cmd.setSourceDataTypeCode(getSqlTypeCode(cmd.getSourceDataType()));
        String IS_AUTOINCREMENT = _getRsStringByName(rs, "IS_AUTOINCREMENT");
        cmd.setAutoIncrement(
                "YES".equalsIgnoreCase(IS_AUTOINCREMENT) ? YesNo.YES :
                        "NO".equalsIgnoreCase(IS_AUTOINCREMENT) ? YesNo.NO :
                                YesNo.UNKNOWN
        );
        String IS_GENERATEDCOLUMN = _getRsStringByName(rs, "IS_GENERATEDCOLUMN");
        cmd.setGeneratedColumns(
                "YES".equalsIgnoreCase(IS_GENERATEDCOLUMN) ? YesNo.YES :
                        "NO".equalsIgnoreCase(IS_GENERATEDCOLUMN) ? YesNo.NO :
                                YesNo.UNKNOWN
        );
        cmd.setStoreType(createFileColType(cmd));
        return cmd;
    }

    @Override
    public TableDefinition getTableDefinition(TableId name) {
        LOG.log(Level.FINEST, "[" + name.getFullName() + "] load table metadata");
        TableDefinition tmd = null;
        try {
            DatabaseMetaData md = connection.getMetaData();
            try (ResultSet rs = md.getTables(name.getCatalogName(), name.getSchemaName(), name.getTableName(), null)) {
                while (rs.next()) {
                    String tableCat = _getRsStringByName(rs, "TABLE_CAT");
                    if (tableCat == null) {
                        tableCat = connection.getCatalog();
                    }
                    String tableSchem = _getRsStringByName(rs, "TABLE_SCHEM");
                    if (tableSchem == null) {
                        tableSchem = getSchema();
                    }
                    String tableName = _getRsStringByName(rs, "TABLE_NAME");
                    TableId table = new TableId(tableCat, tableSchem, tableName);
                    if (table.equals(name)) {
                        tmd = new TableDefinition();
                        tmd.setTableName(tableName);
                        tmd.setSchemaName(tableSchem);
                        tmd.setCatalogName(tableCat);
                        tmd.setTableType(_getRsStringByName(rs, "TABLE_TYPE"));
                        tmd.setSelfReferencingColName(StringUtils.trimToNull(_getRsStringByName(rs, "SELF_REFERENCING_COL_NAME")));
                        tmd.setRefGeneration(StringUtils.trimToNull(_getRsStringByName(rs, "REF_GENERATION")));
                        break;
                    }
                }
            }
            Map<String, ColumnDefinition> columns = new LinkedHashMap<String, ColumnDefinition>();
            if (tmd != null) {
                for (ColumnDefinition cmd : loadColumDefinitions(tmd.getCatalogName(), tmd.getSchemaName(), tmd.getTableName(), null)) {
                    columns.put(cmd.getColumnName(), cmd);
                }
                tmd.setColumns(columns.values().toArray(new ColumnDefinition[0]));
                try (ResultSet rs = md.getExportedKeys(connection.getCatalog(), getSchema(), name.getTableName())) {
                    while (rs.next()) {
                        String PKTABLE_CAT = _getRsStringByName(rs, "PKTABLE_CAT");
                        String PKTABLE_SCHEM = _getRsStringByName(rs, "PKTABLE_SCHEM");
                        String PKTABLE_NAME = _getRsStringByName(rs, "PKTABLE_NAME");
                        String PKCOLUMN_NAME = _getRsStringByName(rs, "PKCOLUMN_NAME");
                        String FKTABLE_CAT = _getRsStringByName(rs, "FKTABLE_CAT");
                        String FKTABLE_SCHEM = _getRsStringByName(rs, "FKTABLE_SCHEM");
                        String FKTABLE_NAME = _getRsStringByName(rs, "FKTABLE_NAME");
                        String FKCOLUMN_NAME = _getRsStringByName(rs, "FKCOLUMN_NAME");

                        if (PKTABLE_CAT == null) {
                            PKTABLE_CAT = connection.getCatalog();
                        }
                        if (FKTABLE_CAT == null) {
                            FKTABLE_CAT = connection.getCatalog();
                        }
                        if (PKTABLE_SCHEM == null) {
                            PKTABLE_SCHEM = getSchema();
                        }
                        if (FKTABLE_SCHEM == null) {
                            FKTABLE_SCHEM = getSchema();
                        }

                        int KEY_SEQ = _getRsIntByName(rs, "KEY_SEQ");
                        short UPDATE_RULE = _getRsShortByName(rs, "UPDATE_RULE");
                        short DELETE_RULE = _getRsShortByName(rs, "UPDATE_RULE");
                        TableId tid1 = new TableId(PKTABLE_CAT, PKTABLE_SCHEM, PKTABLE_NAME);
                        TableId tid2 = new TableId(FKTABLE_CAT, FKTABLE_SCHEM, FKTABLE_NAME);
                        if (tid1.equals(name)) {
                            ColumnDefinition c = tmd.getColumns().stream()
                                    .filter(x -> x.getColumnName().equals(PKCOLUMN_NAME))
                                    .findAny().get();
                            c.setPk(true);
                            c.setPkIndex(KEY_SEQ);
                        } else if (tid2.equals(name)) {
                            ColumnDefinition c = tmd.getColumns().stream()
                                    .filter(x -> x.getColumnName().equals(FKCOLUMN_NAME))
                                    .findAny().get();
                            c.setFk(true);
                        }
                    }
                }
            }
            return tmd;
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
    }

    @Override
    public List<SchemaHeader> getSchemas(String catalog) {
        return getSchemas()
                .stream().filter(
                        x -> Objects.equals(x.getCatalogName(), catalog)
                ).collect(Collectors.toList());
    }

    @Override
    public List<SchemaHeader> getSchemas() {
        ArrayList<SchemaHeader> all = new ArrayList<>();
        try (ResultSet rs = connection.getMetaData().getSchemas()) {
            while (rs.next()) {
                String cat = _getRsStringByName(rs, "TABLE_CATALOG");
                if (cat == null) {
                    cat = connection.getCatalog();
                }
                String schem = _getRsStringByName(rs, "TABLE_SCHEM");
                if (schem == null) {
                    schem = getSchema();
                }
                all.add(new SchemaHeader(
                        cat,
                        schem
                ));
            }
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
        return all;
    }

    @Override
    public List<CatalogHeader> getCatalogs() {
        ArrayList<CatalogHeader> all = new ArrayList<>();
        try (ResultSet rs = connection.getMetaData().getCatalogs()) {
            while (rs.next()) {
                String cat = _getRsStringByName(rs, "TABLE_CAT");
                if (cat == null) {
                    cat = connection.getCatalog();
                }
                all.add(new CatalogHeader(
                        cat
                ));
            }
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
        return all;
    }

    @Override
    public List<TableHeader> getAnyTables(CatalogId catalogId) {
        return getAnyTables(catalogId == null ? null : new SchemaId(catalogId.getCatalogName(), null));
    }


    @Override
    public List<TableHeader> getTableHeaders(SchemaId schemaId) {
        return getAnyTables(schemaId).stream().filter(TableHeader::isTable).collect(Collectors.toList());
    }

    @Override
    public List<TableHeader> getTableHeaders(CatalogId catalogId) {
        return getAnyTables(catalogId).stream().filter(TableHeader::isTable).collect(Collectors.toList());
    }

    @Override
    public List<TableId> getTableIds(SchemaId schemaId) {
        return getAnyTables(schemaId).stream().filter(TableHeader::isTable).map(TableHeader::toTableId).collect(Collectors.toList());
    }

    @Override
    public List<TableId> getTableIds(CatalogId catalogId) {
        return getAnyTables(catalogId).stream().filter(TableHeader::isTable).map(TableHeader::toTableId).collect(Collectors.toList());
    }

    @Override
    public List<TableHeader> getTableHeaders() {
        return getAnyTables().stream().filter(TableHeader::isTable).collect(Collectors.toList());
    }

    @Override
    public boolean isSpecialTable(TableId tableId) {
        return false;
    }

    @Override
    public List<TableId> getTableIds() {
        return getAnyTables().stream().filter(TableHeader::isTable).map(TableHeader::toTableId).collect(Collectors.toList());
    }

    @Override
    public List<TableHeader> getAnyTables(SchemaId schemaId) {
        ArrayList<TableHeader> all = new ArrayList<TableHeader>();
        try (ResultSet rs = connection.getMetaData().getTables(schemaId == null ? null : schemaId.getCatalogName(),
                schemaId == null ? null : schemaId.getSchemaName(),
                null, null)) {
            while (rs.next()) {
                String cat = _getRsStringByName(rs, "TABLE_CAT");
                if (cat == null) {
                    cat = connection.getCatalog();
                }
                String schem = _getRsStringByName(rs, "TABLE_SCHEM");
                if (schem == null) {
                    schem = getSchema();
                }
                all.add(new TableHeader(
                        cat,
                        schem,
                        _getRsStringByName(rs, "TABLE_NAME"),
                        _getRsStringByName(rs, "TABLE_TYPE")
                ));
            }
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
        return all;
    }

    @Override
    public List<TableHeader> getAnyTables() {
        return getAnyTables(new SchemaId(getCatalog(), getSchema()));
    }

    private String getCatalog() {
        try {
            return connection.getCatalog();
        } catch (java.lang.AbstractMethodError e) {
            return null;
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    private String getSchema() {
        try {
            return connection.getSchema();
        } catch (java.lang.AbstractMethodError e) {
            return null;
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    @Override
    public StoreRows getTableRows(TableId table) {
        TableDefinition tableMetaData = getTableDefinition(table);
        long startTime = System.currentTimeMillis();
        LOG.log(Level.FINEST, "[" + table.getFullName() + "] reading from DB... ");
        ResultSet rs = getTableResultSet(table);
        long endTime = System.currentTimeMillis();
        LOG.log(Level.FINEST, "[" + table.getFullName() + "] read in " + (endTime - startTime) + "ms... ");
        return new StoreRows() {
            TableRowsDefinition t;

            {
                ResultSetMetaData md;
                try {
                    md = rs.getMetaData();
                } catch (SQLException ex) {
                    throw new UncheckedSQLException(ex);
                }
                t = createRowsDefinition(md);
                t.setCatalogName(table.getCatalogName());
                t.setSchemaName(table.getSchemaName());
                t.setTableName(table.getTableName());
            }

            @Override
            public TableDefinition getDefinition() {
                return tableMetaData;
            }

            @Override
            public IoRow nextRow() {
                try {
                    if (rs.next()) {
                        List<ColumnDefinition> columns = tableMetaData.getColumns();
                        return new IoRowFromResultSet(AbstractDatabaseDriver.this, columns, rs, tableMetaData);
                    } else {
                        return null;
                    }
                } catch (SQLException ex) {
                    throw new UncheckedSQLException(ex);
                }
            }

            @Override
            public void close() {
                try {
                    rs.close();
                } catch (SQLException e) {
                    throw new UncheckedIOException(new IOException(e));
                }
            }
        };
    }

    public String escapeIdentifier(TableId name) {
        return escapeIdentifier(name.getTableName());
    }

    public String escapeIdentifier(String name) {
        return "\"" + name + "\"";
    }

    public ResultSet getTableResultSet(TableId table) {
        try {
            String sql = "Select * from " + escapeIdentifier(table);
            LOG.log(Level.FINEST, "[" + table.getFullName() + "] [SQL] " + sql);
            return connection.createStatement().executeQuery(sql);
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
    }

    @Override
    public void clearTable(TableId t) {
        runDDL(t, "delete from " + escapeIdentifier(t));
    }

    public String resolveSqlTypeDefinition(ColumnDefinition def) {
        switch (def.getStoreType()) {
            case STRING:
            case NSTRING: {
                if (getMaxVarcharLength() > 0) {
                    if (def.getScale() > getMaxVarcharLength()) {
                        def = def.copy();
                        def.setStoreType(StoreDataType.NCHAR_STREAM);
                        return resolveSqlTypeDefinition(def);
                    }
                }
                return "VARCHAR(" + def.getScale() + ")";
            }
            case BIG_DECIMAL:
            case BIG_INT:
            case NBIG_DECIMAL:
            case NBIG_INT: {
                return "NUMERIC(" + def.getScale() + "," + def.getPrecision() + ")";
            }
            case INT:
            case NINT: {
                return "INT";
            }
            case LONG:
            case NLONG: {
                return "LONG";
            }
            case FLOAT:
            case NFLOAT: {
                return "FLOAT";
            }
            case DOUBLE:
            case NDOUBLE: {
                return "DOUBLE";
            }
            case BOOLEAN:
            case NBOOLEAN: {
                return "BOOLEAN";
            }
            case SHORT:
            case NSHORT: {
                return "SHORT";
            }
            case BYTE:
            case NBYTE: {
                return "BYTE";
            }
            case DATE:
            case NDATE: {
                return "DATE";
            }
            case TIMESTAMP:
            case NTIMESTAMP: {
                return "TIMESTAMP";
            }
            case TIME:
            case NTIME: {
                return "TIME";
            }
            case JAVA_OBJECT:
            case NJAVA_OBJECT: {
                def = def.copy();
                def.setStoreType(StoreDataType.BYTES);
                return resolveSqlTypeDefinition(def);
            }
            case BYTES:
            case NBYTES: {
                return "BLOB";
            }
            case CHAR_STREAM:
            case NCHAR_STREAM: {
                return "CLOB";
            }
            case BYTE_STREAM:
            case NBYTE_STREAM: {
                return "BLOB";
            }
            case DOCUMENT:
            case NDOCUMENT:
            case NULL:
            default: {
                throw new UncheckedSQLException(new SQLException("unsupported " + def.getStoreType()));
            }
        }
    }

    public String resolveSqlColumnDefinition(ColumnDefinition def) {
        return escapeIdentifier(def.getColumnName()) + " " + resolveSqlTypeDefinition(def);
    }

    @Override
    public void createTable(TableDefinition def) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ");
        sql.append(escapeIdentifier(def.getTableName()));
        sql.append("(");
        List<ColumnDefinition> columns = def.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            ColumnDefinition column = columns.get(i);
            if (i > 0) {
                sql.append(",");
            }
            sql.append(resolveSqlColumnDefinition(column));
        }
        sql.append(")");
        runDDL(sql.toString());
    }

    public void createDatabase(String s) {
        runDDL("create database " + escapeIdentifier(s));
    }


    @Override
    public String getSqlTypeCode(int id) {
        switch (id) {
            case Types.BIT:
                return "BIT";
            case Types.TINYINT:
                return "TINYINT";
            case Types.SMALLINT:
                return "SMALLINT";
            case Types.INTEGER:
                return "INTEGER";
            case Types.BIGINT:
                return "BIGINT";
            case Types.FLOAT:
                return "FLOAT";
            case Types.REAL:
                return "REAL";
            case Types.DOUBLE:
                return "DOUBLE";
            case Types.NUMERIC:
                return "NUMERIC";
            case Types.DECIMAL:
                return "DECIMAL";
            case Types.CHAR:
                return "CHAR";
            case Types.VARCHAR:
                return "VARCHAR";
            case Types.LONGVARCHAR:
                return "LONGVARCHAR";
            case Types.DATE:
                return "DATE";
            case Types.TIME:
                return "TIME";
            case Types.TIMESTAMP:
                return "TIMESTAMP";
            case Types.BINARY:
                return "BINARY";
            case Types.VARBINARY:
                return "VARBINARY";
            case Types.LONGVARBINARY:
                return "LONGVARBINARY";
            case Types.NULL:
                return "NULL";
            case Types.OTHER:
                return "OTHER";
            case Types.JAVA_OBJECT:
                return "JAVA_OBJECT";
            case Types.DISTINCT:
                return "DISTINCT";
            case Types.STRUCT:
                return "STRUCT";
            case Types.ARRAY:
                return "ARRAY";
            case Types.BLOB:
                return "BLOB";
            case Types.CLOB:
                return "CLOB";
            case Types.REF:
                return "REF";
            case Types.DATALINK:
                return "DATALINK";
            case Types.BOOLEAN:
                return "BOOLEAN";
            case Types.ROWID:
                return "ROWID";
            case Types.NCHAR:
                return "NCHAR";
            case Types.NVARCHAR:
                return "NVARCHAR";
            case Types.LONGNVARCHAR:
                return "LONGNVARCHAR";
            case Types.NCLOB:
                return "NCLOB";
            case Types.SQLXML:
                return "SQLXML";

            case Types.REF_CURSOR:
                return "REF_CURSOR";

            case Types.TIME_WITH_TIMEZONE:
                return "TIME_WITH_TIMEZONE";
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return "TIMESTAMP_WITH_TIMEZONE";

        }
        return null;
    }

    public void dropDatabase(String s) {
        runDDL("drop database " + s);
    }

    @Override
    public boolean databaseExists(String s) {
        unuseDatabase();
        try {
            useDatabase(s);
        } catch (Exception ex) {
            return false;
        }
        unuseDatabase();
        return true;
    }

    @Override
    public void useDatabase(String s) {
        runDDL("use database " + escapeIdentifier(s));
    }

    @Override
    public void unuseDatabase() {
        useDatabase(getDefaultDatabaseName());
    }

    protected String getDefaultDatabaseName() {
        return "postgres";
    }

    protected void runDDL(String sql) {
        LOG.log(Level.FINEST, "[SQL] " + sql);
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    protected void runDDL(TableId table, String sql) {
        LOG.log(Level.FINEST, "[" + table + "] [SQL] " + sql);
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    @Override
    public void dropTable(TableId tableId) {
        runDDL(tableId, "drop table " + escapeIdentifier(tableId));
    }


    @Override
    public void dropColumn(ColumnId col) {
        TableId table = col.getTableId();
        runDDL(table,
                "alter table " +
                        escapeIdentifier(table)
                        + " drop column "
                        + escapeIdentifier(col.getColumnName())
        );
    }

    @Override
    public boolean dropColumnIfExists(ColumnId col) {
        if (columnExists(col)) {
            dropColumn(col);
            return true;
        }
        return false;
    }

    @Override
    public boolean columnExists(ColumnId c) {
        return getColumnDefinition(c)!=null;
    }

    @Override
    public IoCell createCell(ResultSet rs, ColumnDefinition column) {
        return new DefaultResultSetIoCell(column, rs);
    }


    public void prepareStatement(PreparedStatement ps, int index, StoreFieldDefinition st, Object value) {
        this.prepareStatement(ps, index, st.getStoreType(), value);
    }

    public void prepareStatement(PreparedStatement ps, int index, StoreDataType st, Object value) {
        try {
            switch (st) {
                case NULL: {
                    ps.setNull(index, Types.OTHER);
                    break;
                }
                case BOOLEAN:
                case NBOOLEAN: {
                    if (value == null) {
                        ps.setNull(index, Types.BIT);
                    } else {
                        ps.setBoolean(index, (Boolean) value);
                    }
                    break;
                }
                case BYTE:
                case NBYTE: {
                    if (value == null) {
                        ps.setNull(index, Types.INTEGER);
                    } else {
                        ps.setByte(index, ((Byte) value));
                    }
                    break;
                }
                case SHORT:
                case NSHORT: {
                    if (value == null) {
                        ps.setNull(index, Types.INTEGER);
                    } else {
                        ps.setShort(index, ((Short) value));
                    }
                    break;
                }
                case INT:
                case NINT: {
                    if (value == null) {
                        ps.setNull(index, Types.INTEGER);
                    } else {
                        ps.setInt(index, ((Integer) value));
                    }
                    break;
                }
                case LONG:
                case NLONG: {
                    if (value == null) {
                        ps.setNull(index, Types.NUMERIC);
                    } else {
                        if (value instanceof BigDecimal) {
                            System.out.println("there is a problem!!");
                        }
                        ps.setLong(index, ((Number) value).longValue());
                    }
                    break;
                }
                case DATE:
                case NDATE: {
                    if (value == null) {
                        ps.setNull(index, Types.DATE);
                    } else {
                        ps.setDate(index, (Date) value);
                    }
                    break;
                }
                case TIME:
                case NTIME: {
                    if (value == null) {
                        ps.setNull(index, Types.TIME);
                    } else {
                        ps.setTime(index, (Time) value);
                    }
                    break;
                }
                case TIMESTAMP:
                case NTIMESTAMP: {
                    if (value == null) {
                        ps.setNull(index, Types.TIME);
                    } else {
                        ps.setTimestamp(index, (Timestamp) value);
                    }
                    break;
                }
                case BIG_INT:
                case NBIG_INT: {
                    if (value == null) {
                        ps.setNull(index, Types.BIGINT);
                    } else {
                        ps.setBigDecimal(index, NumberUtils.asBigDecimal(((Number) value)));
                    }
                    break;
                }
                case BIG_DECIMAL:
                case NBIG_DECIMAL: {
                    if (value == null) {
                        ps.setNull(index, Types.DECIMAL);
                    } else {
                        ps.setBigDecimal(index, NumberUtils.asBigDecimal(((Number) value)));
                    }
                    break;
                }
                case STRING:
                case NSTRING: {
                    if (value == null) {
                        ps.setNull(index, Types.VARCHAR);
                    } else {
                        ps.setString(index, (String) value);
                    }
                    break;
                }
                case DOUBLE:
                case NDOUBLE: {
                    if (value == null) {
                        ps.setNull(index, Types.DOUBLE);
                    } else {
                        ps.setDouble(index, (Double) value);
                    }
                    break;
                }
                case FLOAT:
                case NFLOAT: {
                    if (value == null) {
                        ps.setNull(index, Types.FLOAT);
                    } else {
                        ps.setFloat(index, (Float) value);
                    }
                    break;
                }
                case BYTES:
                case NBYTES: {
                    if (value == null) {
                        ps.setNull(index, Types.BLOB);
                    } else {
                        ps.setBytes(index, (byte[]) value);
                    }
                    break;
                }
                case BYTE_STREAM:
                case NBYTE_STREAM: {
                    if (value == null) {
                        ps.setNull(index, Types.BLOB);
                    } else {
                        ps.setBinaryStream(index, (InputStream) value);
                    }
                    break;
                }
                case CHAR_STREAM:
                case NCHAR_STREAM: {
                    if (value == null) {
                        ps.setNull(index, Types.CLOB);
                    } else {
                        ps.setCharacterStream(index, (Reader) value);
                    }
                    break;
                }
                case JAVA_OBJECT:
                case NJAVA_OBJECT: {
                    if (value == null) {
                        ps.setNull(index, Types.JAVA_OBJECT);
                    } else {
                        ps.setObject(index, value);
                    }
                    break;
                }
                case DOCUMENT:
                case NDOCUMENT:
                default: {
                    throw new UncheckedIOException(new IOException("Unsupported " + st));
                }
            }
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    protected StoreDataType createDefaultFileColType(ColumnDefinition c) {
        int precision = c.getPrecision();
        boolean dec = precision > 0;
        switch (c.getSqlType()) {
            case Types.BIT:
                return StoreDataType.NBOOLEAN;
            case Types.TINYINT:
                return StoreDataType.NBYTE;
            case Types.SMALLINT:
                return StoreDataType.NSHORT;
            case Types.INTEGER: {
                if (dec) {
                    //some drivers are so dump
                    return StoreDataType.NBIG_DECIMAL;
                }
                return StoreDataType.NINT;
            }
            case Types.BIGINT: {
                if (dec) {
                    //some drivers are so dump
                    return StoreDataType.NBIG_DECIMAL;
                }
                if ("java.lang.Long".equals(c.getJavaClassName())) {
                    return StoreDataType.NLONG;
                }
                return StoreDataType.NBIG_INT;
            }
            case Types.FLOAT:
                return StoreDataType.NDOUBLE;
            case Types.REAL:
                return StoreDataType.NDOUBLE;
            case Types.DOUBLE:
                return StoreDataType.NDOUBLE;
            case Types.NUMERIC: {
                if (dec) {
                    return StoreDataType.NBIG_DECIMAL;
                } else {
                    return StoreDataType.NBIG_INT;
                }
            }
            case Types.DECIMAL: {
                return StoreDataType.NBIG_DECIMAL;
            }
            case Types.CHAR: {
                return StoreDataType.NSTRING;
            }
            case Types.VARCHAR: {
                return StoreDataType.NSTRING;
            }
            case Types.LONGVARCHAR: {
                return StoreDataType.NSTRING;
            }
            case Types.DATE: {
                return StoreDataType.NDATE;
            }
            case Types.TIME: {
                return StoreDataType.NTIME;
            }
            case Types.TIMESTAMP: {
                return StoreDataType.NTIMESTAMP;
            }
            case Types.BINARY: {
                return StoreDataType.NBYTES;
            }
            case Types.VARBINARY: {
                return StoreDataType.NBYTE_STREAM;
            }
            case Types.LONGVARBINARY: {
                return StoreDataType.NBYTE_STREAM;
            }
            case Types.NULL: {
                return StoreDataType.NULL;
            }
            case Types.OTHER:
            case Types.JAVA_OBJECT:
            case Types.STRUCT:
            case Types.ARRAY: {
                return StoreDataType.NJAVA_OBJECT;
            }
            case Types.BLOB: {
                return StoreDataType.NBYTE_STREAM;
            }
            case Types.CLOB: {
                return StoreDataType.NBYTE_STREAM;
            }
            case Types.REF:
            case Types.DATALINK:
            case Types.REF_CURSOR: {
                return StoreDataType.NJAVA_OBJECT;
            }
            case Types.BOOLEAN: {
                return StoreDataType.NBOOLEAN;
            }
            case Types.NCHAR: {
                return StoreDataType.NSTRING;
            }
            case Types.NVARCHAR: {
                return StoreDataType.NSTRING;
            }
            case Types.LONGNVARCHAR: {
                return StoreDataType.NSTRING;
            }
            case Types.NCLOB: {
                return StoreDataType.NCHAR_STREAM;
            }
            case Types.SQLXML: {
                return StoreDataType.NCHAR_STREAM;
            }
            case Types.TIME_WITH_TIMEZONE: {
                return StoreDataType.NTIME;
            }
            case Types.TIMESTAMP_WITH_TIMEZONE: {
                return StoreDataType.NTIMESTAMP;
            }
            default: {
                throw new IllegalArgumentException("unsupported");
            }

        }
    }

    protected ColumnDefinition resolveDefaultColumnDefinition(ResultSetMetaData rs, int index) {
        try {
            ColumnDefinition c = new ColumnDefinition();
            c.setIndex(index);
            c.setColumnName(rs.getColumnName(index));
            c.setLabel(rs.getColumnLabel(index));
            c.setLabel(rs.getColumnLabel(index));
            c.setSqlType(rs.getColumnType(index));
            c.setSqlTypeCode(getSqlTypeCode(c.getSqlType()));
            c.setSqlTypeName(rs.getColumnTypeName(index));
            c.setDisplaySize(rs.getColumnDisplaySize(index));
            c.setPrecision(rs.getPrecision(index));
            c.setScale(rs.getScale(index));
            c.setJavaClassName(rs.getColumnClassName(index));
            return c;
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    @Override
    public boolean tableConstraintsExists(TableConstraintsId id) {
        LOG.log(Level.SEVERE, "[" + id.getFullName() + "] not yet supported tableConstraintsExists");
        throw new IllegalArgumentException("not supported tableConstraintsExists");
    }

    @Override
    public void createTableConstraints(TableConstraintsDefinition def) {
        LOG.log(Level.SEVERE, "[" + def.getTableConstraintsId().getFullName() + "] not yet supported createTableConstraints");
        throw new IllegalArgumentException("not supported createTableConstraints");
    }

    @Override
    public void dropTableConstraints(TableConstraintsId id) {
        LOG.log(Level.SEVERE, "[" + id.getFullName() + "] not yet supported createTableConstraints");
        throw new IllegalArgumentException("not supported createTableConstraints");
    }

    @Override
    public boolean dropTableConstraintsIfExists(TableConstraintsId id) {
        if (tableConstraintsExists(id)) {
            dropTableConstraints(id);
            return true;
        }
        return false;
    }

    protected ResultSet logResultSetCall(String name, SqlSupplier<ResultSet> r) {
        LOG.log(Level.SEVERE, "[SQL-SUPPLY] " + name);
        try {
            return r.get();
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    @Override
    public boolean patchColumn(ColumnDefinition col) {
        if (columnExists(col.toFieldId())) {
            ColumnDefinition old = getColumnDefinition(col.getColumnId());
            if (old == null) {
                createColumn(col);
                return true;
            } else {
                //TODO implement me later
                // to check types, etc...
                return false;
            }
        } else {
            createColumn(col);
            return true;
        }
    }

    @Override
    public void createColumn(ColumnDefinition col) {
        LOG.log(Level.SEVERE, "[" + col.toTableId().getFullName() + "][" + col.getColumnName() + "] not yet supported createColumn");
        TableId table = (TableId) col.toTableId();
        runDDL(table,
                "alter table " +
                        escapeIdentifier(table)
                        + " add column "
                        + resolveSqlColumnDefinition(col)
        );
    }

    @Override
    public boolean tableExists(TableId c) {
        return getTableDefinition(c) != null;
    }

    @Override
    public boolean catalogExists(CatalogId c) {
        try (SafeResultSet rs = new SafeResultSet(logResultSetCall("getCatalogs()", () -> getConnection().getMetaData().getCatalogs()))) {
            return rs.readStream(ResultSetMappers.ofString("TABLE_CAT")).anyMatch(x -> Objects.equals(c.getCatalogName(), x));
        }
    }

    @Override
    public boolean schemaExists(SchemaId s) {
        try (SafeResultSet rs = new SafeResultSet(logResultSetCall("getSchemas()", () -> getConnection().getMetaData().getSchemas()))) {
            return rs.readStream(x -> new TableId(
                    x.getString("TABLE_CATALOG"),
                    x.getString("TABLE_SCHEM"),
                    "ANY"
            )).anyMatch(x -> {
                if (!StringUtils.isBlank(s.getCatalogName())) {
                    if (!Objects.equals(s.getCatalogId(), x.getCatalogName())) {
                        return false;
                    }
                }
                if (!StringUtils.isBlank(s.getSchemaName())) {
                    if (!Objects.equals(s.getSchemaName(), x.getSchemaName())) {
                        return false;
                    }
                }
                return true;
            });
        }
    }

    public long getMaxVarcharLength() {
        return maxVarcharLength;
    }

    public AbstractDatabaseDriver setMaxVarcharLength(long maxVarcharLength) {
        this.maxVarcharLength = maxVarcharLength;
        return this;
    }

    @Override
    public boolean dropTableIfExists(TableId tableId) {
        if (tableExists(tableId)) {
            dropTable(tableId);
            return true;
        }
        return false;
    }

    @Override
    public boolean createTableConstraintsIfNotExists(TableConstraintsDefinition def) {
        if (!tableConstraintsExists(def.getTableConstraintsId())) {
            createTableConstraints(def);
            return true;
        }
        return false;
    }

    @Override
    public boolean createTableIfNotExists(TableDefinition def) {
        if (!tableExists(def.getTableId())) {
            createTable(def);
            return true;
        }
        return false;
    }

    @Override
    public boolean patchTable(TableDefinition def, TableRestoreOptions options) {
        if (!tableExists(def.getTableId())) {
            createTable(def);
            return true;
        } else {
            boolean logDropped = false;
            boolean logCreated = false;
            boolean someUpdates = false;
            Set<String> createdColumns = new HashSet<>();
            Set<String> updatedColumns = new HashSet<>();
            Set<String> droppedColumns = new HashSet<>();

            TableDefinition newMd = def;
            TableId newTable = null;
            newTable = new TableId(getSchemaId(), newMd.getTableName());
            TableDefinition oldMd = this.getTableDefinition(newTable);
            if (oldMd != null) {
                if (options.isDropTable()) {
                    this.dropTable(newTable);
                    logDropped = true;
                    oldMd = null;
                    someUpdates = true;
                }
            }
            if (oldMd == null) {
                this.createTable(newMd);
                logCreated = true;
                someUpdates = true;
            } else {
                List<ColumnDefinition> newCols = newMd.getColumns();
                List<ColumnDefinition> oldCols = oldMd.getColumns();
                for (ColumnDefinition newCol : newCols) {
                    ColumnDefinition oldCol = oldCols.stream().filter(x -> x.getColumnName().equals(newCol.getColumnName())).findAny().orElse(null);
                    if (oldCol == null) {
                        if (options.isCreateColumn()) {
                            this.createColumn(newCol);
                            createdColumns.add(newCol.getColumnName());
                            someUpdates = true;
                        } else {
                            throw new IllegalArgumentException("missing column " + newCol);
                        }
                    } else {
                        if (this.patchColumn(newCol)) {
                            someUpdates = true;
                        }
                    }
                }
                for (ColumnDefinition oldCol : oldCols) {
                    ColumnDefinition newCol = newCols.stream().filter(x -> x.getColumnName().equals(oldCol.getColumnName())).findAny().orElse(null);
                    if (newCol == null) {
                        if (options.isDropColumn()) {
                            this.dropColumn(oldCol.toFieldId());
                            someUpdates = true;
                            droppedColumns.add(newCol.getColumnName());
                        } else {
                            throw new IllegalArgumentException("cannot drop column " + oldCol);
                        }
                    } else {
                        if (this.patchColumn(newCol)) {
                            someUpdates = true;
                        }
                    }
                }
            }

            StringBuilder ll = new StringBuilder();
            if (logDropped) {
                ll.append("re-created");
            } else if (logCreated) {
                ll.append("created");
            }
            if (!createdColumns.isEmpty()) {
                if (ll.length() > 0) {
                    ll.append(", ");
                }
                ll.append("added columns : ").append(createdColumns);
            }
            if (!droppedColumns.isEmpty()) {
                if (ll.length() > 0) {
                    ll.append(", ");
                }
                ll.append("dropped columns : " + droppedColumns);
            }
            if (!updatedColumns.isEmpty()) {
                if (ll.length() > 0) {
                    ll.append(", ");
                }
                ll.append("altered columns : " + updatedColumns);
            }
            if (ll.length() > 0) {
                LOG.log(Level.FINE, "[" + newTable.getFullName() + "] " + ll);
            } else {
                LOG.log(Level.FINE, "[" + newTable.getFullName() + "] clean and uptodate.");
            }
            return someUpdates;
        }
    }

    @Override
    public boolean addColumnIfNotExists(ColumnDefinition col) {
        if (!columnExists(col.getColumnId())) {
            createColumn(col);
            return true;
        }
        return false;
    }

    @Override
    public boolean dropDatabaseIfExists(String s) {
        if (databaseExists(s)) {
            dropDatabase(s);
            return true;
        }
        return false;
    }

    protected static class PreparedStatementExt {
        PreparedStatement ps;
        String sql;
    }

    protected static class ImportDataContext {
        private PreparedStatementExt insertRowPreparedStatement;
        private TableRestoreOptions schemaMode;
        private TableId newTable;
        protected StoreRows rows;
        protected boolean failFastSQL = false;
    }

    public void importData(StoreRows rows, TableRestoreOptions options) {
        ImportDataContext cc = new ImportDataContext();
        cc.schemaMode = options == null ? new TableRestoreOptions() : options;
        TableDefinition definition = (TableDefinition) rows.getDefinition();
        cc.newTable = new TableId(getSchemaId(), definition.getTableName());
        cc.rows = rows;
        if (cc.schemaMode.isClearTable()) {
            this.clearTable(cc.newTable);
        }
        IoRow r = null;
        TableDefinition d2 = ((TableDefinition) rows.getDefinition()).copy();
        d2.setTableName(cc.newTable.getTableName());
        cc.insertRowPreparedStatement = _createInsertQuery(d2);
        while ((r = rows.nextRow()) != null) {
            try (RepeatableReadIoCellArr ccc = new RepeatableReadIoCellArr(r)) {
                executeInsert(ccc, cc);
            }
        }
    }

    private void executeInsert(RepeatableReadIoCellArr ccc, ImportDataContext cc) {
        RepeatableReadIoCell[] cells = ccc.getCells();
        Object[] vals = new Object[cells.length];
        PreparedStatementExt ps = cc.insertRowPreparedStatement;
        for (int i = 0; i < cells.length; i++) {
            RepeatableReadIoCell c = cells[i];
            Object vv = c.getObject();
            this.prepareStatement(ps.ps, i + 1, c.getDefinition(), vv);
            vals[i] = c.getDefinition().getStoreType().name() + "@" + StringUtils.litString(vv);
        }
        LOG.log(Level.FINEST, "[" + cc.newTable.getFullName() + "] " + ps.sql + " :: " + Arrays.asList(vals));
        try {
            ps.ps.executeUpdate();
        } catch (SQLException e) {
            if (cc.failFastSQL) {
                throw new UncheckedSQLException(e);
            } else {
                LOG.log(Level.SEVERE, "[" + cc.newTable.getFullName() + "] " + ps.sql + " :: " + Arrays.asList(vals) + " : " + e);
            }
        }
    }

    private PreparedStatementExt _createInsertQuery(TableDefinition def) {
        StringBuilder sb = new StringBuilder("insert into ");
        List<ColumnDefinition> columns = (List) def.getColumns();
        sb.append(this.escapeIdentifier(def.getTableName()));
        sb.append("(");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(this.escapeIdentifier(columns.get(i).getColumnName()));
        }
        sb.append(")");
        sb.append(" values ");
        sb.append("(");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("?");
        }
        sb.append(")");
        PreparedStatementExt a = new PreparedStatementExt();
        try {
            a.sql = sb.toString();
            a.ps = this.getConnection().prepareStatement(a.sql);
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
        return a;
    }

    @Override
    public void enableConstraints(TableDefinition d, TableRestoreOptions schemaMode) {
        LOG.log(Level.SEVERE, "[" + d.getTableId().getFullName() + "] not yet supported enableConstraints");
    }

    @Override
    public void disableConstraints(TableDefinition d, TableRestoreOptions schemaMode) {
        LOG.log(Level.SEVERE, "[" + d.getTableId().getFullName() + "] not yet supported disableConstraints");
    }

}
