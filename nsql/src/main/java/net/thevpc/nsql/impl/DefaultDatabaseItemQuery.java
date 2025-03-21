package net.thevpc.nsql.impl;

import net.thevpc.nsql.*;
import net.thevpc.nsql.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DefaultDatabaseItemQuery implements NDatabaseItemQuery {
    private NSqlConnection driver;
    private Predicate<NSqlColumnValue> contentCondition;
    private Predicate<String> structureTextCondition;

    public DefaultDatabaseItemQuery(NSqlConnection driver) {
        this.driver = driver;
    }

    @Override
    public NDatabaseItemQuery whereContentText(Predicate<String> test) {
        this.contentCondition = test == null ? null : c -> {
            Object value = NLobUtils.repeatable(c.getValue());
            if (!NLobUtils.isLobPointer(value)) {
                String s = value == null ? null : value.toString();
                if (s != null) {
                    return test.test(s);
                }
            }
            return false;
        };
        return this;
    }

    @Override
    public NDatabaseItemQuery whereStructureText(Predicate<String> test) {
        this.structureTextCondition = test;
        return this;
    }

    @Override
    public NDatabaseItemQuery whereText(Predicate<String> test) {
        whereContentText(test);
        whereStructureText(test);
        return this;
    }

    @Override
    public NDatabaseItemQuery whereContent(Predicate<NSqlColumnValue> test) {
        this.contentCondition = test;
        return this;
    }

    @Override
    public Stream<NSqlSearchItem> run() {
        List<NSqlTableId> t = driver.getTableIds();
        List<Stream<NSqlSearchItem>> streams = new ArrayList<>();
        if (structureTextCondition != null) {
            Predicate<String> structureTextConditionSafe = s -> {
                if (s == null) {
                    return false;
                }
                return structureTextCondition.test(s);
            };
            streams.add(
                    NSqlUtils.toStream(new Iterator<NSqlSearchItem>() {
                        private Map<NSqlSearchItem.Type, Set<String>> visited = new HashMap<>();
                        private List<NSqlSearchItem> curr = new ArrayList<>();
                        private Iterator<NSqlTableId> it;

                        {
                            it = t.iterator();
                        }

                        private boolean visit(NSqlSearchItem.Type t, String n) {
                            return visited.computeIfAbsent(t, d -> new HashSet<>()).add(n);
                        }

                        private boolean reg(String n, NSqlSearchItem.Type t, Object o) {
                            if (structureTextConditionSafe.test(n)) {
                                curr.add(new NSqlSearchItemImpl(n, t, o));
                                return true;
                            }
                            return false;
                        }

                        @Override
                        public boolean hasNext() {
                            if (!curr.isEmpty()) {
                                return true;
                            }
                            while (it.hasNext()) {
                                NSqlTableId u = it.next();
                                NSqlCatalogId c = u.getCatalogId();
                                if (c != null && !visit(NSqlSearchItem.Type.CATALOG, c.getFullName())) {
                                    reg(c.getCatalogName(), NSqlSearchItem.Type.CATALOG, c);
                                }
                                NSqlSchemaId s = u.getSchemaId();
                                if (s != null && !visit(NSqlSearchItem.Type.SCHEMA, s.getFullName())) {
                                    reg(s.getSchemaName(), NSqlSearchItem.Type.SCHEMA, s);
                                }
                                reg(u.getTableName(), NSqlSearchItem.Type.TABLE, u);
                                NSqlTableDefinition d = driver.getTableDefinition(u);
                                if (d != null) {
                                    if (!visit(NSqlSearchItem.Type.TABLE_TYPE, d.getTableType())) {
                                        reg(d.getTableType(), NSqlSearchItem.Type.TABLE_TYPE, d.getTableType());
                                    }
                                    for (NSqlColumn column : d.getColumns()) {
                                        reg(column.getColumnName(), NSqlSearchItem.Type.COLUMN_NAME, column);
                                        if(!reg(column.getSqlTypeCode(), NSqlSearchItem.Type.COLUMN_TYPE, column)){
                                            reg(column.getColumnType().name(), NSqlSearchItem.Type.COLUMN_TYPE, column);
                                        }
                                    }
                                }
                            }
                            return false;
                        }

                        @Override
                        public NSqlSearchItem next() {
                            return curr.remove(0);
                        }
                    })
            );
        }
        if (contentCondition != null) {
            Predicate<NSqlColumnValue> contentConditionSafe = new Predicate<NSqlColumnValue>() {
                @Override
                public boolean test(NSqlColumnValue sqlColumnValue) {
                    if (contentCondition != null) {
                        return contentCondition.test(sqlColumnValue);
                    }
                    return true;
                }
            };
            for (NSqlTableId tableId : t) {
                NSqlTableDefinition tableDef = driver.getTableDefinition(tableId);
                ResultSet tableResultSet = driver.getTableResultSet(tableId);
                Iterator<NSqlSearchItem> vals = new Iterator<NSqlSearchItem>() {
                    List<NSqlSearchItem> curr=new ArrayList<>();
                    long rowIndex=1;
                    @Override
                    public boolean hasNext() {
                        if(!curr.isEmpty()){
                            return false;
                        }
                        try {
                            while (tableResultSet.next()) {
                                List<NSqlColumn> columns = tableDef.getColumns();
                                for (int i = 0; i < columns.size(); i++) {
                                    NSqlColumn column = columns.get(i);
                                    MySqlColumnValue t1 = new MySqlColumnValue(column, tableResultSet, i + 1);
                                    if (contentConditionSafe.test(t1)) {
                                        curr.add(new NSqlSearchItemImpl(t1.value, NSqlSearchItem.Type.COLUMN_VALUE, column, new NSqlTableRowIndex(rowIndex, tableDef)));
                                    }
                                }
                                rowIndex++;
                                if(!curr.isEmpty()){
                                    return true;
                                }
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        return false;
                    }

                    @Override
                    public NSqlSearchItem next() {
                        if(!curr.isEmpty()){
                            return curr.remove(0);
                        }
                        return null;
                    }
                };
                streams.add(NSqlUtils.toStream(vals));
            }
        }
        return NSqlUtils.concatStreams(streams);
    }

    private static class MySqlColumnValue implements NSqlColumnValue {
        private final NSqlColumn column;
        private final ResultSet rss;
        private final int index;
        private Object value;
        private boolean valueEvaluated;

        public MySqlColumnValue(NSqlColumn column, ResultSet rss, int index) {
            this.column = column;
            this.rss = rss;
            this.index = index;
        }

        @Override
        public NSqlColumn getColumn() {
            return column;
        }

        @Override
        public Object getValue() {
            if (valueEvaluated) {
                return value;
            }
            try {
                value = NLobUtils.repeatable(rss.getObject(index));
                valueEvaluated = true;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return value;
        }
    }
}
