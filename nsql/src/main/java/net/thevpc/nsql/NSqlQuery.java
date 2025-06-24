package net.thevpc.nsql;

public interface NSqlQuery {
    NSqlQuery append(String sql);

    NSqlQuery setParam(NSqlParam param);

    int executeUpdate();

    NQueryResult executeQuery();
}
