package net.thevpc.nsql;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NNameFormat;

public enum NSqlColumnType {
    STRING,
    BIGINT,
    BIGDECIMAL,
    DECIMAL,
    INT,
    LONG,
    DOUBLE,
    FLOAT,
    DATE,
    TIME,
    TIMESTAMP,
    BOOLEAN,
    BLOB,
    CLOB,
    SHORT,
    BYTE,
    JAVA_OBJECT,
    NULL,
    ;

    public static NSqlColumnType of(Class any) {
        if (any == null) {
            return null;
        }
        switch (any.getName()) {
            case "boolean":
            case "java.lang.Boolean": {
                return BOOLEAN;
            }
            case "int":
            case "java.lang.Integer": {
                return INT;
            }
            case "long":
            case "java.lang.Long": {
                return LONG;
            }
            case "double":
            case "java.lang.Double": {
                return DOUBLE;
            }
            case "java.math.BigInteger": {
                return BIGINT;
            }
            case "java.math.BigDecimal": {
                return BIGDECIMAL;
            }
            case "java.sql.Date": {
                return DATE;
            }
            case "java.sql.Time": {
                return TIME;
            }
            case "java.util.Date":
            case "java.sql.Timestamp": {
                return TIMESTAMP;
            }
            case "java.lang.String": {
                return STRING;
            }
        }
        throw new IllegalArgumentException("unsupported type " + any);
    }

    public static NSqlColumnType parse(String any) {
        if (!NBlankable.isBlank(any)) {
            switch (NNameFormat.LOWER_KEBAB_CASE.format(any.trim())) {
                case "string":
                case "varchar":
                    return STRING;
                case "double":
                case "real":
                    return DOUBLE;
                case "timestamp":
                    return TIMESTAMP;
                case "bigint":
                case "big-int":
                    return BIGINT;
                case "bigdecimal":
                case "big-decimal":
                    return BIGDECIMAL;
                case "decimal":
                    return DECIMAL;
                case "date":
                    return DATE;
                case "time":
                    return TIME;
                case "long":
                    return LONG;
                case "boolean":
                    return BOOLEAN;
            }
            throw new IllegalArgumentException("unsupported type " + any);
        }
        return null;
    }
}
