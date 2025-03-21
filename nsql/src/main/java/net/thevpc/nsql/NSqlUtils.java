package net.thevpc.nsql;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NSqlUtils {

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

    public static final <T> Stream<T> concatStreams(List<Stream<T>> all){
        if(all.isEmpty()){
            return (Stream) Collections.emptyList().stream();
        }
        Stream<T> s=all.get(0);
        for (int i = 1; i <all.size(); i++) {
            s=Stream.concat(s,all.get(i));
        }
        return s;
    }
    public static final <T> Stream<T> toStream(Iterator<T> sourceIterator){
        return  StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(sourceIterator, Spliterator.ORDERED),
                false);
    }

}
