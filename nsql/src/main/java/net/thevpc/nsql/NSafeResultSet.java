package net.thevpc.nsql;

import net.thevpc.nsql.mapper.NResultSetMapper;

import java.io.Closeable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NSafeResultSet implements Closeable {
    private ResultSet rs;

    public NSafeResultSet(ResultSet rs) {
        this.rs = rs;
    }

    public <T> Iterator<T> readIterator(NResultSetMapper<T> t) {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                try {
                    return rs.next();
                } catch (SQLException e) {
                    throw new UncheckedSqlException(e);
                }
            }

            @Override
            public T next() {
                return t.get(rs);
            }
        };
    }

    public <T> Stream<T> readStream(NResultSetMapper<T> t) {
        Iterable<T> iterable = () -> readIterator(t);
        Stream<T> targetStream = StreamSupport.stream(iterable.spliterator(), false);
        return targetStream;
    }

    public <T> List<T> readList(NResultSetMapper<T> t) {
        List<T> all = new ArrayList<>();
        while (true) {
            try {
                if (!rs.next()) break;
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
            all.add(t.get(rs));
        }
        return all;
    }


    public <T> Optional<T> read(NResultSetMapper<T> t) {
        try {
            if (rs.next()) {
                return Optional.of(get(t));
            }
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
        return Optional.empty();
    }

    public <T> T get(NResultSetMapper<T> t) {
        return t.get(rs);
    }

    public ResultSet getResultSet() {
        return rs;
    }

    public String getString(String n) {
        try {
            return rs.getString(n);
        } catch (SQLException e) {
            return null;
        }
    }

    public Integer getInt(String n) {
        try {
            int i = rs.getInt(n);
            return rs.wasNull()?null:i;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public void close() {
        try {
            rs.close();
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }
}
