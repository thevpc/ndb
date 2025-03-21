package net.thevpc.nsql.impl;

import net.thevpc.nsql.NQueryResult;
import net.thevpc.nsql.NSqlRow;
import net.thevpc.nsql.UncheckedSqlException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ResultSetQueryResult implements NQueryResult {
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
    public Optional<NSqlRow> first() {
        return stream().findFirst();
    }

    @Override
    public Stream<NSqlRow> stream() {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterable().iterator(), Spliterator.ORDERED),
                false);
    }

    @Override
    public Iterable<NSqlRow> iterable() {
        ResultSetSqlRow.ResultSetSqlRowContext cc = new ResultSetSqlRow.ResultSetSqlRowContext(rs);
        return new Iterable<NSqlRow>() {
            @Override
            public Iterator<NSqlRow> iterator() {
                return new Iterator<NSqlRow>() {
                    @Override
                    public boolean hasNext() {
                        try {
                            return rs.next();
                        } catch (SQLException ex) {
                            throw new UncheckedSqlException(ex);
                        }
                    }

                    @Override
                    public NSqlRow next() {
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
            throw new UncheckedSqlException(e);
        } finally {
            if (close != null) {
                close.run();
            }
        }
    }

    public NSqlRow columnsRow(){
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


}
