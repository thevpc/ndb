/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.dbrman.model;

import net.thevpc.dbrman.util.DbInfoModuleInstaller;
import net.thevpc.vio2.model.StoreFieldDefinition;
import net.thevpc.vio2.model.StoreRowsDefinition;
import net.thevpc.vio2.model.StoreStructHeader;
import net.thevpc.vio2.model.StoreStructId;


import java.util.Arrays;
import java.util.List;

/**
 * @author vpc
 */
public class TableRowsDefinition implements StoreRowsDefinition {
    static {
        DbInfoModuleInstaller.init();
    }
    private String resultName;
    private String tableType;
    private String tableName;
    private String catalogName;
    private String schemaName;
    private StoreFieldDefinition[] columns;

    public StoreStructId toTableId() {
        return new TableId(catalogName, schemaName, resultName);
    }

    public StoreStructHeader toTableHeader() {
        return new TableHeader(catalogName, schemaName, resultName, tableType);
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


    public List<StoreFieldDefinition> getColumns() {
        return Arrays.asList(columns);
    }

    public void setColumns(ColumnDefinition[] columns) {
        this.columns = columns;
    }

}
