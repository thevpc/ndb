package net.thevpc.dbrman.impl;

import net.thevpc.dbrman.api.QueryResult;
import net.thevpc.dbrman.api.SqlRow;
import net.thevpc.dbrman.api.SqlRowConversionContext;
import net.thevpc.dbrman.io.Out;
import net.thevpc.dbrman.util.UncheckedSQLException;
import net.thevpc.vio2.util.IOUtils;

import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ResultSetQueryResult implements QueryResult {
    public ResultSet rs;
    public Runnable close;

    public ResultSetQueryResult(ResultSet rs, Runnable close) {
        this.rs = rs;
        this.close = close;
    }

    public ResultSet getResultSet() {
        return rs;
    }


    @Override
    public Optional<SqlRow> first() {
        return stream().findFirst();
    }

    @Override
    public Stream<SqlRow> stream() {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterable().iterator(), Spliterator.ORDERED),
                false);
    }

    @Override
    public Iterable<SqlRow> iterable() {
        ResultSetSqlRow.ResultSetSqlRowContext cc = new ResultSetSqlRow.ResultSetSqlRowContext(rs);
        return new Iterable<SqlRow>() {
            @Override
            public Iterator<SqlRow> iterator() {
                return new Iterator<SqlRow>() {
                    @Override
                    public boolean hasNext() {
                        try {
                            return rs.next();
                        } catch (SQLException ex) {
                            throw new UncheckedSQLException(ex);
                        }
                    }

                    @Override
                    public SqlRow next() {
                        return new ResultSetSqlRow(rs, cc);
                    }
                };
            }
        };
    }

    @Override
    public void close() {
        try {
            rs.close();
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        } finally {
            if (close != null) {
                close.run();
            }
        }
    }

    @Override
    public void writeCsv(Out file, SqlRowConversionContext ccontext) {
        IOUtils.Creatable<PrintStream> ps = file.toPrintStream();
        if (ps == null) {
            ps = new IOUtils.Creatable<>(System.out, false);
        }
        ps.getValue().println(columnsRow().asCsv(ccontext));
        for (SqlRow sqlRow : iterable()) {
            ps.getValue().println(sqlRow.asCsv(ccontext));
        }
    }

    public SqlRow columnsRow(){
        ResultSetMetaData md = null;
        String[] all;
        try {
            md = getResultSet().getMetaData();
            all = new String[md.getColumnCount()];
            for (int i = 0; i < all.length; i++) {
                all[i] = md.getColumnName(i + 1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new ArraySqlRow(all);
    }

    @Override
    public void writeJsonArrays(Out file, SqlRowConversionContext ccontext) {
        IOUtils.Creatable<PrintStream> ps = file.toPrintStream();
        if (ps == null) {
            ps = new IOUtils.Creatable<>(System.out, false);
        }
        ps.getValue().println(columnsRow().asJsonArray(ccontext));
        for (SqlRow sqlRow : iterable()) {
            ps.getValue().println(sqlRow.asJsonArray(ccontext));
        }
    }
    @Override
    public void writeJsonObjects(Out file, SqlRowConversionContext ccontext) {
        IOUtils.Creatable<PrintStream> ps = file.toPrintStream();
        if (ps == null) {
            ps = new IOUtils.Creatable<>(System.out, false);
        }
        for (SqlRow sqlRow : iterable()) {
            ps.getValue().println(sqlRow.asJsonObject(ccontext));
        }
    }

}
