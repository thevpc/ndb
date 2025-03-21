package net.thevpc.nsql.mapper;

import net.thevpc.nuts.util.NOptional;

public interface ResultSetMapperFactory {
    <T> NOptional<NResultSetMapper<T>> createMapper(NResultSetMapperContext context);
}
