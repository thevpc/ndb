package net.thevpc.nsql;

import net.thevpc.nuts.util.NOptional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class NSqlUpdateCallerContext<T, V> {
    private T userObject;
    private NSqlConnection c;
    private PreparedStatement rs;
    private V resultValue;
    private long result;

    public NSqlUpdateCallerContext(T userObject, NSqlConnection c, long result, PreparedStatement ps) {
        this.userObject = userObject;
        this.c = c;
        this.rs = ps;
        this.result = result;
    }

    public long getResult() {
        return result;
    }

    public T userObject() {
        return userObject;
    }

    public NSqlConnection connection() {
        return c;
    }

    public PreparedStatement ps() {
        return rs;
    }

    public NSqlUpdateCallerContext<T, V> terminate(V resultValue) {
        this.resultValue = resultValue;
        return this;
    }

    public NOptional<Integer> getGeneratedIntKey() {
        return NOptional.ofFirst(getGeneratedIntKeys());
    }

    public List<Integer> getGeneratedIntKeys() {
        return getGeneratedIntKeys(rs -> {
            try {
                return rs.getInt(1);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public NOptional<Long> getGeneratedLongKey() {
        return NOptional.ofFirst(getGeneratedLongKeys());
    }

    public List<Long> getGeneratedLongKeys() {
        return getGeneratedIntKeys(rs -> {
            try {
                return rs.getLong(1);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public <T> List<T> getGeneratedIntKeys(Function<ResultSet, T> mapper) {
        List<T> values = new ArrayList<>();
        try (ResultSet generatedKeys = ps().getGeneratedKeys()) {
            while (generatedKeys.next()) {
                values.add(mapper.apply(generatedKeys));
            }
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
        return values;
    }

    public V getResultValue() {
        return resultValue;
    }

}
