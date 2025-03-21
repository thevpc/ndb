package net.thevpc.nsql.dump.model;

import net.thevpc.lib.nserializer.model.StoreFieldDefinition;
import net.thevpc.lib.nserializer.model.StoreStructDefinition;
import net.thevpc.lib.nserializer.model.StoreStructHeader;
import net.thevpc.lib.nserializer.model.StoreStructId;
import net.thevpc.nsql.dump.common.SqlColumnAsStoreFieldDefinition;
import net.thevpc.nsql.dump.util.NSqlDumpModuleInstaller;
import net.thevpc.nsql.dump.util.SqlColumnTypeToStoreUtils;
import net.thevpc.nsql.model.NSqlTableDefinition;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TableDefinitionAsStoreStructDefinition implements StoreStructDefinition {
    static {
        NSqlDumpModuleInstaller.init();
    }

    private NSqlTableDefinition tableDefinition;

    public TableDefinitionAsStoreStructDefinition(NSqlTableDefinition tableDefinition) {
        this.tableDefinition = tableDefinition;
    }

    @Override
    public StoreStructId toStoreStructId() {
        return new TableIdAsStoreStructId(tableDefinition.getTableId());
    }

    @Override
    public int getColumnsCount() {
        return tableDefinition.getColumnsCount();
    }

    @Override
    public StoreStructHeader toTableHeader() {
        return new TableHeaderAsStoreStructHeader(tableDefinition.toTableHeader());
    }

    @Override
    public List<? extends StoreFieldDefinition> getColumns() {
        return tableDefinition.getColumns().stream().map(x-> new SqlColumnAsStoreFieldDefinition(SqlColumnTypeToStoreUtils.toStoreDataType(x), x)).collect(Collectors.toList());
    }

    public NSqlTableDefinition getTableDefinition() {
        return tableDefinition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableDefinitionAsStoreStructDefinition that = (TableDefinitionAsStoreStructDefinition) o;
        return Objects.equals(tableDefinition, that.tableDefinition);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tableDefinition);
    }
}
