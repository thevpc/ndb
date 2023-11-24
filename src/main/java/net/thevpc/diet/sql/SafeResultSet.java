package net.thevpc.diet.sql;

import java.io.Closeable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SafeResultSet implements Closeable {
    private ResultSet rs;

    public SafeResultSet(ResultSet rs) {
        this.rs = rs;
    }

    public <T> Iterator<T> readIterator(ResultSetMapper<T> t) {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                try {
                    return rs.next();
                } catch (SQLException e) {
                    throw new UncheckedSQLException(e);
                }
            }

            @Override
            public T next() {
                return t.get(SafeResultSet.this);
            }
        };
    }

    public <T> Stream<T> readStream(ResultSetMapper<T> t) {
        Iterable<T> iterable = () -> readIterator(t);
        Stream<T> targetStream = StreamSupport.stream(iterable.spliterator(), false);
        return targetStream;
    }

    public <T> List<T> readList(ResultSetMapper<T> t) {
        List<T> all = new ArrayList<>();
        while (true) {
            try {
                if (!rs.next()) break;
            } catch (SQLException e) {
                throw new UncheckedSQLException(e);
            }
            all.add(t.get(this));
        }
        return all;
    }


    public <T> Optional<T> read(ResultSetMapper<T> t) {
        try {
            if (rs.next()) {
                return Optional.of(get(t));
            }
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
        return Optional.empty();
    }

    public <T> T get(ResultSetMapper<T> t) {
        return t.get(this);
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

    @Override
    public void close() {
        try {
            rs.close();
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }
}
