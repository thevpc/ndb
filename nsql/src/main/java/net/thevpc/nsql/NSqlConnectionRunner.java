package net.thevpc.nsql;

import net.thevpc.nuts.format.NVisitResult;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NRef;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NSqlConnectionRunner {
    private Supplier<NSqlConnectionString> connectionStringSupplier;
    private ThreadLocal<NSqlConnection> curr = new ThreadLocal<>();

    public NSqlConnectionRunner(Supplier<NSqlConnectionString> connectionStringSupplier) {
        this.connectionStringSupplier = connectionStringSupplier;
    }

    public NSqlQueryRunnerBuilder withQuery(String query) {
        return new NSqlQueryRunnerBuilder(this).setQuery(query);
    }

    public <V> NSqlQueryCallerBuilder<V> withQueryCall(String query) {
        return new NSqlQueryCallerBuilder<V>(this).setQuery(query);
    }

    public <V> NSqlUpdateCallerBuilder<V> withUpdateCall(String query) {
        return new NSqlUpdateCallerBuilder<V>(this).setQuery(query);
    }

    public void withQuery(String query, NSqlQueryRunner<Object> runnable) {
        withConnection((c) -> {
            try (Statement statement = c.getConnection().createStatement()) {
                try (ResultSet rs = statement.executeQuery(query)) {
                    NSqlQueryRunnerContext<Object> context = new NSqlQueryRunnerContext<Object>(c, rs);
                    while (rs.next()) {
                        runnable.eachRow(context);
                        if (context.getVisitResult() == NVisitResult.TERMINATE) {
                            break;
                        }
                    }
                }
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        });
    }

    public <V> NOptional<V> withQueryCall(String query, Consumer<PreparedStatement> preparer, NSqlQueryCaller<V> runnable) {
        return withQueryCall(null, query, preparer, runnable);
    }

    public <V> NOptional<V> withQueryCall(Object principal, String query, Consumer<PreparedStatement> preparer, NSqlQueryCaller<V> runnable) {
        NRef<V> result = NRef.ofNull();
        withConnection((c) -> {
            try (PreparedStatement statement = c.getConnection().prepareStatement(query)) {
                if (preparer != null) {
                    preparer.accept(statement);
                }
                try (ResultSet rs = statement.executeQuery()) {
                    NSqlQueryCallerContext<V> context = new NSqlQueryCallerContext<V>(principal, c, rs);
                    while (rs.next()) {
                        if (runnable != null) {
                            runnable.eachRow(context);
                        }
                        if (context.getVisitResult() == NVisitResult.TERMINATE) {
                            result.set(context.getResultValue());
                        }
                    }
                }
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        });
        return NOptional.of(result.get());
    }


    public <V> NOptional<V> withUpdateCall(String query, Consumer<PreparedStatement> preparer, boolean returnGenerated, NSqlUpdateCaller<Object, V> runnable) {
        return withUpdateCall(null, query, preparer, returnGenerated, runnable);
    }

    public <V> NOptional<V> withUpdateCall(Object principal, String query, Consumer<PreparedStatement> preparer, boolean returnGenerated, NSqlUpdateCaller<Object, V> runnable) {
        NRef<V> result = NRef.ofNull();
        withConnection((c) -> {
            try (PreparedStatement statement =
                         returnGenerated ? c.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS) : c.getConnection().prepareStatement(query)) {
                if (preparer != null) {
                    preparer.accept(statement);
                }
                int i = statement.executeUpdate();
                NSqlUpdateCallerContext<Object, V> context = new NSqlUpdateCallerContext<>(principal, c, i, statement);
                if (runnable != null) {
                    runnable.run(context);
                }
                result.set(context.getResultValue());
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        });
        return NOptional.of(result.get());
    }

    public void withConnection(Consumer<NSqlConnection> runnable) {
        NSqlConnection c = curr.get();
        if (c == null) {
            c = new NSimpleSqlConnectionFactory(connectionStringSupplier.get()).create();
            NSqlConnection o = curr.get();
            curr.set(c);
            runnable.accept(c);
            curr.set(o);
            if (o == null) {
                c.close();
            }
        } else {
            runnable.accept(c);
        }
    }

    public <T> T callWithConnection(Function<NSqlConnection, T> runnable) {
        NSqlConnection c = curr.get();
        if (c == null) {
            c = new NSimpleSqlConnectionFactory(connectionStringSupplier.get()).create();
            NSqlConnection o = curr.get();
            curr.set(c);
            T r = runnable.apply(c);
            curr.set(o);
            if (o == null) {
                c.close();
            }
            return r;
        } else {
            return runnable.apply(c);
        }
    }
}
