package net.thevpc.nsql;

import net.thevpc.nuts.util.NOptional;

public class NSqlUpdateCallerBuilder<V> extends AbstractNSqlQueryBuilder<NSqlUpdateCallerBuilder<V>>{
    private String query;
    private Object userObject;
    private NSqlUpdateCaller<Object,V> processor;
    private NSqlConnectionRunner connectionRunner;
    private boolean returnGenerated;

    public NSqlUpdateCallerBuilder(NSqlConnectionRunner connectionRunner) {
        this.connectionRunner = connectionRunner;
    }

    public boolean isReturnGenerated() {
        return returnGenerated;
    }

    public NSqlUpdateCallerBuilder<V> returnGenerated() {
        return setReturnGenerated(true);
    }
    public NSqlUpdateCallerBuilder<V> setReturnGenerated(boolean returnGenerated) {
        this.returnGenerated = returnGenerated;
        return this;
    }

    public String getQuery() {
        return query;
    }

    public NSqlUpdateCallerBuilder<V> setQuery(String query) {
        this.query = query;
        return this;
    }

    public <T> T getUserObject() {
        return (T) userObject;
    }

    public NSqlUpdateCallerBuilder<V> setUserObject(Object userObject) {
        this.userObject = userObject;
        return this;
    }

    public NSqlUpdateCaller<Object,V> getProcessor() {
        return processor;
    }

    public NSqlUpdateCallerBuilder<V> setProcessor(NSqlUpdateCaller<Object,V> processor) {
        this.processor = processor;
        return this;
    }

    public NOptional<V> run(NSqlUpdateCaller<Object,V> processor) {
        setProcessor(processor);
        return run();
    }

    public NOptional<V> run(){
        return connectionRunner.withUpdateCall(
                getUserObject(),
                getQuery(),
                this::doPrepare,
                returnGenerated,
                getProcessor());
    }

}
