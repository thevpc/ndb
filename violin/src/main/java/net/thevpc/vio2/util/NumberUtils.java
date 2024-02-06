package net.thevpc.vio2.util;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberUtils {
    public static BigInteger asBigInteger(Number b) {
        if (b == null) {
            return null;
        }
        if (b instanceof BigInteger) {
            return (BigInteger) b;
        }
        if (b instanceof BigDecimal) {
            return ((BigDecimal) b).toBigInteger();
        }
        return BigInteger.valueOf(b.longValue());
    }

    public static BigDecimal asBigDecimal(Number b) {
        if (b == null) {
            return null;
        }
        if (b instanceof BigDecimal) {
            return (BigDecimal) b;
        }
        if (b instanceof BigInteger) {
            return new BigDecimal(((BigInteger) b));
        }
        return BigDecimal.valueOf(b.doubleValue());
    }
}
