package net.thevpc.nsql.db;

import net.thevpc.nsql.*;
import net.thevpc.nsql.model.NSqlDatabaseHeader;
import net.thevpc.nsql.model.NSqlDatabaseHeaderImpl;
import net.thevpc.nsql.model.NSqlTableId;
import net.thevpc.nsql.model.YesNo;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.text.NMsg;

import java.io.InputStream;
import java.io.Reader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OracleSqlConnection extends NSqlConnection {
    private static Logger LOG = Logger.getLogger(OracleSqlConnection.class.getName());

    public OracleSqlConnection(NSqlConnectionFactory connectionFactory, Connection connection,boolean sharedConnection) {
        super(connectionFactory, connection,sharedConnection);
        setMaxVarcharLength(10485760);
    }

    @Override
    protected String createTableDdl(NSqlTable jdbcTable) {
        String validTableName = NBlankable.isBlank(jdbcTable.getTableName()) ? "hal_packet" : jdbcTable.getTableName();
// "CREATE TABLE IF NOT EXISTS packets( id SERIAL PRIMARY KEY,protocol varchar(20) NOT NULL,added_at TIMESTAMP,packet_type int NOT NULL ,latitude REAL,longitude REAL,speed REAL,terminal_id BIGINT,raw_bytes VARCHAR(1000));"
        StringBuilder ddl = new StringBuilder();
        ddl.append("CREATE TABLE IF NOT EXISTS ")
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

    public String getDatabaseName() {
        return executeQuery("SELECT SYS_CONTEXT('USERENV','DB_NAME') FROM DUAL").first().map(x -> x.getString(1)).get();
    }

    protected String quotedIdentifier(String s) {
        return "\"" + s + "\"";
    }

    public String resolveSqlTypeDefinition(NSqlColumn def) {
        switch (def.getColumnType()) {
            case CLOB: {
                return "TEXT";
            }
            case BLOB: {
                return "BYTEA";
            }
            case BYTE: {
                return "NUMERIC(1,0)";
            }
            case SHORT: {
                return "int2";
            }
            case INT: {
                return "int4";
            }
            case LONG: {
                return "int8";
            }
            case DOUBLE: {
                return "double precision";
            }
            case FLOAT: {
                return "real";
            }
        }
        return super.resolveSqlTypeDefinition(def);
    }

    @Override
    public NSqlColumnType resolveColumnType(NSqlColumn c) {
        switch (c.getSqlType()) {
            case Types.BIGINT: {
                String n = c.getSqlTypeName();
                if (n != null) {
                    switch (n) {
                        case "int8": {
                            return NSqlColumnType.LONG;
                        }
                        case "int4": {
                            return NSqlColumnType.INT;
                        }
                        case "int2": {
                            return NSqlColumnType.SHORT;
                        }
                    }
                }
            }
            case Types.OTHER: {
                String n = c.getSqlTypeName();
                if ("json".equals(n)) {
                    return NSqlColumnType.STRING;
                }
                break;
            }
        }
        return super.resolveColumnType(c);
    }

    public void prepareStatement(PreparedStatement ps, int index, NSqlColumnType st, String columnName, Object value, NPrepareStatementContext prepareStatementContext) {
        try {
            switch (st) {
                case STRING: {
                    if (value == null) {
                        ps.setNull(index, Types.VARCHAR);
                    } else {
                        if ((value instanceof InputStream || value instanceof Reader) && prepareStatementContext.isExternalLob()) {
                            super.prepareStatement(ps, index, st, columnName, value, prepareStatementContext);
                        } else {
                            String s = (String) value;
                            //just remove invalid \0
                            int i = s.indexOf('\0');
                            if (i >= 0) {
                                if (i == s.length() - 1) {
                                    s = s.substring(0, s.length() - 1);
                                } else {
                                    LOG.log(Level.SEVERE, "[" + st + "," + index + "] prepare statement : string with invalid zero char in " + columnName);
                                }
                            }
                            ps.setString(index, s);
                        }
                    }
                    break;
                }
                case BLOB: {
                    if (value == null) {
                        //BUG FIX
                        ps.setNull(index, Types.OTHER);
                    } else {
                        ps.setBytes(index, NLobUtils.toLobByteArray(value));
                    }
                    break;
                }
                default: {
                    super.prepareStatement(ps, index, st, columnName, value, prepareStatementContext);
                }
            }
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public boolean databaseExists(String dbname) {
        try (PreparedStatement s = getConnection().prepareStatement("SELECT datname FROM pg_database where datallowconn is true and datistemplate is false and datname=?")) {
            s.setString(1, dbname);
            try (ResultSet rs = s.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
                return false;
            }
        } catch (SQLException ex) {
            throw new UncheckedSqlException(ex);
        }
    }

    public List<NSqlDatabaseHeader> getDatabases() {
        try (Statement s = getConnection().createStatement()) {
            try (ResultSet rs = s.executeQuery("SELECT datname FROM pg_database where datallowconn is true and datistemplate is false")) {
                List<NSqlDatabaseHeader> h = new ArrayList<>();
                while (rs.next()) {
                    h.add(new NSqlDatabaseHeaderImpl(null, null, rs.getString(1)));
                }
                return h;
            }
        } catch (SQLException ex) {
            throw new UncheckedSqlException(ex);
        }
    }

    public void dropDatabase(String s) {
        executeUpdate("drop database " + s + " WITH (FORCE) ");
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


    @Override
    public long reindexTable(NSqlTableId nSqlTableId) {
        Set<String> indexNames = query("Select index_name FROM user_indexes WHERE table_name = :tableName")
                .setParam(NSqlParam.ofString("tableName", nSqlTableId.getTableName().toUpperCase()))
                .executeQuery().stream().map(x -> {
                    return x.getString(0);
                }).toSet();
        long count = 0;
        for (String indexName : indexNames) {
            String q = NMsg.ofC("ALTER INDEX %s REBUILD", indexName).toString();
            count += executeUpdate(q);
        }
        return count;
    }
}
