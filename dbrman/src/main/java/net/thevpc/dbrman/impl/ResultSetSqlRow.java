package net.thevpc.dbrman.impl;

import com.google.gson.Gson;
import net.thevpc.dbrman.api.SqlRow;
import net.thevpc.dbrman.api.SqlRowConversionContext;
import net.thevpc.dbrman.util.UncheckedSQLException;
import net.thevpc.vio2.impl.AbstractIoCell;
import net.thevpc.vio2.util.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

class ResultSetSqlRow extends AbstractSqlRow {
    private ResultSet rs;
    private ResultSetSqlRowContext rowContext;

    public ResultSetSqlRow(ResultSet rs, ResultSetSqlRowContext rowContext) {
        this.rs = rs;
        this.rowContext = rowContext;
    }

    @Override
    public Long asLong() {
        return getLong(1);
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> lm = new LinkedHashMap<>();
        DD[] cols = rowContext.dd();
        for (DD dd : cols) {
            try {
                lm.put(dd.name, rs.getObject(dd.index));
            } catch (SQLException e) {
                throw new UncheckedSQLException(e);
            }
        }
        return lm;
    }

    @Override
    public Long getLong(int index) {
        try {
            long l = rs.getLong(index);
            if (l == 0) {
                if (rs.wasNull()) {
                    return null;
                }
            }
            return l;
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    @Override
    public String getString(int index) {
        try {
            return rs.getString(index);
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    @Override
    public Object getObject(int index) {
        try {
            return rs.getObject(index);
        } catch (SQLException e) {
            throw new UncheckedSQLException(e);
        }
    }

    @Override
    public int getColumnsCount() {
        return rowContext.getColumnsCount();
    }

    private static class DD {
        int index;
        String name;

        public DD(int index, String name) {
            this.index = index;
            this.name = name;
        }
    }

    public static class ResultSetSqlRowContext {
        private ResultSet rs;
        ResultSetMetaData md;
        int columnCount;
        DD[] colsArray;

        public ResultSetSqlRowContext(ResultSet rs) {
            this.rs = rs;
        }

        public int getColumnsCount() {
            try {
                return getMetaData().getColumnCount();
            } catch (SQLException e) {
                throw new UncheckedSQLException(e);
            }
        }

        public ResultSetMetaData getMetaData() {
            if (md == null) {
                try {
                    md = rs.getMetaData();
                } catch (SQLException e) {
                    throw new UncheckedSQLException(e);
                }
            }
            return md;
        }

        public DD[] dd() {
            if (colsArray == null) {
                Map<String, Integer> cols = new LinkedHashMap<>();
                try {
                    ResultSetMetaData md = getMetaData();
                    columnCount = md.getColumnCount();
                    for (int i = 1; i < columnCount + 1; i++) {
                        String s = md.getColumnName(i);
                        if (!cols.containsKey(s)) {
                            cols.put(s, i);
                        }
                    }
                } catch (SQLException ex) {
                    throw new UncheckedSQLException(ex);
                }

                List<DD> aa = new ArrayList<>();
                for (Map.Entry<String, Integer> e : cols.entrySet()) {
                    aa.add(new DD(e.getValue(), e.getKey()));
                }
                colsArray = aa.toArray(new DD[0]);
            }
            return colsArray;
        }
    }
}
