package net.thevpc.nsql.mapper;

import net.thevpc.nsql.NSqlColumnReaders;
import net.thevpc.nsql.UncheckedSqlException;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class NResultSetMappers {
    public static <T> NResultSetMapper<T> ofType(Class<T> type, ResultSetMetaData md) {
        return new ReflectResultSetMapper<>(type, md, DefaultNResultSetMapperFactory.INSTANCE);
    }

    public static NResultSetMapper<String> ofString(String col) {
        return x -> NSqlColumnReaders.STRING.read(x,col);
    }

    public static NResultSetMapper<Integer> ofInt(String col) {
        return x -> NSqlColumnReaders.INT.read(x,col);
    }

    public static NResultSetMapper<String> ofString(int col) {
        return x -> NSqlColumnReaders.STRING.read(x,col);
    }

    public static NResultSetMapper<Integer> ofInt(int col) {
        return rs -> {
            try {
                int u = rs.getInt(col);
                if (rs.wasNull()) {
                    return null;
                }
                return u;
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        };
    }
}
