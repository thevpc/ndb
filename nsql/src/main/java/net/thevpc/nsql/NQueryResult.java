package net.thevpc.nsql;

import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.pipeline.NStream;

import java.io.Closeable;
import java.sql.ResultSet;

public interface NQueryResult extends Closeable {
    NOptional<NSqlRow> first();

    NStream<NSqlRow> stream();

    Iterable<NSqlRow> iterable();

    ResultSet getResultSet();

    void close();

//    void writeCsv(Out file, SqlRowConversionContext ccontext);
//    void writeJsonArrays(Out file, SqlRowConversionContext ccontext);
//    void writeJsonObjects(Out file, SqlRowConversionContext ccontext);
}
