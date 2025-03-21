package net.thevpc.nsql;

import java.io.Closeable;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.stream.Stream;

public interface NQueryResult extends Closeable {
    Optional<NSqlRow> first();

    Stream<NSqlRow> stream();

    Iterable<NSqlRow> iterable();

    ResultSet getResultSet();

    void close();

//    void writeCsv(Out file, SqlRowConversionContext ccontext);
//    void writeJsonArrays(Out file, SqlRowConversionContext ccontext);
//    void writeJsonObjects(Out file, SqlRowConversionContext ccontext);
}
