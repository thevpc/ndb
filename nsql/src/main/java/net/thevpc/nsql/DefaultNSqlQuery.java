package net.thevpc.nsql;

import net.thevpc.nsql.impl.ResultSetQueryResult;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NStreamTokenizer;

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

class DefaultNSqlQuery implements NSqlQuery {
    private static Logger LOG = Logger.getLogger(DefaultNSqlQuery.class.getName());
    private StringBuilder userSql = new StringBuilder();
    private List<NSqlParam> params = new ArrayList<>();
    private NSqlParam.Mode mode;
    private NSqlConnection connection;
    private NPrepareStatementContext prepareStatementContext;

    public DefaultNSqlQuery(NSqlConnection connection) {
        this.connection = connection;
    }

    @Override
    public NSqlQuery append(String sql) {
        if (sql != null) {
            userSql.append(sql);
        }
        return this;
    }

    @Override
    public NSqlQuery setParam(NSqlParam param) {
        if (param != null) {
            if (mode != null) {
                if (mode != param.getMode()) {
                    throw new NIllegalArgumentException(NMsg.ofC("unsupported mixed param modes"));
                }
            } else {
                this.mode = param.getMode();
            }
            params.add(param);
        }
        return this;
    }

    @Override
    public int executeUpdate() {
        LOG.log(Level.FINEST, "[SQL] " + userSql.toString());
        SqlInfo sqlInfo = toSqlInfo();
        try (PreparedStatement ps = connection.getConnection().prepareStatement(sqlInfo.sql)) {
            for (NSqlParam param : sqlInfo.indexedParams) {
                connection.prepareStatement(ps, param.columnIndex, param.columnType, param.columnName, param.value, prepareStatementContext);
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Override
    public NQueryResult executeQuery() {
        LOG.log(Level.FINEST, "[SQL] " + userSql.toString());
        SqlInfo sqlInfo = toSqlInfo();

        PreparedStatement ps = null;
        try {
            ps = connection.getConnection().prepareStatement(sqlInfo.sql);
            for (NSqlParam param : sqlInfo.indexedParams) {
                connection.prepareStatement(ps, param.columnIndex, param.columnType, param.columnName, param.value, prepareStatementContext);
            }
            Statement finalS = ps;
            return new ResultSetQueryResult(ps.executeQuery(), () -> {
                try {
                    finalS.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (SQLException ex) {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    //
                }
            }
            throw new UncheckedSqlException(ex);
        }
    }

    private SqlInfo toSqlInfo() {
        switch (mode == null ? NSqlParam.Mode.INDEX : mode) {
            case INDEX: {
                SqlInfo s = new SqlInfo();
                s.sql = userSql.toString();
                s.indexedParams.addAll(params);
                return s;
            }
            case NAME: {
                NStreamTokenizer st = new NStreamTokenizer(new StringReader(userSql.toString()));
                st.wordChar(':');
                st.wordChar('_');
                List<NSqlParam> params2 = new ArrayList<>();
                StringBuilder sql2 = new StringBuilder();
                while (st.nextToken() != StreamTokenizer.TT_EOF) {
                    switch (st.ttype) {
                        case StreamTokenizer.TT_WORD: {
                            if (st.image.startsWith(":")) {
                                String u = st.image.substring(1);
                                if (!u.startsWith(":")) {
                                    Optional<NSqlParam> p = params.stream().filter(x -> x.columnName.equals(u)).findFirst();
                                    if (p.isPresent()) {
                                        NSqlParam pp = p.get();
                                        params2.add(NSqlParam.of(params2.size()+1, pp.columnType, pp.value));
                                    } else {
                                        throw new NIllegalArgumentException(NMsg.ofC("invalid param name %s", u));
                                    }
                                    sql2.append("?");
                                } else {
                                    sql2.append(st.image);
                                }
                            } else {
                                sql2.append(st.image);
                            }
                            break;
                        }
                        default: {
                            sql2.append(st.image);
                        }
                    }
                }
                SqlInfo s = new SqlInfo();
                s.sql = sql2.toString();
                s.indexedParams.addAll(params2);
                return s;
            }
        }
        throw new IllegalStateException("unexpected sql mode");
    }

    private static class SqlInfo {
        String sql;
        List<NSqlParam> indexedParams = new ArrayList<>();
    }


}
