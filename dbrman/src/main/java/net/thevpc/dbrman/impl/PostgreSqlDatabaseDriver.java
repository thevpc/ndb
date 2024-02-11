/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.dbrman.impl;

import net.thevpc.dbrman.api.DatabaseHeader;
import net.thevpc.dbrman.model.ColumnDefinition;
import net.thevpc.dbrman.common.AbstractDatabaseDriver;
import net.thevpc.dbrman.model.DefaultDatabaseHeader;
import net.thevpc.dbrman.model.SchemaId;
import net.thevpc.dbrman.util.UncheckedSQLException;
import net.thevpc.vio2.model.StoreDataType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author vpc
 */
public class PostgreSqlDatabaseDriver extends AbstractDatabaseDriver {

    public static Logger LOG = Logger.getLogger(PostgreSqlDatabaseDriver.class.getName());

    public PostgreSqlDatabaseDriver(Connection connection) {
        super(connection);
        setMaxVarcharLength(10485760);
    }

    public String resolveSqlTypeDefinition(ColumnDefinition def) {
        switch (def.getStoreType()) {
            case CHAR_STREAM:
            case NCHAR_STREAM: {
                return "TEXT";
            }
            case BYTES:
            case NBYTES:
            case BYTE_STREAM:
            case NBYTE_STREAM: {
                return "BYTEA";
            }
            case BYTE:
            case NBYTE: {
                return "NUMERIC(1,0)";
            }
            case SHORT:
            case NSHORT: {
                return "int2";
            }
            case INT:
            case NINT: {
                return "int4";
            }
            case LONG:
            case NLONG: {
                return "int8";
            }
            case DOUBLE:
            case NDOUBLE: {
                return "double precision";
            }
            case FLOAT:
            case NFLOAT: {
                return "real";
            }
        }
        return super.resolveSqlTypeDefinition(def);
    }

    protected StoreDataType createFileColType(ColumnDefinition c) {
        switch (c.getSqlType()) {
            case Types.BIGINT: {
                String n = c.getSqlTypeName();
                if (n != null) {
                    switch (n) {
                        case "int8": {
                            return StoreDataType.LONG;
                        }
                        case "int4": {
                            return StoreDataType.INT;
                        }
                        case "int2": {
                            return StoreDataType.SHORT;
                        }
                    }
                }
            }
            case Types.OTHER: {
                String n = c.getSqlTypeName();
                if ("json".equals(n)) {
                    return StoreDataType.STRING;
                }
                break;
            }
        }
        return createDefaultFileColType(c);
    }

    public void prepareStatement(PreparedStatement ps, int index, StoreDataType st, Object value) {
        try {
            switch (st) {
                case STRING:
                case NSTRING: {
                    if (value == null) {
                        ps.setNull(index, Types.VARCHAR);
                    } else {
                        String s = (String) value;
                        //just remove last \0
                        int i = s.indexOf('\0');
                        if (i >= 0) {
                            if (i == s.length() - 1) {
                                s = s.substring(0, s.length() - 1);
                            } else {
                                LOG.log(Level.SEVERE, "[" + st + "," + index + "] prepare statement : string with invalid zero char");
                            }
                        }
                        ps.setString(index, s);
                    }
                    break;
                }
                default: {
                    super.prepareStatement(ps, index, st, value);
                }
            }
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    @Override
    public boolean databaseExists(String s) {
        return schemaExists(new SchemaId(null, s));
    }

    @Override
    public List<DatabaseHeader> getDatabases() {
        try (Statement s = getConnection().createStatement()) {
            try (ResultSet rs = s.executeQuery("SELECT datname FROM pg_database where datallowconn is true and datistemplate is false")) {
                List<DatabaseHeader> h = new ArrayList<>();
                while (rs.next()) {
                    h.add(new DefaultDatabaseHeader(null, null, rs.getString(1)));
                }
                return h;
            }
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
    }
}
