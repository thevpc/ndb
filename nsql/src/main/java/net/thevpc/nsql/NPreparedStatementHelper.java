package net.thevpc.nsql;

import net.thevpc.nuts.util.NLiteral;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

public class NPreparedStatementHelper {
    public static void set(Object value, int index, NSqlColumnType columnType, PreparedStatement ps) {
        try {
            switch (columnType) {
                case STRING: {
                    String sValue = null;
                    if (value != null) {
                        sValue = value.toString();
                    }
                    ps.setString(index, sValue);
                    break;
                }
                case TIMESTAMP: {
                    Timestamp sValue = null;
                    if (value != null) {
                        if (value instanceof Timestamp) {
                            sValue = (Timestamp) value;
                        } else if (value instanceof Date) {
                            sValue = new Timestamp(((Date) value).getTime());
                        } else {
                            throw new IllegalArgumentException("expected Timestamp :" + value);
                        }
                    }
                    ps.setTimestamp(index, sValue);
                    break;
                }
                case DATE: {
                    java.sql.Date sValue = null;
                    if (value != null) {
                        if (value instanceof java.sql.Date) {
                            sValue = (java.sql.Date) value;
                        } else if (value instanceof Date) {
                            sValue = new java.sql.Date(((Date) value).getTime());
                        } else {
                            throw new IllegalArgumentException("expected java.sql.Date :" + value);
                        }
                    }
                    ps.setDate(index, sValue);
                    break;
                }
                case TIME: {
                    java.sql.Time sValue = null;
                    if (value != null) {
                        if (value instanceof java.sql.Time) {
                            sValue = (java.sql.Time) value;
                        } else if (value instanceof Date) {
                            sValue = new java.sql.Time(((Date) value).getTime());
                        } else {
                            throw new IllegalArgumentException("expected java.sql.Date :" + value);
                        }
                    }
                    ps.setTime(index, sValue);
                    break;
                }
                case BIGINT: {
                    BigInteger sValue = value == null ? null : NLiteral.of(value).asBigIntValue().get();
                    ps.setBigDecimal(index, sValue == null ? null : new BigDecimal(sValue));
                    break;
                }
                case DECIMAL:
                case BIGDECIMAL: {
                    BigDecimal sValue = value == null ? null : NLiteral.of(value).asBigDecimalValue().get();
                    ps.setBigDecimal(index, sValue);
                    break;
                }
                case INT: {
                    Integer sValue = value == null ? null : NLiteral.of(value).asIntValue().get();
                    if (sValue == null) {
                        ps.setNull(index, Types.INTEGER);
                    } else {
                        ps.setInt(index, sValue);
                    }
                    break;
                }
                case LONG: {
                    Long sValue = value == null ? null : NLiteral.of(value).asLongValue().get();
                    if (sValue == null) {
                        ps.setNull(index, Types.BIGINT);
                    } else {
                        ps.setLong(index, sValue);
                    }
                    break;
                }
                case DOUBLE: {
                    Double sValue = value == null ? null : NLiteral.of(value).asDoubleValue().get();
                    if (sValue == null) {
                        ps.setNull(index, Types.DOUBLE);
                    } else {
                        ps.setDouble(index, sValue);
                    }
                    break;
                }
                case BOOLEAN: {
                    Boolean sValue = value == null ? null : NLiteral.of(value).asBooleanValue().get();
                    if (sValue == null) {
                        ps.setNull(index, Types.BOOLEAN);
                    } else {
                        ps.setBoolean(index, sValue);
                    }
                    break;
                }
                default: {
                    throw new IllegalArgumentException("unsupported type :" + columnType);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
