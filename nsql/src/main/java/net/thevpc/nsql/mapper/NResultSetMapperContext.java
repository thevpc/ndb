package net.thevpc.nsql.mapper;

import java.lang.reflect.Field;
import java.sql.ResultSetMetaData;

public interface NResultSetMapperContext {
    Field field();

    ResultSetMetaData metaData();

    Integer findColumnIndexByName(String columnName);
}
