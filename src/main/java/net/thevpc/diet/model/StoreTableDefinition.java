/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.diet.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author vpc
 */
public class StoreTableDefinition {
    private String tableName;
    private String schemaName;
    private String catalogName;
    private String tableType;
    private String refGeneration;
    private String selfReferencingColName;
    private StoreColumnDefinition[] columns;

    public String getTableName() {
        return tableName;
    }

    public StoreTableDefinition setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public StoreColumnDefinition[] getColumns() {
        return columns;
    }

    public StoreTableDefinition setColumns(StoreColumnDefinition... columns) {
        this.columns = columns;
        return this;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public StoreTableDefinition setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public StoreTableDefinition setCatalogName(String catalogName) {
        this.catalogName = catalogName;
        return this;
    }

    public String getTableType() {
        return tableType;
    }

    public StoreTableDefinition setTableType(String tableType) {
        this.tableType = tableType;
        return this;
    }

    public String getRefGeneration() {
        return refGeneration;
    }

    public StoreTableDefinition setRefGeneration(String refGeneration) {
        this.refGeneration = refGeneration;
        return this;
    }

    public String getSelfReferencingColName() {
        return selfReferencingColName;
    }

    public StoreTableDefinition setSelfReferencingColName(String selfReferencingColName) {
        this.selfReferencingColName = selfReferencingColName;
        return this;
    }

    public TableId toTableId() {
        return new TableId(catalogName, schemaName, tableName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoreTableDefinition that = (StoreTableDefinition) o;
        return Objects.equals(tableName, that.tableName) && Objects.equals(schemaName, that.schemaName) && Objects.equals(catalogName, that.catalogName) && Objects.equals(tableType, that.tableType) && Objects.equals(refGeneration, that.refGeneration) && Objects.equals(selfReferencingColName, that.selfReferencingColName) && Arrays.equals(columns, that.columns);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(tableName, schemaName, catalogName, tableType, refGeneration, selfReferencingColName);
        result = 31 * result + Arrays.hashCode(columns);
        return result;
    }

    public TableHeader toTableHeader() {
        return new TableHeader(catalogName, schemaName, tableName, tableType);
    }

}
