package net.thevpc.nsql;

import net.thevpc.nsql.model.YesNo;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NStringUtils;
import net.thevpc.nuts.elem.*;

import java.sql.PreparedStatement;
import java.util.*;
import java.util.function.Function;

public class NSqlTable {
    private String tableName;
    private NSqlColumn[] columns;
    private NDdlAuto ddlAuto = NDdlAuto.UPDATE;

    public static NSqlTable ofRoot(String defaultTableName, NElement element, String[] defaultFields, Function<String, NSqlColumn> defs) {
        Map<String,NElement> vals=new LinkedHashMap<>();
        String name = NSqlTsonUtils.stringOfField("name", element, true);
        String expectedTableName = NStringUtils.firstNonBlank(name, defaultTableName);
        NElement columns = NSqlTsonUtils.fieldByNameOf("columns", element);
        vals.put("name", NElements.ofName(name));
        vals.put("columns", columns);
        return of(expectedTableName, vals, defaultFields, defs);
    }

    public static NSqlTable of(String tableName, Map<String, NElement> props, String[] defaultFields, Function<String, NSqlColumn> defs) {
        tableName = NStringUtils.firstNonBlank(NSqlTsonUtils.stringOf(props.get("name")), tableName);
        NElement columns = props.get("columns");
        NAssert.requireNonBlank(tableName, "tableName");
        NDdlAuto mode= NDdlAuto.parse(NSqlTsonUtils.stringOf(props.get("ddl-auto")));
        NListContainerElement expectedColumns = NSqlTsonUtils.containerOf(columns);
        List<NSqlColumn> columnsList = new ArrayList<>();
        boolean hasId = false;
        int insertIndex = 1;
        Set<String> visited = new HashSet<>();
        for (int i = 0; i < defaultFields.length; i++) {
            String field = defaultFields[i];
            NElement columnDefinition = NSqlTsonUtils.fieldByNameOf(field, expectedColumns);
            NSqlColumn dc = defs == null ? null : defs.apply(field);
            if (dc == null) {
                dc = new NSqlColumn();
                dc.setFieldName(field);
            }
            NSqlColumn e = NSqlColumn.of(columnDefinition, dc);
            visited.add(e.getFieldName());
            if (e.getEnabled()) {
                e.setIndex(columnsList.size() + 1);
                columnsList.add(e);
                if (e.getId() != null && e.getId()) {
                    hasId = true;
                }
                if (e.getAutoIncrement()!=YesNo.YES) {
                    e.setInsertIndex(insertIndex++);
                }
            }
        }
        if (expectedColumns.children() != null) {
            for (NElement cc : expectedColumns.children()) {
                String s = NSqlTsonUtils.nameOf(cc);
                if (s != null) {
                    NElement columnDefinition = NSqlTsonUtils.fieldByNameOf(s, expectedColumns);
                    NSqlColumn dc = defs == null ? null : defs.apply(s);
                    if (dc == null) {
                        dc = new NSqlColumn();
                        dc.setFieldName(s);
                    }
                    NSqlColumn e = NSqlColumn.of(columnDefinition, dc);

                }
            }
        }
        NSqlTable tt = new NSqlTable();
        tt.setTableName(tableName);
        tt.setColumns(columnsList.toArray(new NSqlColumn[0]));
        tt.setDdlAuto(mode==null? NDdlAuto.UPDATE : mode);
        if (!hasId) {
            throw new IllegalArgumentException("missing id field");
        }
        return tt;
    }

    public String getTableName() {
        return tableName;
    }

    public NSqlTable setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public NSqlColumn[] getColumns() {
        return columns;
    }

    public NSqlTable setColumns(NSqlColumn[] columns) {
        this.columns = columns;
        return this;
    }

    public NDdlAuto getDdlAuto() {
        return ddlAuto;
    }

    public NSqlTable setDdlAuto(NDdlAuto ddlAuto) {
        this.ddlAuto = ddlAuto;
        return this;
    }

    public NSqlTable prepareStatement(PreparedStatement ps, Function<String, Object> valueResolver) {
        for (NSqlColumn o : getColumns()) {
            if (o.autoIncrement== YesNo.YES) {
                continue;
            }
            int insertIndex = o.getInsertIndex() == null ? 0 : o.getInsertIndex();
            o.set(valueResolver.apply(o.resolveFieldOrColumnName()), insertIndex, ps);
        }

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NSqlTable sqlTable = (NSqlTable) o;
        return Objects.equals(tableName, sqlTable.tableName) && Objects.deepEquals(columns, sqlTable.columns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName, Arrays.hashCode(columns));
    }

    @Override
    public String toString() {
        return "SqlTable{" +
                "tableName='" + tableName + '\'' +
                ", columns=" + Arrays.toString(columns) +
                '}';
    }
}
