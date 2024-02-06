/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.dbinfo.model;

import net.thevpc.dbinfo.util.DbInfoModuleInstaller;
import net.thevpc.vio2.model.StoreStructDefinition;
import net.thevpc.vio2.model.StoreStructHeader;
import net.thevpc.vio2.model.StoreStructId;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author vpc
 */
public class TableConstraintsDefinition {
    static {
        DbInfoModuleInstaller.init();
    }
    private String tableName;
    private String schemaName;
    private String catalogName;
    private String constraintsName;
    private TableConstraintsType tableConstraintsType;
    private String tableType;
    private String refGeneration;
    private String selfReferencingColName;
    private ColumnDefinition[] columns;

    public String getTableName() {
        return tableName;
    }

    public TableConstraintsDefinition setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public List<ColumnDefinition> getColumns() {
        return Arrays.asList(columns);
    }

    public TableConstraintsDefinition setColumns(ColumnDefinition... columns) {
        this.columns = columns;
        return this;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public TableConstraintsDefinition setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public TableConstraintsDefinition setCatalogName(String catalogName) {
        this.catalogName = catalogName;
        return this;
    }

    public String getTableType() {
        return tableType;
    }

    public TableConstraintsDefinition setTableType(String tableType) {
        this.tableType = tableType;
        return this;
    }

    public String getRefGeneration() {
        return refGeneration;
    }

    public TableConstraintsDefinition setRefGeneration(String refGeneration) {
        this.refGeneration = refGeneration;
        return this;
    }

    public String getSelfReferencingColName() {
        return selfReferencingColName;
    }

    public TableConstraintsDefinition setSelfReferencingColName(String selfReferencingColName) {
        this.selfReferencingColName = selfReferencingColName;
        return this;
    }

    public TableId getTableId() {
        return new TableId(catalogName, schemaName, tableName);
    }

    public TableConstraintsId getTableConstraintsId() {
        return new TableConstraintsId(catalogName, schemaName, tableName,constraintsName,tableConstraintsType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableConstraintsDefinition that = (TableConstraintsDefinition) o;
        return Objects.equals(tableName, that.tableName) && Objects.equals(schemaName, that.schemaName) && Objects.equals(catalogName, that.catalogName) && Objects.equals(tableType, that.tableType) && Objects.equals(refGeneration, that.refGeneration) && Objects.equals(selfReferencingColName, that.selfReferencingColName) && Arrays.equals(columns, that.columns);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(tableName, schemaName, catalogName, tableType, refGeneration, selfReferencingColName);
        result = 31 * result + Arrays.hashCode(columns);
        return result;
    }

    public TableHeader getTableHeader() {
        return new TableHeader(catalogName, schemaName, tableName, tableType);
    }

}
