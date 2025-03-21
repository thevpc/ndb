package net.thevpc.nsql.mapper;

import net.thevpc.nsql.util.ReflectionHelper;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.function.Supplier;

public class ReflectResultSetMapper<T> implements NResultSetMapper<T> {
    private Class<T> type;
    private ResultSetMetaData md;
    private FieldImporter[] importers;
    private Supplier<T> constructor;
    private ResultSetMapperFactory fieldReaderFactory;

    private interface FieldImporter {
        void importField(ResultSet rs, Object instance);
    }

    public ReflectResultSetMapper(Class<T> type, ResultSetMetaData md, ResultSetMapperFactory fieldReaderFactory) {
        this.type = type;
        this.md = md;
        this.fieldReaderFactory = fieldReaderFactory == null ? DefaultNResultSetMapperFactory.INSTANCE : fieldReaderFactory;
        Constructor<T> c;
        try {
            c = this.type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        c.setAccessible(true);
        constructor = () -> {
            try {
                return c.newInstance();
            } catch (Exception e) {
                throw ReflectionHelper.asRuntimeException(e);
            }
        };
        Class cc = type;
        LinkedHashMap<String, Integer> columnToIndex = new LinkedHashMap<>();
        int cCount = 0;
        try {
            cCount = md.getColumnCount();
            for (int i = 1; i <= cCount; i++) {
                String cn = NNameFormat.CONST_NAME.format(md.getColumnName(i + 1));
                if (!columnToIndex.containsKey(cn)) {
                    columnToIndex.put(cn, i);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        LinkedHashMap<String, FieldImporter> fieldImporters = new LinkedHashMap<>();
        while (cc != null) {
            for (Field f : cc.getDeclaredFields()) {
                String n = f.getName();
                int m = f.getModifiers();
                if (!fieldImporters.containsKey(n)
                        && !Modifier.isFinal(m)
                        && !Modifier.isStatic(m)
                ) {
                    String cn = NNameFormat.CONST_NAME.format(n);
                    Integer i = columnToIndex.get(cn);
                    if (i != null) {
                        f.setAccessible(true);
                        NOptional<NResultSetMapper<T>> v = this.fieldReaderFactory.createMapper(new NResultSetMapperContext() {
                            @Override
                            public Field field() {
                                return f;
                            }

                            @Override
                            public ResultSetMetaData metaData() {
                                return md;
                            }

                            @Override
                            public Integer findColumnIndexByName(String columnName) {
                                return columnToIndex.get(NNameFormat.CONST_NAME.format(columnName));
                            }
                        });
                        fieldImporters.put(n, new FieldImporterImpl(f, v.get()));
                    }
                }
            }
            cc = cc.getSuperclass();
        }
        this.importers = fieldImporters.values().toArray(new FieldImporter[0]);
    }

    @Override
    public T get(ResultSet rs) {
        T instance = constructor.get();
        for (FieldImporter importer : importers) {
            importer.importField(rs, instance);
        }
        return instance;
    }

    private static class FieldImporterImpl implements FieldImporter {
        private final Field f;
        private final NResultSetMapper fieldReader;

        public FieldImporterImpl(Field f, NResultSetMapper fieldReader) {
            this.f = f;
            this.fieldReader = fieldReader;
        }

        @Override
        public void importField(ResultSet rs, Object instance) {
            try {
                f.set(instance, fieldReader.get(rs));
            } catch (Exception e) {
                throw ReflectionHelper.asRuntimeException(e);
            }
        }
    }
}
