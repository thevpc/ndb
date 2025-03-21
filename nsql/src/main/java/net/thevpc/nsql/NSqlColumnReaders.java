package net.thevpc.nsql;

import net.thevpc.nuts.util.NClassMap;
import net.thevpc.nuts.util.NOptional;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NSqlColumnReaders {
    public static final NSqlColumnReaders INSTANCE = new NSqlColumnReaders();

    private NClassMap<NSqlColumnReader> map = new NClassMap<>(NSqlColumnReader.class);

    public static NSqlColumnReader<Boolean> PRIMITIVE_BOOLEAN = new NSqlColumnReader<Boolean>() {
        @Override
        public Boolean read(ResultSet resultSet, int columnIndex) {
            try {
                return resultSet.getBoolean(columnIndex);
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        }

        @Override
        public Boolean read(ResultSet resultSet, String columnName) {
            try {
                return resultSet.getBoolean(columnName);
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        }
    };
    public static NSqlColumnReader<Boolean> BOOLEAN = new NSqlColumnReader<Boolean>() {
        @Override
        public Boolean read(ResultSet resultSet, int columnIndex) {
            try {
                boolean u = resultSet.getBoolean(columnIndex);
                if (resultSet.wasNull()) {
                    return null;
                }
                return u;
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        }

        @Override
        public Boolean read(ResultSet resultSet, String columnName) {
            try {
                boolean u = resultSet.getBoolean(columnName);
                if (resultSet.wasNull()) {
                    return null;
                }
                return u;
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        }
    };

    public static NSqlColumnReader<Integer> PRIMITIVE_INT = new NSqlColumnReader<Integer>() {
        @Override
        public Integer read(ResultSet resultSet, int columnIndex) {
            try {
                return resultSet.getInt(columnIndex);
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        }

        @Override
        public Integer read(ResultSet resultSet, String columnName) {
            try {
                return resultSet.getInt(columnName);
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        }
    };
    public static NSqlColumnReader<Integer> INT = new NSqlColumnReader<Integer>() {
        @Override
        public Integer read(ResultSet resultSet, int columnIndex) {
            try {
                int u = resultSet.getInt(columnIndex);
                if (resultSet.wasNull()) {
                    return null;
                }
                return u;
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        }

        @Override
        public Integer read(ResultSet resultSet, String columnName) {
            try {
                int u = resultSet.getInt(columnName);
                if (resultSet.wasNull()) {
                    return null;
                }
                return u;
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        }
    };

    public static NSqlColumnReader<Long> PRIMITIVE_LONG = new NSqlColumnReader<Long>() {
        @Override
        public Long read(ResultSet resultSet, int columnIndex) {
            try {
                return resultSet.getLong(columnIndex);
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        }

        @Override
        public Long read(ResultSet resultSet, String columnName) {
            try {
                return resultSet.getLong(columnName);
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        }
    };
    public static NSqlColumnReader<Long> LONG = new NSqlColumnReader<Long>() {
        @Override
        public Long read(ResultSet resultSet, int columnIndex) {
            try {
                long u = resultSet.getLong(columnIndex);
                if (resultSet.wasNull()) {
                    return null;
                }
                return u;
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        }

        @Override
        public Long read(ResultSet resultSet, String columnName) {
            try {
                long u = resultSet.getLong(columnName);
                if (resultSet.wasNull()) {
                    return null;
                }
                return u;
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        }
    };

    public static NSqlColumnReader<Double> PRIMITIVE_DOUBLE = new NSqlColumnReader<Double>() {
        @Override
        public Double read(ResultSet resultSet, int columnIndex) {
            try {
                return resultSet.getDouble(columnIndex);
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        }

        @Override
        public Double read(ResultSet resultSet, String columnName) {
            try {
                return resultSet.getDouble(columnName);
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        }
    };
    public static NSqlColumnReader<Double> DOUBLE = new NSqlColumnReader<Double>() {
        @Override
        public Double read(ResultSet resultSet, int columnIndex) {
            try {
                Double u = resultSet.getDouble(columnIndex);
                if (resultSet.wasNull()) {
                    return null;
                }
                return u;
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        }

        @Override
        public Double read(ResultSet resultSet, String columnName) {
            try {
                Double u = resultSet.getDouble(columnName);
                if (resultSet.wasNull()) {
                    return null;
                }
                return u;
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        }
    };

    public static NSqlColumnReader<String> STRING = new NSqlColumnReader<String>() {
        @Override
        public String read(ResultSet resultSet, int columnIndex) {
            try {
                return resultSet.getString(columnIndex);
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        }

        @Override
        public String read(ResultSet resultSet, String columnName) {
            try {
                return resultSet.getString(columnName);
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        }
    };

    public NSqlColumnReaders() {
    }


    public <T> NSqlColumnReaders register(Class<T> clazz, NSqlColumnReader<T> r) {
        if (r == null) {
            map.put(clazz, r);
        } else {
            map.remove(clazz);
        }
        return this;
    }

    public <T> NOptional<NSqlColumnReader<T>> of(Class<T> clazz) {
        NSqlColumnReader u = map.get(clazz);
        if (u == null) {
            return ofDefault(clazz);
        }
        return NOptional.of(u);
    }

    public static <T> NOptional<NSqlColumnReader<T>> ofDefault(Class<T> clazz) {
        switch (clazz.getName()) {
            case "boolean":
                return NOptional.of((NSqlColumnReader<T>) PRIMITIVE_BOOLEAN);
            case "java.lang.Boolean":
                return NOptional.of((NSqlColumnReader<T>) BOOLEAN);
            case "int":
                return NOptional.of((NSqlColumnReader<T>) PRIMITIVE_INT);
            case "java.lang.Integer":
                return NOptional.of((NSqlColumnReader<T>) INT);
            case "long":
                return NOptional.of((NSqlColumnReader<T>) PRIMITIVE_LONG);
            case "java.lang.Long":
                return NOptional.of((NSqlColumnReader<T>) LONG);
            case "double":
                return NOptional.of((NSqlColumnReader<T>) PRIMITIVE_DOUBLE);
            case "java.lang.Double":
                return NOptional.of((NSqlColumnReader<T>) DOUBLE);
            case "java.lang.String":
                return NOptional.of((NSqlColumnReader<T>) STRING);
        }
        return NOptional.ofNamedEmpty("reader for " + clazz.getSimpleName());
    }

}
