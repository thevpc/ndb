/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.dbrman.model;

import net.thevpc.dbrman.util.DbInfoModuleInstaller;
import net.thevpc.vio2.model.StoreStructDefinition;
import net.thevpc.vio2.model.StoreStructHeader;
import net.thevpc.vio2.model.StoreStructId;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author vpc
 */
public class TableDefinition implements StoreStructDefinition,Cloneable {
    static {
        DbInfoModuleInstaller.init();
    }
    private String tableName;
    private String schemaName;
    private String catalogName;
    private String tableType;
    private String refGeneration;
    private String selfReferencingColName;
    private ColumnDefinition[] columns;

    public String getTableName() {
        return tableName;
    }

    public TableDefinition setTableName(String tableName) {
        this.tableName = tableName;
        _updateColumns();
        return this;
    }

    @Override
    public int getColumnsCount() {
        return columns.length;
    }

    public List<ColumnDefinition> getColumns() {
        return Arrays.asList(columns);
    }

    public TableDefinition setColumns(ColumnDefinition... columns) {
        this.columns = columns;
        return this;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public TableDefinition setSchemaName(String schemaName) {
        this.schemaName = schemaName;
        _updateColumns();
        return this;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public TableDefinition setCatalogName(String catalogName) {
        this.catalogName = catalogName;
        _updateColumns();
        return this;
    }

    public String getTableType() {
        return tableType;
    }

    public TableDefinition setTableType(String tableType) {
        this.tableType = tableType;
        return this;
    }

    public String getRefGeneration() {
        return refGeneration;
    }

    public TableDefinition setRefGeneration(String refGeneration) {
        this.refGeneration = refGeneration;
        return this;
    }

    public String getSelfReferencingColName() {
        return selfReferencingColName;
    }

    public TableDefinition setSelfReferencingColName(String selfReferencingColName) {
        this.selfReferencingColName = selfReferencingColName;
        return this;
    }

    @Override
    public StoreStructId toStoreStructId() {
        return new TableId(catalogName, schemaName, tableName);
    }

    public TableId getTableId() {
        return new TableId(catalogName, schemaName, tableName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableDefinition that = (TableDefinition) o;
        return Objects.equals(tableName, that.tableName) && Objects.equals(schemaName, that.schemaName) && Objects.equals(catalogName, that.catalogName) && Objects.equals(tableType, that.tableType) && Objects.equals(refGeneration, that.refGeneration) && Objects.equals(selfReferencingColName, that.selfReferencingColName) && Arrays.equals(columns, that.columns);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(tableName, schemaName, catalogName, tableType, refGeneration, selfReferencingColName);
        result = 31 * result + Arrays.hashCode(columns);
        return result;
    }

    @Override
    public StoreStructHeader toTableHeader() {
        return new TableHeader(catalogName, schemaName, tableName, tableType);
    }

    public TableDefinition copy() {
        return clone();
    }

    @Override
    protected TableDefinition clone() {
        TableDefinition clone = null;
        try {
            clone = (TableDefinition) super.clone();
            clone.columns=clone.columns==null?null:new ColumnDefinition[clone.columns.length];
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

    public TableDefinition setSchemaId(SchemaId ss) {
        this.catalogName = ss.getCatalogName();
        this.schemaName = ss.getSchemaName();
        _updateColumns();
        return this;
    }

    public TableDefinition setTableId(TableId ss) {
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
}
