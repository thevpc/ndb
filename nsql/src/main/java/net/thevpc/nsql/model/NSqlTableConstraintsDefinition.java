/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nsql.model;

import net.thevpc.nsql.NSqlColumn;
import net.thevpc.nsql.util.WithFullName;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author vpc
 */
public class NSqlTableConstraintsDefinition implements WithFullName {
    private String tableName;
    private String schemaName;
    private String catalogName;
    private String constraintsName;
    private NSqlTableConstraintsType tableConstraintsType;
    private String tableType;
    private String refGeneration;
    private String selfReferencingColName;
    private NSqlColumn[] columns;

    public String getTableName() {
        return tableName;
    }

    public NSqlTableConstraintsDefinition setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public List<NSqlColumn> getColumns() {
        return Arrays.asList(columns);
    }

    public NSqlTableConstraintsDefinition setColumns(NSqlColumn... columns) {
        this.columns = columns;
        return this;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public NSqlTableConstraintsDefinition setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public NSqlTableConstraintsDefinition setCatalogName(String catalogName) {
        this.catalogName = catalogName;
        return this;
    }

    public String getTableType() {
        return tableType;
    }

    public NSqlTableConstraintsDefinition setTableType(String tableType) {
        this.tableType = tableType;
        return this;
    }

    public String getRefGeneration() {
        return refGeneration;
    }

    public NSqlTableConstraintsDefinition setRefGeneration(String refGeneration) {
        this.refGeneration = refGeneration;
        return this;
    }

    public String getSelfReferencingColName() {
        return selfReferencingColName;
    }

    public NSqlTableConstraintsDefinition setSelfReferencingColName(String selfReferencingColName) {
        this.selfReferencingColName = selfReferencingColName;
        return this;
    }

    public NSqlTableId getTableId() {
        return new NSqlTableId(catalogName, schemaName, tableName);
    }

    public NSqlTableConstraintsId getTableConstraintsId() {
        return new NSqlTableConstraintsId(catalogName, schemaName, tableName,constraintsName,tableConstraintsType);
    }

    @Override
    public String getFullName() {
        return getTableConstraintsId().getFullName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NSqlTableConstraintsDefinition that = (NSqlTableConstraintsDefinition) o;
        return Objects.equals(tableName, that.tableName) && Objects.equals(schemaName, that.schemaName) && Objects.equals(catalogName, that.catalogName) && Objects.equals(tableType, that.tableType) && Objects.equals(refGeneration, that.refGeneration) && Objects.equals(selfReferencingColName, that.selfReferencingColName) && Arrays.equals(columns, that.columns);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(tableName, schemaName, catalogName, tableType, refGeneration, selfReferencingColName);
        result = 31 * result + Arrays.hashCode(columns);
        return result;
    }

    public NSqlTableHeader getTableHeader() {
        return new NSqlTableHeader(catalogName, schemaName, tableName, tableType);
    }

}
