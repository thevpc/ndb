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
public class NSqlTableDefinition implements Cloneable, WithFullName {
    private String tableName;
    private String schemaName;
    private String catalogName;
    private String tableType;
    private String refGeneration;
    private String selfReferencingColName;
    private NSqlColumn[] columns;

    public String getTableName() {
        return tableName;
    }

    public NSqlTableDefinition setTableName(String tableName) {
        this.tableName = tableName;
        _updateColumns();
        return this;
    }

    public int getColumnsCount() {
        return columns.length;
    }

    public List<NSqlColumn> getColumns() {
        return Arrays.asList(columns);
    }

    public NSqlTableDefinition setColumns(NSqlColumn... columns) {
        this.columns = columns;
        return this;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public NSqlTableDefinition setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        _updateColumns();
        return this;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public NSqlTableDefinition setCatalogName(String catalogName) {
        this.catalogName = catalogName;
        _updateColumns();
        return this;
    }

    public String getTableType() {
        return tableType;
    }

    public NSqlTableDefinition setTableType(String tableType) {
        this.tableType = tableType;
        return this;
    }

    public String getRefGeneration() {
        return refGeneration;
    }

    public NSqlTableDefinition setRefGeneration(String refGeneration) {
        this.refGeneration = refGeneration;
        return this;
    }

    public String getSelfReferencingColName() {
        return selfReferencingColName;
    }

    public NSqlTableDefinition setSelfReferencingColName(String selfReferencingColName) {
        this.selfReferencingColName = selfReferencingColName;
        return this;
    }

    public NSqlTableId getTableId() {
        return new NSqlTableId(catalogName, schemaName, tableName);
    }

    @Override
    public String getFullName() {
        return getTableId().getFullName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NSqlTableDefinition that = (NSqlTableDefinition) o;
        return Objects.equals(tableName, that.tableName) && Objects.equals(schemaName, that.schemaName) && Objects.equals(catalogName, that.catalogName) && Objects.equals(tableType, that.tableType) && Objects.equals(refGeneration, that.refGeneration) && Objects.equals(selfReferencingColName, that.selfReferencingColName) && Arrays.equals(columns, that.columns);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(tableName, schemaName, catalogName, tableType, refGeneration, selfReferencingColName);
        result = 31 * result + Arrays.hashCode(columns);
        return result;
    }

    public NSqlTableHeader toTableHeader() {
        return new NSqlTableHeader(catalogName, schemaName, tableName, tableType);
    }

    public NSqlTableDefinition copy() {
        return clone();
    }

    @Override
    protected NSqlTableDefinition clone() {
        NSqlTableDefinition clone = null;
        try {
            clone = (NSqlTableDefinition) super.clone();
            clone.columns=clone.columns==null?null:new NSqlColumn[clone.columns.length];
            if(clone.columns!=null) {
                for (int i = 0; i < this.columns.length; i++) {
                    if (this.columns[i] != null) {
                        clone.columns[i] = this.columns[i].copy();
                    }
                }
            }
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return clone;
    }

    public NSqlTableDefinition setSchemaId(NSqlSchemaId ss) {
        this.catalogName = ss.getCatalogName();
        this.schemaName = ss.getSchemaName();
        _updateColumns();
        return this;
    }

    public NSqlTableDefinition setTableId(NSqlTableId ss) {
        this.catalogName = ss.getCatalogName();
        this.schemaName = ss.getSchemaName();
        this.tableName = ss.getTableName();
        _updateColumns();
        return this;
    }

    private void _updateColumns(){
        if(this.columns!=null){
            for (int i = 0; i < this.columns.length; i++) {
                if(this.columns[i]!=null){
                    this.columns[i]=this.columns[i].copy().setTableId(getTableId());
                }
            }
        }
    }

    @Override
    public String toString() {
        return "TableDefinition{" +
                "tableName='" + tableName + '\'' +
                ", schemaName='" + schemaName + '\'' +
                ", catalogName='" + catalogName + '\'' +
                ", tableType='" + tableType + '\'' +
                '}';
    }
}
