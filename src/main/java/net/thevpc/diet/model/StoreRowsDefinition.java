/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.diet.model;

/**
 *
 * @author vpc
 */
public class StoreRowsDefinition {
    private String resultName;
    private String tableType;
    private String tableName;
    private String catalogName;
    private String schemaName;
    private StoreColumnDefinition[] columns;

    public TableId toTableId() {
        return new TableId(catalogName, schemaName, resultName);
    }
    public TableHeader toTableHeader() {
        return new TableHeader(catalogName, schemaName, resultName,tableType);
    }


    public String getResultName() {
        return resultName;
    }

    public void setResultName(String resultName) {
        this.resultName = resultName;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
    
    

    public StoreColumnDefinition[] getColumns() {
        return columns;
    }

    public void setColumns(StoreColumnDefinition[] columns) {
        this.columns = columns;
    }
    
}
