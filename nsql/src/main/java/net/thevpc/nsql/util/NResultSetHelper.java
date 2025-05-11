/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nsql.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import net.thevpc.nsql.UncheckedSqlException;

/**
 *
 * @author vpc
 */
public class NResultSetHelper {

    public static boolean getBoolean0(ResultSet resultSet, int columnIndex) {
        try {
            return resultSet.getBoolean(columnIndex);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static Boolean getBoolean(ResultSet resultSet, int columnIndex) {
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

    public static Boolean getBoolean(ResultSet resultSet, String columnName) {
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

    public static int getInt0(ResultSet resultSet, int columnIndex) {
        try {
            return resultSet.getInt(columnIndex);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static Integer getInt(ResultSet resultSet, int columnIndex) {
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

    public static Integer getInt(ResultSet resultSet, String columnName) {
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

    public static long getLong0(ResultSet resultSet, int columnIndex) {
        try {
            return resultSet.getLong(columnIndex);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static Long getLong(ResultSet resultSet, int columnIndex) {
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

    public static Long getLong(ResultSet resultSet, String columnName) {
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

    public static double getDouble0(ResultSet resultSet, int columnIndex) {
        try {
            return resultSet.getDouble(columnIndex);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static Double getDouble(ResultSet resultSet, int columnIndex) {
        try {
            double u = resultSet.getDouble(columnIndex);
            if (resultSet.wasNull()) {
                return null;
            }
            return u;
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static Double getDouble(ResultSet resultSet, String columnName) {
        try {
            double u = resultSet.getDouble(columnName);
            if (resultSet.wasNull()) {
                return null;
            }
            return u;
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static Timestamp getTimestamp(ResultSet resultSet, int columnIndex) {
        try {
            return resultSet.getTimestamp(columnIndex);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static Timestamp columnName(ResultSet resultSet, String columnName) {
        try {
            return resultSet.getTimestamp(columnName);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static Instant getInstant(ResultSet resultSet, int columnIndex) {
        try {
            Timestamp u = resultSet.getTimestamp(columnIndex);
            return u == null ? null : u.toInstant();
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static Instant getInstant(ResultSet resultSet, String columnName) {
        try {
            Timestamp u = resultSet.getTimestamp(columnName);
            return u == null ? null : u.toInstant();
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static java.util.Date getUtilDate(ResultSet resultSet, int columnIndex) {
        try {
            Timestamp u = resultSet.getTimestamp(columnIndex);
            return u == null ? null : new java.util.Date(u.getTime());
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static java.util.Date getUtilDate(ResultSet resultSet, String columnName) {
        try {
            Timestamp u = resultSet.getTimestamp(columnName);
            return u == null ? null : new java.util.Date(u.getTime());
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static LocalDate getLocalDate(ResultSet resultSet, int columnIndex) {
        try {
            java.sql.Date u = resultSet.getDate(columnIndex);
            return u == null ? null : u.toLocalDate();
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static LocalDate getLocalDate(ResultSet resultSet, String columnName) {
        try {
            java.sql.Date u = resultSet.getDate(columnName);
            return u == null ? null : u.toLocalDate();
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static LocalTime getLocalTime(ResultSet resultSet, int columnIndex) {
        try {
            java.sql.Time u = resultSet.getTime(columnIndex);
            return u == null ? null : u.toLocalTime();
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static LocalTime getLocalTime(ResultSet resultSet, String columnName) {
        try {
            java.sql.Time u = resultSet.getTime(columnName);
            return u == null ? null : u.toLocalTime();
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static java.sql.Time getSqlTime(ResultSet resultSet, int columnIndex) {
        try {
            return resultSet.getTime(columnIndex);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static java.sql.Time getSqlTime(ResultSet resultSet, String columnName) {
        try {
            return resultSet.getTime(columnName);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static java.sql.Date getSqlDate(ResultSet resultSet, int columnIndex) {
        try {
            return resultSet.getDate(columnIndex);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static java.sql.Date getSqlDate(ResultSet resultSet, String columnName) {
        try {
            return resultSet.getDate(columnName);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static java.sql.Timestamp getSqlTimestamp(ResultSet resultSet, int columnIndex) {
        try {
            return resultSet.getTimestamp(columnIndex);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static java.sql.Timestamp getSqlTimestamp(ResultSet resultSet, String columnName) {
        try {
            return resultSet.getTimestamp(columnName);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static String getString(ResultSet resultSet, int columnIndex) {
        try {
            return resultSet.getString(columnIndex);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    public static String getString(ResultSet resultSet, String columnName) {
        try {
            return resultSet.getString(columnName);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }
}
