package net.thevpc.nsql;

import net.thevpc.nsql.mapper.NResultSetMapper;
import net.thevpc.nuts.util.NOptional;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NSqlQueryCallerBuilder<V> extends AbstractNSqlQueryBuilder<NSqlQueryCallerBuilder<V>> {
    private String query;
    private Object userObject;
    private NSqlQueryCaller<V> processor;
    private NSqlConnectionRunner connectionRunner;

    public NSqlQueryCallerBuilder(NSqlConnectionRunner connectionRunner) {
        this.connectionRunner = connectionRunner;
    }

    public String getQuery() {
        return query;
    }

    public NSqlQueryCallerBuilder<V> setQuery(String query) {
        this.query = query;
        return this;
    }

    public <T> T getUserObject() {
        return (T) userObject;
    }

    public NSqlQueryCallerBuilder<V> setUserObject(Object userObject) {
        this.userObject = userObject;
        return this;
    }

    public NSqlQueryCaller<V> getProcessor() {
        return processor;
    }

    public NSqlQueryCallerBuilder<V> setProcessor(NSqlQueryCaller<V> processor) {
        this.processor = processor;
        return this;
    }

    public NOptional<V> run(NSqlQueryCaller<V> processor) {
        setProcessor(processor);
        return run();
    }

    public NOptional<V> run() {
        return connectionRunner.withQueryCall(getUserObject(), getQuery(),
                this::doPrepare,
                getProcessor());
    }

    public NOptional<V> getSingleResult(NResultSetMapper<V> m) {
        return NOptional.ofFirst(getResultList(m));
    }

    public List<V> getResultList(NResultSetMapper<V> m) {
        List<V> found = new ArrayList<>();
        run(new NSqlQueryCaller<V>() {
            @Override
            public void eachRow(NSqlQueryCallerContext<V> context) throws SQLException {
                found.add(m.get(context.rs()));
            }
        });
        return found;
    }
}
