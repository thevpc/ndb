/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.dbinfo.model;

import net.thevpc.dbinfo.util.DbInfoModuleInstaller;
import net.thevpc.vio2.model.StoreStructHeader;
import net.thevpc.vio2.util.StringUtils;

/**
 *
 * @author vpc
 */
public class TableHeader implements StoreStructHeader {
    static {
        DbInfoModuleInstaller.init();
    }
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String tableType;

    public TableHeader(String catalogName, String schemaName, String tableName, String tableType) {
        this.schemaName = schemaName;
        this.catalogName = catalogName;
        this.tableName = tableName;
        this.tableType = tableType;
    }

    public TableId toTableId() {
        return new TableId(catalogName, schemaName, tableName);
    }
    public String getSchemaName() {
        return schemaName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getTableName() {
        return tableName;
    }

    public boolean isTable() {
        return "TABLE".equalsIgnoreCase(tableType);
    }

    public String getTableType() {
        return tableType;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Table(");
        boolean first = true;
        if (!StringUtils.isBlank(catalogName)) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("catalog='" + catalogName + '\'');
        }
        if (!StringUtils.isBlank(schemaName)) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("schema='" + schemaName + '\'');
        }
        if (!StringUtils.isBlank(tableName)) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("table='" + tableName + '\'');
        }
        if (!StringUtils.isBlank(tableType)) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("type='" + tableType + '\'');
        }
        sb.append(")");
        return sb.toString();
    }
}
