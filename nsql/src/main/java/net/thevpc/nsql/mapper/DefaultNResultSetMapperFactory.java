package net.thevpc.nsql.mapper;

import net.thevpc.nsql.NSqlColumnReader;
import net.thevpc.nsql.NSqlColumnReaders;
import net.thevpc.nsql.NColumn;
import net.thevpc.nsql.NEmbeddable;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class DefaultNResultSetMapperFactory implements ResultSetMapperFactory {
    public static final DefaultNResultSetMapperFactory INSTANCE = new DefaultNResultSetMapperFactory();

    public <T>NOptional<NResultSetMapper<T>> createDefaultFieldReader(NResultSetMapperContext context) {
        Field field = context.field();
        switch (field.getType().getName()) {
            case "bool": {
                return findSimple(field, () -> (NSqlColumnReader<T>) NSqlColumnReaders.PRIMITIVE_BOOLEAN, context);
            }
            case "java.lang.Boolean": {
                return findSimple(field, () -> (NSqlColumnReader<T>) NSqlColumnReaders.BOOLEAN, context);
            }
            case "int": {
                return findSimple(field, () -> (NSqlColumnReader<T>) NSqlColumnReaders.PRIMITIVE_INT, context);
            }
            case "java.lang.Integer": {
                return findSimple(field, () -> (NSqlColumnReader<T>) NSqlColumnReaders.INT, context);
            }
            case "long": {
                return findSimple(field, () -> (NSqlColumnReader<T>) NSqlColumnReaders.PRIMITIVE_LONG, context);
            }
            case "java.lang.Long": {
                return findSimple(field, () -> (NSqlColumnReader<T>) NSqlColumnReaders.LONG, context);
            }
            case "java.lang.String": {
                return findSimple(field, () -> (NSqlColumnReader<T>) NSqlColumnReaders.STRING, context);
            }
        }
        return NOptional.ofNamedEmpty("reader for " + field.getType().getName());
    }

    public <T> NOptional<NResultSetMapper<T>> createCustomFieldReader(NResultSetMapperContext context) {
        return null;
    }

    public <T> NOptional<NResultSetMapper<T>> createMapper(NResultSetMapperContext context) {
        Field field = context.field();
        NOptional<NResultSetMapper<T>> u = createCustomFieldReader(context);
        if(u!=null){
            if(u.isPresent()){
                return u;
            }
        }
        if (field.getAnnotation(NEmbeddable.class) != null) {
            return NOptional.of(new ReflectResultSetMapper<T>((Class<T>) field.getType(), context.metaData(), this));
        }
        return createDefaultFieldReader(context);
    }

    private <T> NOptional<NResultSetMapper<T>> findSimple(Field field, Supplier<NSqlColumnReader<T>> r, NResultSetMapperContext context) {
        String name = findColumnName(field);
        Integer columnIndexByName = context.findColumnIndexByName(name);
        if (columnIndexByName != null) {
            NSqlColumnReader<T> rr = r.get();
            return NOptional.of(rs -> rr.read(rs, columnIndexByName));
        }
        return NOptional.ofNamedEmpty("reader for " + field.getType().getName());
    }

    private String findColumnName(Field field) {
        NColumn c = field.getAnnotation(NColumn.class);
        if (c != null) {
            String n = NStringUtils.trimToNull(c.name());
            if (n != null) {
                return n;
            }
        }
        return field.getName();
    }
}
