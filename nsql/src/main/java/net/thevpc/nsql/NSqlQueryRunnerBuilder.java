package net.thevpc.nsql;

public class NSqlQueryRunnerBuilder extends AbstractNSqlQueryBuilder<NSqlQueryRunnerBuilder>{
    private String query;
    private NSqlQueryRunner<Object> processor;
    private NSqlConnectionRunner connectionRunner;

    public NSqlQueryRunnerBuilder(NSqlConnectionRunner connectionRunner) {
        this.connectionRunner = connectionRunner;
    }

    public String getQuery() {
        return query;
    }

    public NSqlQueryRunnerBuilder setQuery(String query) {
        this.query = query;
        return this;
    }

    public NSqlQueryRunner<Object> getProcessor() {
        return processor;
    }

    public NSqlQueryRunnerBuilder setProcessor(NSqlQueryRunner<Object> processor) {
        this.processor = processor;
        return this;
    }

    public NSqlQueryRunnerBuilder run(NSqlQueryRunner<Object> processor) {
        setProcessor(processor);
        run();
        return this;
    }

    public void run(){
        connectionRunner.withQuery(getQuery(),getProcessor());
    }
}
