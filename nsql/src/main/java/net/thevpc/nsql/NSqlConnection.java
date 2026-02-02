package net.thevpc.nsql;

import net.thevpc.nsql.impl.DefaultDatabaseItemQuery;
import net.thevpc.nsql.impl.ResultSetQueryResult;
import net.thevpc.nsql.mapper.NResultSetMappers;
import net.thevpc.nsql.model.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.io.NIOUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class NSqlConnection implements AutoCloseable {
    public static NSqlConnection of(NSqlConnectionString params) {
        return NSqlConnectionFactory.of(params).create();
    }

    private static Logger LOG = Logger.getLogger(NSqlConnection.class.getName());
    private NSqlDialect dialect;
    private NSqlConnectionFactory connectionFactory;
    private Connection connection;
    private boolean connectionReady;
    private Map<String, PreparedStatement> insertPreparedStatementMap = new LinkedHashMap<>();
    private Map<String, NSqlTable> tables = new LinkedHashMap<>();
    private long maxVarcharLength;
    private boolean sharedConnection;

    public NSqlConnection(NSqlConnectionFactory connectionFactory, Connection connection,boolean sharedConnection) {
        this.connectionFactory = connectionFactory;
        this.dialect = connectionFactory.dialect();
        this.connection = connection;
        this.sharedConnection = sharedConnection;
    }

    public void declareTable(NSqlTable sqlTable) {
        if (sqlTable != null) {
            String n = sqlTable.getTableName();
            if (tables.containsKey(n)) {
                throw new IllegalArgumentException("Table already registered : " + n);
            }
            tables.put(sqlTable.getTableName(), sqlTable);
        }
    }

    public NSqlDialect getDialect() {
        return dialect;
    }

    public String getDatabaseName() {
        try {
            return connection.getCatalog();
//            return connection.getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    protected String createTableDdl(NSqlTable jdbcTable) {
        String validTableName = escapeIdentifier(jdbcTable.getTableName());
        StringBuilder ddl = new StringBuilder();
        ddl.append("CREATE TABLE ")
                .append(validTableName)
                .append("(");
        boolean first = true;
        for (NSqlColumn value : jdbcTable.getColumns()) {
            if (first) {
                first = false;
            } else {
                ddl.append(",");
            }
            ddl.append(" ").append(value.columnName).append(" ").append(createTypeDefinition(value));
        }
        ddl.append(")");
        return ddl.toString();
    }

    protected String quotedIdentifier(String s) {
        return s;
    }

    protected IllegalArgumentException unsupportedDialect() {
        return new IllegalArgumentException("unsupported dialect " + dialect);
    }

    private boolean tableExists(String tableName) {
        try (Statement s = getConnection().createStatement()) {
            try (ResultSet rs = s.executeQuery("Select count(1) from " + quotedIdentifier(tableName))) {
                //table exists
            } catch (SQLException e) {
                return false;
            }
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
        return true;
    }

    public boolean validateTableSchema(NSqlTable logTable) {
        NDdlAuto ddlAuto = logTable.getDdlAuto();
        if (ddlAuto == null) {
            ddlAuto = NDdlAuto.UPDATE;
        }
        switch (ddlAuto) {
            case NONE: {
                return false;
            }
            case DROP_CREATE: {
                if (tableExists(logTable.getTableName())) {
                    dropTable(logTable.getTableName());
                }
                return createTable(logTable);
            }
            case CREATE: {
                if (!tableExists(logTable.getTableName())) {
                    return createTable(logTable);
                } else {
                    return false;
                }
            }
            case UPDATE: {
                if (!tableExists(logTable.getTableName())) {
                    return createTable(logTable);
                } else {
                    return updateTableSchema(logTable);
                }
            }
        }
        if (!tableExists(logTable.getTableName())) {
            return createTable(logTable);
        }
        return false;
    }

    private boolean updateTableSchema(NSqlTable logTable) {
        LOG.log(Level.SEVERE, "unsupported updateTableSchema({0}). ignored", logTable.getTableName());
        return false;
    }

    public boolean execute(String sql) {
        try {
            try (Statement s = getConnection().createStatement()) {
                return s.execute(sql);
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, e, () -> "Error executing query " + sql);
            return false;
        }
    }

    public boolean createTable(NSqlTable logTable) {
        String query = createTableDdl(logTable);
        try {
            executeUpdate(query);
            return true;
        } catch (UncheckedSqlException e) {
            LOG.log(Level.SEVERE, e, () -> "Error executing query " + query);
            return false;
        }

    }

    public boolean dropTable(String logTable) {
        String query = "DROP TABLE " + quotedIdentifier(logTable);
        try {
            executeUpdate(query);
            return true;
        } catch (UncheckedSqlException e) {
            LOG.log(Level.SEVERE, e, () -> "Error executing query " + query);
            return false;
        }

    }

    public void insertSafe(NSqlTable table, Object value, NSqlObjectExtractor valueResolver) {
        boolean refreshConnection = false;
        if (this.ensureConnectionReady(true)) {
            try {
                this.insert(table, value, valueResolver);
            } catch (UncheckedSqlException e) {
                LOG.log(Level.SEVERE, e, () -> "Error inserting " + table.getTableName());
                if (e.toString().contains("closed")) {
                    refreshConnection = true;
                }
            }
        }
        if (refreshConnection) {
            this.refresh();
            this.insert(table, value, valueResolver);
        }
    }

    public void insert(NSqlTable table, Object value, NSqlObjectExtractor valueResolver) {
        PreparedStatement ps = prepareInsertStatement(table);
        table.prepareStatement(ps, n -> valueResolver.get(value, n));
        try {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public PreparedStatement prepareInsertStatement(NSqlTable table) {
        NAssert.requireNamedNonBlank(table.getTableName(), "tableName");
        PreparedStatement preparedStatement = insertPreparedStatementMap.get(table.getTableName());
        if (preparedStatement != null) {
            return preparedStatement;
        }
        StringBuilder ddl = new StringBuilder();
        ddl.append("INSERT INTO ")
                .append(quotedIdentifier(table.getTableName()))
                .append("(");
        boolean first = true;
        for (NSqlColumn value : table.getColumns()) {
            if (value.autoIncrement != YesNo.YES) {
                if (first) {
                    first = false;
                } else {
                    ddl.append(",");
                }
                ddl.append(" ").append(value.columnName);
            }
        }
        ddl.append(")");
        ddl.append(" values (");
        first = true;
        for (NSqlColumn value : table.getColumns()) {
            if (value.autoIncrement != YesNo.YES) {
                if (first) {
                    first = false;
                } else {
                    ddl.append(",");
                }
                ddl.append(" ?");
            }
        }
        ddl.append(")");
        try {
            return getConnection().prepareStatement(ddl.toString(), Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
//        connection.prepareStatement(
//                "INSERT INTO packets() VALUES (DEFAULT,?)".replace(
//                        "?", "'" + receivedPacket.getClass().getSimpleName() + "'" + ",NOW()," + receivedPacket.getProtocolNumber() + "," + receivedPacket.getLatitude() + "," + receivedPacket.getLongitude() + "," + receivedPacket.getSpeed() + "," + receivedPacket.getTerminalId() + ",'" + receivedBuffer.toHexString() + "'"
//                )
//        );
    }

    public void refresh() {
        if(sharedConnection){
            return;
        }
        close();
        ensureConnectionReady(false);
    }

    public String createTypeDefinition(NSqlColumn d) {
        StringBuilder sb = new StringBuilder();
        boolean nullable = d.getNullable() == YesNo.YES;
        String nullDef = nullable ? " NULL" : " NOT NULL";
        boolean pkey = d.getId() != null && d.getId();
        Integer precision = d.getPrecision();
        Integer scale = d.getScale();
        switch (d.getColumnType()) {
            case STRING: {
                Integer len = d.getColumnLength();
                sb.append("VARCHAR(").append((len == null || len <= 0) ? 255 : len).append(")").append(nullDef);
                break;
            }
            case BIGINT: {
                String nbr = "NUMBER";
                if (precision != null) {
                    nbr += "(" + precision + ")";
                }
                if (pkey) {
                    sb.append("BIGSERIAL NOT NULL PRIMARY KEY");
                } else {
                    sb.append(nbr).append(nullDef);
                }
                break;
            }
            case BIGDECIMAL: {
                String nbr = "NUMBER";
                if (precision != null) {
                    if (scale != null) {
                        nbr += "(" + precision + "," + scale + ")";
                    } else {
                        nbr += "(" + precision + ")";
                    }
                }
                if (pkey) {
                    sb.append("BIGSERIAL NOT NULL PRIMARY KEY");
                } else {
                    // NUMERIC(2, -3)
                    sb.append(nbr).append(nullDef);
                }
                break;
            }
            case DECIMAL: {
                sb.append("DECIMAL").append(nullDef);
                break;
            }
            case INT: {
                if (pkey) {
                    sb.append("SERIAL NOT NULL PRIMARY KEY");
                } else {
                    sb.append("INTEGER").append(nullDef);
                }
                break;
            }
            case LONG: {
                if (pkey) {
                    sb.append("BIGSERIAL NOT NULL PRIMARY KEY");
                } else {
                    sb.append("BIGINT").append(nullDef);
                }
                break;
            }
            case TIMESTAMP: {
                sb.append("TIMESTAMP").append(nullDef);
                break;
            }
            case DATE: {
                sb.append("DATE").append(nullDef);
                break;
            }
            case TIME: {
                sb.append("TIME").append(nullDef);
                break;
            }
            case BOOLEAN: {
                sb.append("boolean").append(nullDef);
                break;
            }
            case DOUBLE: {
                sb.append("REAL").append(nullDef);
                break;
            }

            default: {
                throw new IllegalArgumentException("unsupported column type " + d.getColumnType());
            }
        }
        return sb.toString();
    }

    public boolean ensureConnectionReady(boolean createTables) {
        if (connectionReady) {
            return true;
        }
        synchronized (this) {
            if (connectionReady) {
                return true;
            }
            if (createTables) {
                for (NSqlTable value : tables.values()) {
                    validateTableSchema(value);
                }
            }
            connectionReady = true;
            return true;
        }
    }

    public void close() {
        if(sharedConnection){
            return;
        }
        for (PreparedStatement value : insertPreparedStatementMap.values()) {
            if (value != null) {
                try {
                    value.close();
                } catch (SQLException e) {
                    LOG.log(Level.SEVERE, e, () -> "Error closing prepared statement " + value);
                }
            }
        }
        insertPreparedStatementMap.clear();
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, e, () -> "Error closing connection");
        }
    }

    public NSqlTableRowsDefinition createRowsDefinition(ResultSetMetaData rs) {
        try {
            NSqlTableRowsDefinition c = new NSqlTableRowsDefinition();
            int columnCount;
            columnCount = rs.getColumnCount();
            NSqlColumn[] allCols = new NSqlColumn[columnCount];
            for (int i = 0; i < allCols.length; i++) {
                allCols[i] = createSqlColumn(rs, i + 1);
            }
            c.setColumns(allCols);
            return c;
        } catch (SQLException ex) {
            throw new UncheckedSqlException(ex);
        }
    }

    public NSqlColumn createSqlColumn(ResultSetMetaData rs, int index) {
        NSqlColumn c = resolveDefaultSqlColumn(rs, index);
        c.setColumnType(resolveColumnType(c));
        return c;
    }

    public NSqlColumn getSqlColumn(NSqlColumnId name) {
        LOG.log(Level.FINEST, "[" + name.getFullName() + "] load column metadata");
        List<NSqlColumn> a = loadColumDefinitions(
                name.getCatalogName(),
                name.getSchemaName(),
                name.getTableName(),
                name.getColumnName()
        );
        if (!a.isEmpty()) {
            return a.get(0);
        }
        a = loadColumDefinitions(
                name.getCatalogName(),
                name.getSchemaName(),
                name.getTableName(),
                null
        ).stream().filter(x -> Objects.equals(x.getColumnName(), name.getColumnName())).collect(Collectors.toList());
        if (!a.isEmpty()) {
            return a.get(0);
        }
        return null;
    }

    private List<NSqlColumn> loadColumDefinitions(String catalog, String schemaPattern,
                                                  String tableNamePattern, String columnNamePattern) {
        List<NSqlColumn> columns = new ArrayList<>();
        DatabaseMetaData md = null;
        try {
            md = connection.getMetaData();
        } catch (SQLException ex) {
            throw new UncheckedSqlException(ex);
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
                NSqlTableId name = new NSqlTableId(catalog, schemaPattern, tableNamePattern);
                NSqlTableId tableId = new NSqlTableId(tableCat, tableSchem, tableName);
                if (tableId.equals(name)) {
                    NSqlColumn cmd = loadColumDefinition(rs);
                    cmd.setTableId(tableId);
                    columns.add(cmd);
                }
            }
        } catch (SQLException ex) {
            throw new UncheckedSqlException(ex);
        }
        return columns;
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
//    

    private NSqlColumn loadColumDefinition(ResultSet rs) {
        String columnName = _getRsStringByName(rs, "COLUMN_NAME");
        NSqlColumn cmd = new NSqlColumn();
        String tableName = _getRsStringByName(rs, "TABLE_NAME");
        String tableCat = _getRsStringByName(rs, "TABLE_CAT");
        String tableSchem = _getRsStringByName(rs, "TABLE_SCHEM");
        cmd.setTableName(tableName);
        cmd.setCatalogName(tableCat);
        cmd.setSchemaName(tableSchem);
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
                "YES".equalsIgnoreCase(IS_AUTOINCREMENT) ? YesNo.YES
                        : "NO".equalsIgnoreCase(IS_AUTOINCREMENT) ? YesNo.NO
                        : YesNo.UNKNOWN
        );
        String IS_GENERATEDCOLUMN = _getRsStringByName(rs, "IS_GENERATEDCOLUMN");
        cmd.setGeneratedColumn(
                "YES".equalsIgnoreCase(IS_GENERATEDCOLUMN) ? YesNo.YES
                        : "NO".equalsIgnoreCase(IS_GENERATEDCOLUMN) ? YesNo.NO
                        : YesNo.UNKNOWN
        );
        cmd.setColumnType(resolveColumnType(cmd));
        cmd.setJavaClassName(resolveColumnJavaType(cmd));
        cmd.setEnabled(true);
        return cmd;
    }

    public NSqlTableDefinition getTableDefinition(NSqlTableId name) {
        LOG.log(Level.FINEST, "[" + name.getFullName() + "] load table metadata");
        NSqlTableDefinition tmd = null;
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
                    NSqlTableId table = new NSqlTableId(tableCat, tableSchem, tableName);
                    if (table.equals(name)) {
                        tmd = new NSqlTableDefinition();
                        tmd.setTableName(tableName);
                        tmd.setSchemaName(tableSchem);
                        tmd.setCatalogName(tableCat);
                        tmd.setTableType(_getRsStringByName(rs, "TABLE_TYPE"));
                        tmd.setSelfReferencingColName(NStringUtils.trimToNull(_getRsStringByName(rs, "SELF_REFERENCING_COL_NAME")));
                        tmd.setRefGeneration(NStringUtils.trimToNull(_getRsStringByName(rs, "REF_GENERATION")));
                        break;
                    }
                }
            }
            Map<String, NSqlColumn> columns = new LinkedHashMap<String, NSqlColumn>();
            if (tmd != null) {
                for (NSqlColumn cmd : loadColumDefinitions(tmd.getCatalogName(), tmd.getSchemaName(), tmd.getTableName(), null)) {
                    columns.put(cmd.getColumnName(), cmd);
                }
                tmd.setColumns(columns.values().toArray(new NSqlColumn[0]));
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
                        NSqlTableId tid1 = new NSqlTableId(PKTABLE_CAT, PKTABLE_SCHEM, PKTABLE_NAME);
                        NSqlTableId tid2 = new NSqlTableId(FKTABLE_CAT, FKTABLE_SCHEM, FKTABLE_NAME);
                        if (tid1.equals(name)) {
                            NSqlColumn c = tmd.getColumns().stream()
                                    .filter(x -> x.getColumnName().equals(PKCOLUMN_NAME))
                                    .findAny().get();
                            c.setPk(true);
                            c.setPkIndex(KEY_SEQ);
                        } else if (tid2.equals(name)) {
                            NSqlColumn c = tmd.getColumns().stream()
                                    .filter(x -> x.getColumnName().equals(FKCOLUMN_NAME))
                                    .findAny().get();
                            c.setFk(true);
                        }
                    }
                }
            }
            return tmd;
        } catch (SQLException ex) {
            throw new UncheckedSqlException(ex);
        }
    }

    public List<NSqlSchemaHeader> getSchemas(String catalog) {
        return getSchemas()
                .stream().filter(
                        x -> Objects.equals(x.getCatalogName(), catalog)
                ).collect(Collectors.toList());
    }

    public List<NSqlSchemaHeader> getSchemas(NSqlCatalogId catalog) {
        return getSchemas(catalog == null ? null : catalog.getCatalogName());
    }

    public List<NSqlSchemaHeader> getSchemas() {
        ArrayList<NSqlSchemaHeader> all = new ArrayList<>();
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
                all.add(new NSqlSchemaHeader(
                        cat,
                        schem
                ));
            }
        } catch (SQLException ex) {
            throw new UncheckedSqlException(ex);
        }
        return all;
    }

    public NSqlSchemaId getSchemaId() {
        try {
            return new NSqlSchemaId(
                    getConnection().getCatalog(),
                    getConnection().getSchema()
            );
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public List<NSqlCatalogHeader> getCatalogs() {
        ArrayList<NSqlCatalogHeader> all = new ArrayList<>();
        try (ResultSet rs = connection.getMetaData().getCatalogs()) {
            while (rs.next()) {
                String cat = _getRsStringByName(rs, "TABLE_CAT");
                if (cat == null) {
                    cat = connection.getCatalog();
                }
                all.add(new NSqlCatalogHeader(
                        cat
                ));
            }
        } catch (SQLException ex) {
            throw new UncheckedSqlException(ex);
        }
        return all;
    }

    public List<NSqlTableHeader> getAnyTables(NSqlCatalogId catalogId) {
        return getAnyTables(catalogId == null ? null : new NSqlSchemaId(catalogId.getCatalogName(), null));
    }

    public List<NSqlTableHeader> getAnyTables(NSqlDatabaseId catalogId) {
        return getAnyTables(catalogId == null ? null : new NSqlSchemaId(catalogId.getCatalogName(), null));
    }

    public List<NSqlTableHeader> getTableHeaders(NSqlSchemaId schemaId) {
        return getAnyTables(schemaId).stream().filter(NSqlTableHeader::isTable).collect(Collectors.toList());
    }

    public List<NSqlTableHeader> getTableHeaders(NSqlCatalogId catalogId) {
        return getAnyTables(catalogId).stream().filter(NSqlTableHeader::isTable).collect(Collectors.toList());
    }

    public List<NSqlTableId> getTableIds(NSqlSchemaId schemaId) {
        return getAnyTables(schemaId).stream().filter(NSqlTableHeader::isTable).map(NSqlTableHeader::toTableId).collect(Collectors.toList());
    }

    public List<NSqlTableId> getTableIds(NSqlCatalogId catalogId) {
        return getAnyTables(catalogId).stream().filter(NSqlTableHeader::isTable).map(NSqlTableHeader::toTableId).collect(Collectors.toList());
    }

    public List<NSqlTableId> getTableIds(NSqlDatabaseId catalogId) {
        return getAnyTables(catalogId).stream().filter(NSqlTableHeader::isTable).map(NSqlTableHeader::toTableId).collect(Collectors.toList());
    }

    public List<NSqlTableHeader> getTableHeaders(NSqlDatabaseId catalogId) {
        return getAnyTables(catalogId).stream().filter(NSqlTableHeader::isTable).collect(Collectors.toList());
    }

    public List<NSqlTableHeader> getTableHeaders() {
        return getAnyTables().stream().filter(NSqlTableHeader::isTable).collect(Collectors.toList());
    }

    public boolean isSpecialTable(NSqlTableId tableId) {
        return false;
    }

    public List<NSqlTableId> getTableIds() {
        return getAnyTables().stream().filter(NSqlTableHeader::isTable).map(NSqlTableHeader::toTableId).collect(Collectors.toList());
    }

    public List<NSqlTableHeader> getAnyTables(NSqlSchemaId schemaId) {
        ArrayList<NSqlTableHeader> all = new ArrayList<NSqlTableHeader>();
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
                all.add(new NSqlTableHeader(
                        cat,
                        schem,
                        _getRsStringByName(rs, "TABLE_NAME"),
                        _getRsStringByName(rs, "TABLE_TYPE")
                ));
            }
        } catch (SQLException ex) {
            throw new UncheckedSqlException(ex);
        }
        return all;
    }

    public List<NSqlTableHeader> getAnyTables() {
        return getAnyTables(new NSqlSchemaId(getCatalog(), getSchema()));
    }

    private String getCatalog() {
        try {
            return connection.getCatalog();
        } catch (java.lang.AbstractMethodError e) {
            return null;
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    private String getSchema() {
        try {
            return connection.getSchema();
        } catch (java.lang.AbstractMethodError e) {
            return null;
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public String escapeIdentifier(NSqlTableId name) {
        return escapeIdentifier(name.getTableName());
    }

    public String escapeIdentifier(String name) {
        return "\"" + name + "\"";
    }

    public ResultSet getTableResultSet(NSqlTableId table) {
        try {
            String sql = "Select * from " + escapeIdentifier(table);
            LOG.log(Level.FINEST, "[" + table.getFullName() + "] [SQL] " + sql);
            return connection.createStatement().executeQuery(sql);
        } catch (SQLException ex) {
            throw new UncheckedSqlException(ex);
        }
    }

    public long getApproximateTableCount(NSqlTableId table) {
        return getTableCount(table);
    }

    public long getTableCount(NSqlTableId table) {
        try (Statement s = getConnection().createStatement()) {
            String sql = "Select count(1) from " + escapeIdentifier(table);
            LOG.log(Level.FINEST, "[" + table.getFullName() + "] [SQL] " + sql);
            try (ResultSet rs = s.executeQuery(sql)) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0L;
            }
        } catch (SQLException ex) {
            throw new UncheckedSqlException(ex);
        }
    }

    public void deleteFromTable(NSqlTableId t) {
        executeUpdate(t, "delete from " + escapeIdentifier(t));
    }

    public void createTable(NSqlTableDefinition def) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ");
        sql.append(escapeIdentifier(def.getTableName()));
        sql.append("(");
        List<NSqlColumn> columns = def.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            NSqlColumn column = columns.get(i);
            if (i > 0) {
                sql.append(",");
            }
            sql.append(resolveSqlSqlColumn(column));
        }
        sql.append(")");
        executeUpdate(sql.toString());
    }

    public void createDatabase(String s) {
        executeUpdate("create database " + escapeIdentifier(s));
    }

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
        executeUpdate("drop database " + s);
    }

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

    public List<NSqlDatabaseHeader> getDatabases() {
        return getCatalogs().stream().map(x -> new NSqlDatabaseHeaderImpl(
                x.getCatalogName(),
                null,
                x.getCatalogName()
        )).collect(Collectors.toList());
    }

    public void useDatabase(String s) {
        executeUpdate("use database " + escapeIdentifier(s));
    }

    public void unuseDatabase() {
        useDatabase(getDefaultDatabaseName());
    }

    protected String getDefaultDatabaseName() {
        return "postgres";
    }

    public int executeUpdate(String sql) {
        LOG.log(Level.FINEST, "[SQL] " + sql);
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    protected int executeUpdate(NSqlTableId table, String sql) {
        LOG.log(Level.FINEST, "[" + table + "] [SQL] " + sql);
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public void dropTable(NSqlTableId tableId) {
        executeUpdate(tableId, "drop table " + escapeIdentifier(tableId));
    }

    public void dropColumn(NSqlColumnId col) {
        NSqlTableId table = col.getTableId();
        executeUpdate(table,
                "alter table "
                        + escapeIdentifier(table)
                        + " drop column "
                        + escapeIdentifier(col.getColumnName())
        );
    }

    public boolean dropColumnIfExists(NSqlColumnId col) {
        if (columnExists(col)) {
            dropColumn(col);
            return true;
        }
        return false;
    }

    public boolean columnExists(NSqlColumnId c) {
        return getSqlColumn(c) != null;
    }

    public void prepareStatement(PreparedStatement ps, int index, NSqlColumn column, Object value, NPrepareStatementContext prepareStatementContext) {
        prepareStatement(ps, index, column.getColumnType(), column.getColumnName(), value, prepareStatementContext);
    }

    public void prepareStatement(PreparedStatement ps, int index, NSqlColumnType st, String columnName, Object value, NPrepareStatementContext prepareStatementContext) {
        try {
            switch (st) {
                case NULL: {
                    ps.setNull(index, Types.OTHER);
                    break;
                }
                case BOOLEAN: {
                    if (value == null) {
                        ps.setNull(index, Types.BIT);
                    } else {
                        ps.setBoolean(index, (Boolean) value);
                    }
                    break;
                }
                case BYTE: {
                    if (value == null) {
                        ps.setNull(index, Types.INTEGER);
                    } else {
                        ps.setByte(index, ((Byte) value));
                    }
                    break;
                }
                case SHORT: {
                    if (value == null) {
                        ps.setNull(index, Types.INTEGER);
                    } else {
                        ps.setShort(index, ((Short) value));
                    }
                    break;
                }
                case INT: {
                    if (value == null) {
                        ps.setNull(index, Types.INTEGER);
                    } else {
                        ps.setInt(index, ((Integer) value));
                    }
                    break;
                }
                case DECIMAL: {
                    if (value == null) {
                        ps.setNull(index, Types.DECIMAL);
                    } else {
                        ps.setBigDecimal(index, NSqlUtils.asBigDecimal((Number) value));
                    }
                    break;
                }
                case LONG: {
                    if (value == null) {
                        ps.setNull(index, Types.NUMERIC);
                    } else {
                        ps.setLong(index, ((Number) value).longValue());
                    }
                    break;
                }
                case DATE: {
                    if (value == null) {
                        ps.setNull(index, Types.DATE);
                    } else {
                        ps.setDate(index, (Date) value);
                    }
                    break;
                }
                case TIME: {
                    if (value == null) {
                        ps.setNull(index, Types.TIME);
                    } else {
                        ps.setTime(index, (Time) value);
                    }
                    break;
                }
                case TIMESTAMP: {
                    if (value == null) {
                        ps.setNull(index, Types.TIME);
                    } else {
                        ps.setTimestamp(index, (Timestamp) value);
                    }
                    break;
                }
                case BIGINT: {
                    if (value == null) {
                        ps.setNull(index, Types.BIGINT);
                    } else {
                        ps.setBigDecimal(index, NSqlUtils.asBigDecimal(((Number) value)));
                    }
                    break;
                }
                case BIGDECIMAL: {
                    if (value == null) {
                        ps.setNull(index, Types.DECIMAL);
                    } else {
                        ps.setBigDecimal(index, NSqlUtils.asBigDecimal(((Number) value)));
                    }
                    break;
                }
                case STRING: {
                    if (value == null) {
                        ps.setNull(index, Types.VARCHAR);
                    } else {
                        String cn = columnName;
                        Object u = NLobUtils.toLobFile(value, NBlankable.isBlank(cn) ? prepareStatementContext.getExternalLobFolder() : prepareStatementContext.getExternalLobFolder().resolve(cn));
                        if (u instanceof File || u instanceof Path) {
                            ps.setString(index, u.toString());
                        } else if (u instanceof String) {
                            ps.setString(index, (String) u);
                        } else if (u instanceof char[]) {
                            ps.setString(index, new String((char[]) u));
                        } else {
                            throw new IllegalArgumentException("Unsupported type for column " + columnName + " value " + value);
                        }
                    }
                    break;
                }
                case DOUBLE: {
                    if (value == null) {
                        ps.setNull(index, Types.DOUBLE);
                    } else {
                        ps.setDouble(index, (Double) value);
                    }
                    break;
                }
                case FLOAT: {
                    if (value == null) {
                        ps.setNull(index, Types.FLOAT);
                    } else {
                        ps.setFloat(index, (Float) value);
                    }
                    break;
                }
                case BLOB: {
                    if (value == null) {
                        ps.setNull(index, Types.BLOB);
                    } else {
                        ps.setBinaryStream(index, NLobUtils.toLobInputStream(value));
                    }
                    break;
                }
                case CLOB: {
                    if (value == null) {
                        ps.setNull(index, Types.CLOB);
                    } else {
                        ps.setCharacterStream(index, NLobUtils.toLobReader(value));
                    }
                    break;
                }
                case JAVA_OBJECT: {
                    if (value == null) {
                        ps.setNull(index, Types.JAVA_OBJECT);
                    } else {
                        ps.setObject(index, value);
                    }
                    break;
                }
                default: {
                    throw new UncheckedIOException(new IOException("Unsupported type for column " + columnName + " value " + value));
                }
            }
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    protected NSqlColumn resolveDefaultSqlColumn(ResultSetMetaData rs, int index) {
        try {
            NSqlColumn c = new NSqlColumn();
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
            throw new UncheckedSqlException(ex);
        }
    }

    public boolean tableConstraintsExists(NSqlTableConstraintsId id) {
        NLog.of(getClass()).error(NMsg.ofC("[%s] not yet supported tableConstraintsExists", id.getFullName()));
        throw new IllegalArgumentException("not supported tableConstraintsExists");
    }

    public void createTableConstraints(NSqlTableConstraintsDefinition def) {
        NLog.of(getClass()).error(NMsg.ofC("[%s] not yet supported createTableConstraints", def.getTableConstraintsId().getFullName()));
        throw new IllegalArgumentException("not supported createTableConstraints");
    }

    public void dropTableConstraints(NSqlTableConstraintsId id) {
        NLog.of(getClass()).error(NMsg.ofC("[%s] not yet supported createTableConstraints", id.getFullName()));
        throw new IllegalArgumentException("not supported createTableConstraints");
    }

    public boolean dropTableConstraintsIfExists(NSqlTableConstraintsId id) {
        if (tableConstraintsExists(id)) {
            dropTableConstraints(id);
            return true;
        }
        return false;
    }

    protected ResultSet logResultSetCall(String name, NSqlSupplier<ResultSet> r) {
        LOG.log(Level.SEVERE, "[SQL-SUPPLY] " + name);
        try {
            return r.get();
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public boolean patchColumn(NSqlColumn col) {
        if (columnExists(col.getColumnId())) {
            NSqlColumn old = getSqlColumn(col.getColumnId());
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

    public void createColumn(NSqlColumn col) {
        NSqlTableId table = col.toTableId();
        executeUpdate(table,
                "alter table "
                        + escapeIdentifier(table)
                        + " add column "
                        + resolveSqlSqlColumn(col)
        );
    }

    public boolean tableExists(NSqlTableId c) {
        return getTableDefinition(c) != null;
    }

    public boolean catalogExists(NSqlCatalogId c) {
        try (NSafeResultSet rs = new NSafeResultSet(logResultSetCall("getCatalogs()", () -> getConnection().getMetaData().getCatalogs()))) {
            return rs.readStream(NResultSetMappers.ofString("TABLE_CAT")).anyMatch(x -> Objects.equals(c.getCatalogName(), x));
        }
    }

    public boolean schemaExists(NSqlSchemaId s) {
        try (NSafeResultSet rs = new NSafeResultSet(logResultSetCall("getSchemas()", () -> getConnection().getMetaData().getSchemas()))) {
            return rs.readStream(x -> {
                try {
                    return new NSqlTableId(
                            x.getString("TABLE_CATALOG"),
                            x.getString("TABLE_SCHEM"),
                            "ANY"
                    );
                } catch (SQLException e) {
                    throw new UncheckedSqlException(e);
                }
            }).anyMatch(x -> {
                if (!NBlankable.isBlank(s.getCatalogName())) {
                    if (!Objects.equals(s.toCatalogId().getCatalogName(), x.getCatalogName())) {
                        return false;
                    }
                }
                if (!NBlankable.isBlank(s.getSchemaName())) {
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

    public NSqlConnection setMaxVarcharLength(long maxVarcharLength) {
        this.maxVarcharLength = maxVarcharLength;
        return this;
    }

    public boolean dropTableIfExists(NSqlTableId tableId) {
        if (tableExists(tableId)) {
            dropTable(tableId);
            return true;
        }
        return false;
    }

    public boolean createTableConstraintsIfNotExists(NSqlTableConstraintsDefinition def) {
        if (!tableConstraintsExists(def.getTableConstraintsId())) {
            createTableConstraints(def);
            return true;
        }
        return false;
    }

    public boolean createTableIfNotExists(NSqlTableDefinition def) {
        if (!tableExists(def.getTableId())) {
            createTable(def);
            return true;
        }
        return false;
    }

    public boolean patchTable(NSqlTableDefinition def, NSqlPatchTableOptions options) {
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

            NSqlTableDefinition newMd = def;
            NSqlTableId newTable = null;
            newTable = new NSqlTableId(getSchemaId(), newMd.getTableName());
            NSqlTableDefinition oldMd = this.getTableDefinition(newTable);
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
                List<NSqlColumn> newCols = newMd.getColumns();
                List<NSqlColumn> oldCols = oldMd.getColumns();
                for (NSqlColumn newCol : newCols) {
                    NSqlColumn oldCol = oldCols.stream().filter(x -> x.getColumnName().equals(newCol.getColumnName())).findAny().orElse(null);
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
                for (NSqlColumn oldCol : oldCols) {
                    NSqlColumn newCol = newCols.stream().filter(x -> x.getColumnName().equals(oldCol.getColumnName())).findAny().orElse(null);
                    if (newCol == null) {
                        if (options.isDropColumn()) {
                            this.dropColumn(oldCol.getColumnId());
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

    public boolean addColumnIfNotExists(NSqlColumn col) {
        if (!columnExists(col.getColumnId())) {
            createColumn(col);
            return true;
        }
        return false;
    }

    public boolean dropAndCreateDatabase(String s) {
        boolean d = dropDatabaseIfExists(s);
        createDatabase(s);
        return d;
    }

    public boolean dropDatabaseIfExists(String s) {
        if (databaseExists(s)) {
            dropDatabase(s);
            return true;
        }
        return false;
    }

    public NDatabaseItemQuery search() {
        return new DefaultDatabaseItemQuery(this);
    }

    public void enableConstraints(NSqlTableDefinition d, NSqlPatchTableOptions schemaMode) {
        NLog.of(getClass()).error(NMsg.ofC("[%s] not yet supported enableConstraints", d.getTableId().getFullName()));
    }

    public void disableConstraints(NSqlTableDefinition d, NSqlPatchTableOptions schemaMode) {
        NLog.of(getClass()).error(NMsg.ofC("[%s] not yet supported disableConstraints", d.getTableId().getFullName()));
    }

    public NQueryResult executeQuery(URL resource) {
        return executeQuery(new String(NIOUtils.readBytes(resource)));
    }

    public NSqlQuery query(String sql) {
        return query().append(sql);
    }

    public NSqlQuery query() {
        return new DefaultNSqlQuery(this);
    }

    public NQueryResult executeQuery(String sql) {
        Statement s = null;
        try {
            s = getConnection().createStatement();
            Statement finalS = s;
            return new ResultSetQueryResult(s.executeQuery(sql), () -> {
                try {
                    finalS.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (SQLException ex) {
            if (s != null) {
                try {
                    s.close();
                } catch (SQLException e) {
                    //
                }
            }
            throw new UncheckedSqlException(ex);
        }
    }

//    public long executeUpdate(String sql) {
//        Statement s = null;
//        try {
//            s = getConnection().createStatement();
//            Statement finalS = s;
//            return s.executeUpdate(sql);
//        } catch (SQLException ex) {
//            if (s != null) {
//                try {
//                    s.close();
//                } catch (SQLException e) {
//                    //
//                }
//            }
//            throw new UncheckedSqlException(ex);
//        }
//    }

    public String resolveSqlTypeDefinition(NSqlColumn def) {
        switch (def.getColumnType()) {
            case STRING: {
                if (getMaxVarcharLength() > 0) {
                    if (def.getScale() > getMaxVarcharLength()) {
                        def = def.copy();
                        def.setColumnType(NSqlColumnType.CLOB);
                        return resolveSqlTypeDefinition(def);
                    }
                }
                return "VARCHAR(" + def.getScale() + ")";
            }
            case BIGDECIMAL:
            case BIGINT: {
                return "NUMERIC(" + def.getScale() + "," + def.getPrecision() + ")";
            }
            case INT: {
                return "INT";
            }
            case LONG: {
                return "LONG";
            }
            case FLOAT: {
                return "FLOAT";
            }
            case DOUBLE: {
                return "DOUBLE";
            }
            case BOOLEAN: {
                return "BOOLEAN";
            }
            case SHORT: {
                return "SHORT";
            }
            case BYTE: {
                return "BYTE";
            }
            case DATE: {
                return "DATE";
            }
            case TIMESTAMP: {
                return "TIMESTAMP";
            }
            case TIME: {
                return "TIME";
            }
            case JAVA_OBJECT: {
                def = def.copy();
                def.setColumnType(NSqlColumnType.BLOB);
                return resolveSqlTypeDefinition(def);
            }
            case BLOB: {
                return "BLOB";
            }
            case CLOB: {
                return "CLOB";
            }
            default: {
                throw new UncheckedSqlException(new SQLException("unsupported " + def.getColumnType()));
            }
        }
    }

    public String resolveSqlSqlColumn(NSqlColumn def) {
        return escapeIdentifier(def.getColumnName()) + " " + resolveSqlTypeDefinition(def);
    }

    public String resolveColumnJavaType(NSqlColumn c) {
        NSqlColumnType u = resolveColumnType(c);
        switch (u) {
            case INT:
                return "java.lang.Integer";
            case BOOLEAN:
                return "java.lang.Boolean";
            case DECIMAL:
                return "java.util.BigDecimal";
            case DATE:
                return "java.sql.Date";
            case TIMESTAMP:
                return "java.sql.Timestamp";
            case TIME:
                return "java.sql.Time";
            case LONG:
                return "java.lang.Long";
            case FLOAT:
                return "java.lang.Float";
            case CLOB:
                return "java.io.Reader";
            case BLOB:
                return "java.io.InputStream";
            case NULL:
                return null;
            case SHORT:
                return "java.lang.Short";
            case DOUBLE:
                return "java.lang.Double";
            case STRING:
                return "java.lang.String";
            case BIGINT:
                return "java.util.BigInteger";
            case BYTE:
                return "java.lang.Byte";
            case JAVA_OBJECT:
                return "java.lang.Object";
            case BIGDECIMAL:
                return "java.util.BigDecimal";
        }
        return "java.lang.Object";
    }

    public NSqlColumnType resolveColumnType(NSqlColumn c) {
        int precision = c.getPrecision();
        boolean dec = precision > 0;
        switch (c.getSqlType()) {
            case Types.BIT:
                return NSqlColumnType.BOOLEAN;
            case Types.TINYINT:
                return NSqlColumnType.BYTE;
            case Types.SMALLINT:
                return NSqlColumnType.SHORT;
            case Types.INTEGER: {
                if (dec) {
                    //some drivers are so dump
                    return NSqlColumnType.BIGDECIMAL;
                }
                return NSqlColumnType.INT;
            }
            case Types.BIGINT: {
                if (dec) {
                    //some drivers are so dump
                    return NSqlColumnType.BIGDECIMAL;
                }
                if ("java.lang.Long".equals(c.getJavaClassName())) {
                    return NSqlColumnType.LONG;
                }
                return NSqlColumnType.BIGINT;
            }
            case Types.FLOAT:
                return NSqlColumnType.DOUBLE;
            case Types.REAL:
                return NSqlColumnType.DOUBLE;
            case Types.DOUBLE:
                return NSqlColumnType.DOUBLE;
            case Types.NUMERIC: {
                if (dec) {
                    return NSqlColumnType.BIGDECIMAL;
                } else {
                    return NSqlColumnType.BIGINT;
                }
            }
            case Types.DECIMAL: {
                return NSqlColumnType.BIGDECIMAL;
            }
            case Types.CHAR: {
                return NSqlColumnType.STRING;
            }
            case Types.VARCHAR: {
                return NSqlColumnType.STRING;
            }
            case Types.LONGVARCHAR: {
                return NSqlColumnType.STRING;
            }
            case Types.DATE: {
                return NSqlColumnType.DATE;
            }
            case Types.TIME: {
                return NSqlColumnType.TIME;
            }
            case Types.TIMESTAMP: {
                return NSqlColumnType.TIMESTAMP;
            }
            case Types.BINARY: {
                return NSqlColumnType.BLOB;
            }
            case Types.VARBINARY: {
                return NSqlColumnType.BLOB;
            }
            case Types.LONGVARBINARY: {
                return NSqlColumnType.BLOB;
            }
            case Types.NULL: {
                return NSqlColumnType.NULL;
            }
            case Types.OTHER:
            case Types.JAVA_OBJECT:
            case Types.STRUCT:
            case Types.ARRAY: {
                return NSqlColumnType.JAVA_OBJECT;
            }
            case Types.BLOB: {
                return NSqlColumnType.BLOB;
            }
            case Types.CLOB: {
                return NSqlColumnType.BLOB;
            }
            case Types.REF:
            case Types.DATALINK:
            case Types.REF_CURSOR: {
                return NSqlColumnType.JAVA_OBJECT;
            }
            case Types.BOOLEAN: {
                return NSqlColumnType.BOOLEAN;
            }
            case Types.NCHAR: {
                return NSqlColumnType.STRING;
            }
            case Types.NVARCHAR: {
                return NSqlColumnType.STRING;
            }
            case Types.LONGNVARCHAR: {
                return NSqlColumnType.STRING;
            }
            case Types.NCLOB: {
                return NSqlColumnType.CLOB;
            }
            case Types.SQLXML: {
                return NSqlColumnType.CLOB;
            }
            case Types.TIME_WITH_TIMEZONE: {
                return NSqlColumnType.TIME;
            }
            case Types.TIMESTAMP_WITH_TIMEZONE: {
                return NSqlColumnType.TIMESTAMP;
            }
            default: {
                throw new IllegalArgumentException("unsupported");
            }

        }
    }

    public long reindexTable(NSqlTableId nSqlTableId) {
        return 0;
    }

}
