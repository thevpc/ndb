package net.thevpc.dbrman.impl;

import net.thevpc.dbrman.api.DatabaseDriver;
import net.thevpc.dbrman.api.DatabaseItemQuery;
import net.thevpc.dbrman.api.DbItem;
import net.thevpc.dbrman.model.*;
import net.thevpc.dbrman.util.Utils;
import net.thevpc.vio2.api.IoCell;
import net.thevpc.vio2.api.IoRow;
import net.thevpc.vio2.impl.AbstractIoCell;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DefaultDatabaseItemQuery implements DatabaseItemQuery {
    private DatabaseDriver driver;
    private Predicate<String> contentTextCondition;
    private Predicate<String> structureTextCondition;

    public DefaultDatabaseItemQuery(DatabaseDriver driver) {
        this.driver = driver;
    }

    @Override
    public DatabaseItemQuery whereContentText(Predicate<String> test) {
        this.contentTextCondition = test;
        return this;
    }

    @Override
    public DatabaseItemQuery whereStructureText(Predicate<String> test) {
        this.structureTextCondition = test;
        return this;
    }

    @Override
    public DatabaseItemQuery whereText(Predicate<String> test) {
        whereContentText(test);
        whereStructureText(test);
        return this;
    }

    @Override
    public Stream<DbItem> run() {
        List<TableId> t = driver.getTableIds();
        List<Stream<DbItem>> streams = new ArrayList<>();
        if (structureTextCondition != null) {
            Predicate<String> structureTextConditionSafe = s -> {
                if (s == null) {
                    return false;
                }
                return structureTextCondition.test(s);
            };
            streams.add(
                    Utils.toStream(new Iterator<DbItem>() {
                        private Map<DbItem.Type, Set<String>> visited = new HashMap<>();
                        private List<DbItem> curr = new ArrayList<>();
                        private Iterator<TableId> it;

                        {
                            it = t.iterator();
                        }

                        private boolean visit(DbItem.Type t, String n) {
                            return visited.computeIfAbsent(t, d -> new HashSet<>()).add(n);
                        }

                        private void reg(String n, DbItem.Type t, Object o) {
                            if (structureTextConditionSafe.test(n)) {
                                curr.add(new DefaultDbItem(n, t, o));
                            }
                        }

                        @Override
                        public boolean hasNext() {
                            if (!curr.isEmpty()) {
                                return true;
                            }
                            while (it.hasNext()) {
                                TableId u = it.next();
                                CatalogId c = u.getCatalogId();
                                if (c != null && !visit(DbItem.Type.CATALOG, c.getFullName())) {
                                    reg(c.getCatalogName(), DbItem.Type.CATALOG, c);
                                }
                                SchemaId s = u.getSchemaId();
                                if (s != null && !visit(DbItem.Type.SCHEMA, s.getFullName())) {
                                    reg(s.getSchemaName(), DbItem.Type.SCHEMA, s);
                                }
                                reg(u.getTableName(), DbItem.Type.TABLE, u);
                                TableDefinition d = driver.getTableDefinition(u);
                                if (d != null) {
                                    if (!visit(DbItem.Type.TABLE_TYPE, d.getTableType())) {
                                        reg(d.getTableType(), DbItem.Type.TABLE_TYPE, d.getTableType());
                                    }
                                    for (ColumnDefinition column : d.getColumns()) {
                                        reg(column.getColumnName(), DbItem.Type.COLUMN, column);
                                        reg(column.getSqlTypeCode(), DbItem.Type.COLUMN_SQL_TYPE_CODE, column);
                                        reg(column.getStoreType().name(), DbItem.Type.COLUMN_STORE_TYPE, column);
                                    }
                                }
                            }
                            return false;
                        }

                        @Override
                        public DbItem next() {
                            return curr.remove(0);
                        }
                    })
            );
        }
        if (contentTextCondition != null) {
            Predicate<String> contentTextConditionSafe = s -> {
                if (s == null) {
                    return false;
                }
                return contentTextCondition.test(s);
            };
            for (TableId tableId : t) {
                TableDefinition tableDef = driver.getTableDefinition(tableId);
                Iterator<IoRow> tableRows = driver.getTableRows(tableId).rowsIterator();
                streams.add(
                        Utils.toStream(new Iterator<DbItem>() {
                                           private List<DbItem> curr = new ArrayList<>();

                                           private void reg(String n, DbItem.Type t, Object o, Object... parents) {
                                               if (contentTextConditionSafe.test(n)) {
                                                   curr.add(new DefaultDbItem(n, t, o,parents));
                                               }
                                           }

                                           @Override
                                           public boolean hasNext() {
                                               if (!curr.isEmpty()) {
                                                   return true;
                                               }
                                               while (tableRows.hasNext()) {
                                                   try (IoRow n = tableRows.next()) {
                                                       IoRow rr = n.repeatable();
                                                       for (IoCell column : rr.getColumns()) {
                                                           Object o = column.getObject();
                                                           if (o != null && !AbstractIoCell.isLobObject(o)) {
                                                               String nn = String.valueOf(o);
                                                               reg(nn, DbItem.Type.COLUMN_VALUE, column, column.getDefinition(),rr, tableDef);
                                                           }
                                                       }
                                                       if (!curr.isEmpty()) {
                                                           return true;
                                                       }
                                                   }
                                               }
                                               return false;
                                           }

                                           @Override
                                           public DbItem next() {
                                               return curr.remove(0);
                                           }
                                       }
                        ));
            }
        }
        return Utils.concatStreams(streams);
    }
}
