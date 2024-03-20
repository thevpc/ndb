package net.thevpc.dbrman.api;

import net.thevpc.dbrman.io.Out;

import java.io.Closeable;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.stream.Stream;

public interface QueryResult extends Closeable {
    Optional<SqlRow> first();

    Stream<SqlRow> stream();

    Iterable<SqlRow> iterable();

    ResultSet getResultSet();

    void close();

    void writeCsv(Out file, SqlRowConversionContext ccontext);
    void writeJsonArrays(Out file, SqlRowConversionContext ccontext);
    void writeJsonObjects(Out file, SqlRowConversionContext ccontext);
}
