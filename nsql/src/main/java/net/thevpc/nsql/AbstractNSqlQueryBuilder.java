package net.thevpc.nsql;

import net.thevpc.nuts.util.NAssert;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class AbstractNSqlQueryBuilder<T extends AbstractNSqlQueryBuilder> {
    protected List<NSqlParam> params = new ArrayList<>();
    private NSqlParam.Mode mode;

    public T set(int index, NSqlColumnType type, Object value) {
        NAssert.requireNonNull(type, "type");
        NSqlParam p = NSqlParam.of(index, type, value);
        if (mode == null) {
            mode = p.getMode();
        } else if (mode != p.getMode()) {
            throw new IllegalArgumentException("mode mismatch : expected " + p.getMode());
        }
        params.add(p);
        return (T) this;
    }

    public T setString(int index, String value) {
        return set(index, NSqlColumnType.STRING, value);
    }

    public T setDate(int index, java.sql.Date value) {
        return set(index, NSqlColumnType.DATE, value);
    }

    public T setTimestamp(int index, java.sql.Timestamp value) {
        return set(index, NSqlColumnType.TIMESTAMP, value);
    }

    public T setTime(int index, java.sql.Time value) {
        return set(index, NSqlColumnType.TIME, value);
    }

    public T setLong(int index, Number value) {
        return set(index, NSqlColumnType.LONG, value == null ? null : value.longValue());
    }

    public T setInt(int index, Number value) {
        return set(index, NSqlColumnType.INT, value == null ? null : value.intValue());
    }

    public T setDouble(int index, Number value) {
        return set(index, NSqlColumnType.DOUBLE, value == null ? null : value.doubleValue());
    }

    public T setFloat(int index, Number value) {
        return set(index, NSqlColumnType.DOUBLE, value == null ? null : value.floatValue());
    }

    public T setBoolean(int index, Boolean value) {
        return set(index, NSqlColumnType.BOOLEAN, value);
    }

    protected void doPrepare(PreparedStatement ps) {
        if (mode != null) {
            switch (mode) {
                case INDEX: {
                    for (NSqlParam param : params) {
                        NPreparedStatementHelper.set(param.getValue(), param.getColumnIndex(), param.getColumnType(), ps);
                    }
                    break;
                }
                default: {
                    if (!params.isEmpty()) {
                        throw new IllegalArgumentException("not supported yet");
                    }
                }
            }
        }
    }


}
