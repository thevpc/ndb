package net.thevpc.nsql.db;

import net.thevpc.nsql.*;
import net.thevpc.nsql.model.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.text.NMsg;

import java.sql.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MsSqlServerConnection extends NSqlConnection {
    private static Logger LOG = Logger.getLogger(MsSqlServerConnection.class.getName());

    public MsSqlServerConnection(NSqlConnectionFactory connectionFactory, Connection connection) {
        super(connectionFactory, connection);
    }

    @Override
    public boolean isSpecialTable(NSqlTableId tableId) {
        if (tableId.getTableName().equals("trace_xe_action_map")) {
            return true;
        }
        if (tableId.getTableName().equals("trace_xe_event_map")) {
            return true;
        }
        return super.isSpecialTable(tableId);
    }

    protected String getDefaultDatabaseName() {
        return "master";
    }

    public String getDatabaseName() {
        return executeQuery("SELECT DB_NAME()").first().map(x -> x.getString(1)).get();
    }


    @Override
    public long getApproximateTableCount(NSqlTableId table) {
        try (Statement s = getConnection().createStatement()) {
            String sql = "SELECT SUM(row_count) FROM sys.dm_db_partition_stats WHERE object_id = OBJECT_ID('"+table.getFullName()+"') AND (index_id = 0 OR index_id = 1)";
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

    public List<NSqlDatabaseHeader> getDatabases() {
        return getCatalogs().stream().map(x -> new NSqlDatabaseHeaderImpl(
                x.getCatalogName(),
                null,
                x.getCatalogName()
        )).collect(Collectors.toList());
    }

    public String escapeIdentifier(String name) {
        return "[" + name + "]";
    }

    @Override
    protected String createTableDdl(NSqlTable jdbcTable) {
        String validTableName = NBlankable.isBlank(jdbcTable.getTableName()) ? "no_table" : jdbcTable.getTableName();
        // "CREATE TABLE IF NOT EXISTS packets( id SERIAL PRIMARY KEY,protocol varchar(20) NOT NULL,added_at TIMESTAMP,packet_type int NOT NULL ,latitude REAL,longitude REAL,speed REAL,terminal_id BIGINT,raw_bytes VARCHAR(1000));"
        StringBuilder ddl = new StringBuilder();
        ddl.append("IF OBJECT_ID(N'[dbo].[")
                .append(validTableName)
                .append("]', N'U') IS NULL \nBEGIN\n")
                .append("CREATE TABLE ")
                .append(validTableName)
                .append(" (");
        boolean first = true;
        for (NSqlColumn value : jdbcTable.getColumns()) {
            if (first) {
                first = false;
            } else {
                ddl.append(",");
            }
            ddl.append(" ").append(value.columnName).append(" ").append(createTypeDefinition(value));
        }
        ddl.append(");\n");
        ddl.append("END; ");
        return ddl.toString();
    }

    protected String quotedIdentifier(String s) {
        return "[" + s + "]";
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
                    sb.append(nbr + " NOT NULL AUTO_INCREMENT");
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
                    sb.append(nbr + " NOT NULL AUTO_INCREMENT");
                } else {
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
                    sb.append("INT NOT NULL AUTO_INCREMENT");
                } else {
                    sb.append("INT").append(nullDef);
                }
                break;
            }
            case LONG: {
                if (pkey) {
                    sb.append("BIGINT NOT NULL AUTO_INCREMENT");
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
                sb.append("bit").append(nullDef);
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

    public NSqlColumnType resolveColumnType(NSqlColumn c) {
        switch (c.getSqlType()) {
            case Types.TINYINT:
                // unsigned 0-255!!
                return NSqlColumnType.SHORT;
        }
        return super.resolveColumnType(c);
    }

    @Override
    public long reindexTable(NSqlTableId nSqlTableId) {
        String q = NMsg.ofC("ALTER INDEX ALL ON %s REBUILD",nSqlTableId.toString()).toString();
        return executeUpdate(q);
    }
}
