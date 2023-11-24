/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.diet.sql;

import net.thevpc.diet.io.IoCell;
import net.thevpc.diet.io.IoRow;
import net.thevpc.diet.io.StoreRows;
import net.thevpc.diet.model.*;
import net.thevpc.diet.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author vpc
 */
public abstract class AbstractDatabaseDriver implements DatabaseDriver {
    public static Logger LOG = Logger.getLogger(AbstractDatabaseDriver.class.getName());
    private Connection connection;
    private long maxVarcharLength;


    public AbstractDatabaseDriver(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public StoreRowsDefinition createRowsDefinition(ResultSetMetaData rs) {
        try {
            StoreRowsDefinition c = new StoreRowsDefinition();
            int columnCount;
            columnCount = rs.getColumnCount();
            StoreColumnDefinition[] allCols = new StoreColumnDefinition[columnCount];
            for (int i = 0; i < allCols.length; i++) {
                allCols[i] = createColumnDefinition(rs, i + 1);
            }
            c.setColumns(allCols);
            return c;
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
    }

    public StoreColumnDefinition createColumnDefinition(ResultSetMetaData rs, int index) {
        StoreColumnDefinition c = createDefaultColumnDefinition(rs, index);
        c.setStoreType(createFileColType(c));
        return c;
    }

    protected StoreDataType createFileColType(StoreColumnDefinition c) {
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
    public StoreTableDefinition getTableMetaData(TableId name) {
        LOG.log(Level.FINEST, "[" + name.toStringId() + "] load table metadata");
        StoreTableDefinition tmd = null;
        try {
            DatabaseMetaData md = connection.getMetaData();
            try (ResultSet rs = md.getTables(connection.getCatalog(), connection.getSchema(), name.getTableName(), null)) {
                while (rs.next()) {
                    String tableCat = _getRsStringByName(rs, "TABLE_CAT");
                    if (tableCat == null) {
                        tableCat = connection.getCatalog();
                    }
                    String tableSchem = _getRsStringByName(rs, "TABLE_SCHEM");
                    if (tableSchem == null) {
                        tableSchem = connection.getSchema();
                    }
                    String tableName = _getRsStringByName(rs, "TABLE_NAME");
                    TableId table = new TableId(
                            tableCat, tableSchem, tableName);
                    if (table.equals(name)) {
                        tmd = new StoreTableDefinition();
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
            Map<String, StoreColumnDefinition> columns = new LinkedHashMap<String, StoreColumnDefinition>();
            if (tmd != null) {
                try (ResultSet rs = md.getColumns(tmd.getCatalogName(), tmd.getSchemaName(), tmd.getTableName(), null)) {
                    while (rs.next()) {
                        String tableName = _getRsStringByName(rs, "TABLE_NAME");
                        String tableCat = _getRsStringByName(rs, "TABLE_CAT");
                        String tableSchem = _getRsStringByName(rs, "TABLE_SCHEM");
                        if (tableCat == null) {
                            tableCat = tmd.getCatalogName();
                        }
                        if (tableSchem == null) {
                            tableSchem = tmd.getSchemaName();
                        }
                        TableId table = new TableId(tableCat, tableSchem, tableName);
                        if (table.equals(name)) {
                            StoreColumnDefinition cmd = new StoreColumnDefinition();
                            cmd.setColumnName(_getRsStringByName(rs, "COLUMN_NAME"));
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
                            columns.put(cmd.getColumnName(), cmd);
                        }
                    }
                } catch (SQLException ex) {
                    throw new UncheckedSQLException(ex);
                }
                tmd.setColumns(columns.values().toArray(new StoreColumnDefinition[0]));
                try (ResultSet rs = md.getExportedKeys(connection.getCatalog(), connection.getSchema(), name.getTableName())) {
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
                            PKTABLE_SCHEM = connection.getSchema();
                        }
                        if (FKTABLE_SCHEM == null) {
                            FKTABLE_SCHEM = connection.getSchema();
                        }

                        int KEY_SEQ = _getRsIntByName(rs, "KEY_SEQ");
                        short UPDATE_RULE = _getRsShortByName(rs, "UPDATE_RULE");
                        short DELETE_RULE = _getRsShortByName(rs, "UPDATE_RULE");
                        TableId tid1 = new TableId(PKTABLE_CAT, PKTABLE_SCHEM, PKTABLE_NAME);
                        TableId tid2 = new TableId(FKTABLE_CAT, FKTABLE_SCHEM, FKTABLE_NAME);
                        if (tid1.equals(name)) {
                            StoreColumnDefinition c = Arrays.stream(tmd.getColumns())
                                    .filter(x -> x.getColumnName().equals(PKCOLUMN_NAME))
                                    .findAny().get();
                            c.setPk(true);
                            c.setPkIndex(KEY_SEQ);
                        } else if (tid2.equals(name)) {
                            StoreColumnDefinition c = Arrays.stream(tmd.getColumns())
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
    public List<TableHeader> getTables() {
        ArrayList<TableHeader> all = new ArrayList<TableHeader>();
        try (ResultSet rs = connection.getMetaData().getTables(connection.getCatalog(), connection.getSchema(), null, null)) {
            while (rs.next()) {
                all.add(new TableHeader(
                        _getRsStringByName(rs, "TABLE_CAT"), _getRsStringByName(rs, "TABLE_SCHEM"),
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
    public StoreRows getTableRows(TableId table) {
        StoreTableDefinition tableMetaData = getTableMetaData(table);
        long startTime = System.currentTimeMillis();
        LOG.log(Level.FINEST, "[" + table.toStringId() + "] reading from DB... ");
        ResultSet rs = getTableResultSet(table);
        long endTime = System.currentTimeMillis();
        LOG.log(Level.FINEST, "[" + table.toStringId() + "] read in " + (endTime - startTime) + "ms... ");
        return new StoreRows() {
            StoreRowsDefinition t;

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
            public StoreTableDefinition getDefinition() {
                return tableMetaData;
            }

            @Override
            public IoRow nextRow() {
                try {
                    if (rs.next()) {
                        return new IoRow() {
                            int index = 0;

                            @Override
                            public IoCell nextColumn() {
                                IoCell c = createCell(rs, t.getColumns()[index]);
                                index++;
                                return c;
                            }

                            @Override
                            public StoreTableDefinition getDefinition() {
                                return tableMetaData;
                            }
                        };
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
            LOG.log(Level.FINEST, "[" + table.toStringId() + "] [SQL] " + sql);
            return connection.createStatement().executeQuery(sql);
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
    }

    @Override
    public void clearTable(TableId t) {
        runDDL(t, "delete from " + escapeIdentifier(t));
    }

    public String getTypeDefinition(StoreColumnDefinition def) {
        switch (def.getStoreType()) {
            case STRING:
            case NSTRING: {
                if (getMaxVarcharLength() > 0) {
                    if (def.getScale() > getMaxVarcharLength()) {
                        def = def.copy();
                        def.setStoreType(StoreDataType.NCHAR_STREAM);
                        return getTypeDefinition(def);
                    }
                }
                return "VARCHAR(" + def.getScale() + ")";
            }
            case BIG_DECIMAL:
            case BIG_INT:
            case NBIG_DECIMAL:
            case NBIG_INT: {
                return "NUMBER(" + def.getScale() + "," + def.getPrecision() + ")";
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
                return getTypeDefinition(def);
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

    public String getColumnDefinition(StoreColumnDefinition def) {
        return escapeIdentifier(def.getColumnName()) + " " + getTypeDefinition(def);
    }

    @Override
    public void createTable(StoreTableDefinition def) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ");
        sql.append(escapeIdentifier(def.getTableName()));
        sql.append("(");
        StoreColumnDefinition[] columns = def.getColumns();
        for (int i = 0; i < columns.length; i++) {
            StoreColumnDefinition column = columns[i];
            if (i > 0) {
                sql.append(",");
            }
            sql.append(getColumnDefinition(column));
        }
        sql.append(")");
        runDDL(sql.toString());
    }

    @Override
    public void createTableConstraints(StoreTableDefinition def) {
//        StringBuilder sql = new StringBuilder();
//        sql.append("CREATE TABLE ");
//        sql.append(escapeIdentifier(def.getTableName()));
//        sql.append("(");
//        sql.append(")");
//        runDDL(sql.toString());
    }

    public void createDatabase(String s) {
        runDDL("create database " + s);
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
    public void dropColumn(StoreColumnDefinition col) {
        TableId table = new TableId(col.getCatalogName(), col.getSchemaName(), col.getTableName());
        runDDL(table,
                "alter table " +
                        escapeIdentifier(table)
                        + " drop column "
                        + escapeIdentifier(col.getColumnName())
        );
    }

    @Override
    public IoCell createCell(ResultSet rs, StoreColumnDefinition column) {
        return new DefaultResultSetIoCell(column, rs);
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
                        ps.setLong(index, (Long) value);
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
                        ps.setBigDecimal(index, new BigDecimal(((BigInteger) value)));
                    }
                    break;
                }
                case BIG_DECIMAL:
                case NBIG_DECIMAL: {
                    if (value == null) {
                        ps.setNull(index, Types.DECIMAL);
                    } else {
                        ps.setBigDecimal(index, (BigDecimal) value);
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

    protected StoreDataType createDefaultFileColType(StoreColumnDefinition c) {
        switch (c.getSqlType()) {
            case Types.BIT:
                return StoreDataType.NBOOLEAN;
            case Types.TINYINT:
                return StoreDataType.NBYTE;
            case Types.SMALLINT:
                return StoreDataType.NSHORT;
            case Types.INTEGER:
                return StoreDataType.NINT;
            case Types.BIGINT: {
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
                if (c.getPrecision() > 0) {
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

    protected StoreColumnDefinition createDefaultColumnDefinition(ResultSetMetaData rs, int index) {
        try {
            StoreColumnDefinition c = new StoreColumnDefinition();
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
    public TableImporter createTableImporter() {
        return new DefaultTableImporter(this);
    }

    @Override
    public void dropTableConstraints(StoreTableDefinition def) {
        LOG.log(Level.SEVERE, "[" + def.toTableId().toStringId() + "] not yet supported dropTableConstraints");
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
    public void createColumn(StoreColumnDefinition col) {
        LOG.log(Level.SEVERE, "[" + col.toTableId().toStringId() + "][" + col.getColumnName() + "] not yet supported createColumn");
    }

    @Override
    public boolean catalogExists(String c) {
        try (SafeResultSet rs = new SafeResultSet(logResultSetCall("getCatalogs()",()->getConnection().getMetaData().getCatalogs()))) {
            return rs.readStream(ResultSetMappers.ofString("TABLE_CAT")).anyMatch(x -> Objects.equals(c, x));
        }
    }

    @Override
    public boolean schemaExists(String c, String s) {
        try (SafeResultSet rs = new SafeResultSet(logResultSetCall("getSchemas()",()->getConnection().getMetaData().getSchemas()))) {
            return rs.readStream(x -> new TableId(
                    x.getString("TABLE_CATALOG"),
                    x.getString("TABLE_SCHEM"),
                    "ANY"
            )).anyMatch(x -> {
                if (!StringUtils.isBlank(c)) {
                    if (!Objects.equals(c, x.getCatalogName())) {
                        return false;
                    }
                }
                if (!StringUtils.isBlank(s)) {
                    if (!Objects.equals(s, x.getSchemaName())) {
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
}
