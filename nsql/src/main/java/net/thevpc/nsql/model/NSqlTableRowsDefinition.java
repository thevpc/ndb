/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nsql.model;

import net.thevpc.nsql.NSqlColumn;


import java.util.Arrays;
import java.util.List;

/**
 * @author vpc
 */
public class NSqlTableRowsDefinition {
    private String resultName;
    private String tableType;
    private String tableName;
    private String catalogName;
    private String schemaName;
    private NSqlColumn[] columns;

    public NSqlTableId toTableId() {
        return new NSqlTableId(catalogName, schemaName, resultName);
    }

    public NSqlTableHeader toTableHeader() {
        return new NSqlTableHeader(catalogName, schemaName, resultName, tableType);
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


    public List<NSqlColumn> getColumns() {
        return Arrays.asList(columns);
    }

    public void setColumns(NSqlColumn[] columns) {
        this.columns = columns;
    }

}
