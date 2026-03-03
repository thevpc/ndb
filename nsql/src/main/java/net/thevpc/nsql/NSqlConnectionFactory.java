package net.thevpc.nsql;

import net.thevpc.nuts.elem.*;

import java.util.function.Function;

public interface NSqlConnectionFactory {
    static NSqlConnectionFactory of(NSqlConnectionString params) {
        return new NSimpleSqlConnectionFactory(params);
    }

    static NSqlConnectionFactory of(NSqlConnectionStringBuilder params) {
        return new NSimpleSqlConnectionFactory(params.build().get());
    }

    static NSqlConnectionFactory of(String connectionString) {
        return of(NSqlConnectionString.of(connectionString));
    }

    static NSqlConnectionFactory ofStringFunction(Function<String, String> connectionString) {
        return of(NSqlConnectionString.ofString(connectionString));
    }

    static NSqlConnectionFactory ofTsonFunction(Function<String, NElement> connectionString) {
        return new NSimpleSqlConnectionFactory(NSqlConnectionString.ofTson(connectionString));
    }

    NSqlDialect dialect();

    NSqlConnection create();
}
