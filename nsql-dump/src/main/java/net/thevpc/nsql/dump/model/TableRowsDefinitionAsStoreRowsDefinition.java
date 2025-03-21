package net.thevpc.nsql.dump.model;

import net.thevpc.nsql.dump.common.SqlColumnAsStoreFieldDefinition;
import net.thevpc.nsql.dump.util.NSqlDumpModuleInstaller;
import net.thevpc.nsql.dump.util.SqlColumnTypeToStoreUtils;
import net.thevpc.nsql.model.NSqlTableRowsDefinition;
import net.thevpc.lib.nserializer.model.StoreFieldDefinition;
import net.thevpc.lib.nserializer.model.StoreRowsDefinition;
import net.thevpc.lib.nserializer.model.StoreStructHeader;
import net.thevpc.lib.nserializer.model.StoreStructId;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TableRowsDefinitionAsStoreRowsDefinition implements StoreRowsDefinition {
    static {
        NSqlDumpModuleInstaller.init();
    }
    private NSqlTableRowsDefinition tableRowsDefinition;

    public TableRowsDefinitionAsStoreRowsDefinition(NSqlTableRowsDefinition tableRowsDefinition) {
        this.tableRowsDefinition = tableRowsDefinition;
    }

    @Override
    public StoreStructId toTableId() {
        return new TableIdAsStoreStructId(tableRowsDefinition.toTableId());
    }

    @Override
    public StoreStructHeader toTableHeader() {
        return new TableHeaderAsStoreStructHeader(tableRowsDefinition.toTableHeader());
    }

    @Override
    public List<StoreFieldDefinition> getColumns() {
        return tableRowsDefinition.getColumns().stream().map(x -> new SqlColumnAsStoreFieldDefinition(SqlColumnTypeToStoreUtils.toStoreDataType(x), x)).collect(Collectors.toList());
    }

    public NSqlTableRowsDefinition getTableRowsDefinition() {
        return tableRowsDefinition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableRowsDefinitionAsStoreRowsDefinition that = (TableRowsDefinitionAsStoreRowsDefinition) o;
        return Objects.equals(tableRowsDefinition, that.tableRowsDefinition);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tableRowsDefinition);
    }
}
