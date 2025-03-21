/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nsql.model;

import net.thevpc.nsql.util.WithFullName;
import net.thevpc.nuts.util.NBlankable;

import java.util.Objects;

/**
 * @author vpc
 */
public class NSqlTableHeader implements WithFullName {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String tableType;

    public NSqlTableHeader(String catalogName, String schemaName, String tableName, String tableType) {
        this.schemaName = schemaName;
        this.catalogName = catalogName;
        this.tableName = tableName;
        this.tableType = tableType;
    }

    public NSqlTableId toTableId() {
        return new NSqlTableId(catalogName, schemaName, tableName);
    }

    @Override
    public String getFullName() {
        return toTableId().getFullName();
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

    public boolean isSystemTable() {
        return "SYSTEM TABLE".equalsIgnoreCase(tableType);
    }

    public boolean isView() {
        return "VIEW".equalsIgnoreCase(tableType);
    }

    public String getTableType() {
        return tableType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NSqlTableHeader that = (NSqlTableHeader) o;
        return Objects.equals(catalogName, that.catalogName) && Objects.equals(schemaName, that.schemaName) && Objects.equals(tableName, that.tableName) && Objects.equals(tableType, that.tableType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catalogName, schemaName, tableName, tableType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Table(");
        boolean first = true;
        if (!NBlankable.isBlank(catalogName)) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("catalog='" + catalogName + '\'');
        }
        if (!NBlankable.isBlank(schemaName)) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("schema='" + schemaName + '\'');
        }
        if (!NBlankable.isBlank(tableName)) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("table='" + tableName + '\'');
        }
        if (!NBlankable.isBlank(tableType)) {
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
