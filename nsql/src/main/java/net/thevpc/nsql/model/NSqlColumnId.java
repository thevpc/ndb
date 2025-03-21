/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nsql.model;

import net.thevpc.nsql.util.WithFullName;
import net.thevpc.nuts.util.NBlankable;

/**
 * @author vpc
 */
public class NSqlColumnId implements WithFullName {
//    static {
//        DbInfoModuleInstaller.init();
//    }

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;

    public NSqlColumnId(String catalogName, String schemaName, String tableName, String columnName) {
        this.schemaName = schemaName;
        this.catalogName = catalogName;
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
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


    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.schemaName != null ? this.schemaName.hashCode() : 0);
        hash = 67 * hash + (this.catalogName != null ? this.catalogName.hashCode() : 0);
        hash = 67 * hash + (this.tableName != null ? this.tableName.hashCode() : 0);
        hash = 67 * hash + (this.columnName != null ? this.columnName.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NSqlColumnId other = (NSqlColumnId) obj;
        if ((this.schemaName == null) ? (other.schemaName != null) : !this.schemaName.equals(other.schemaName)) {
            return false;
        }
        if ((this.catalogName == null) ? (other.catalogName != null) : !this.catalogName.equals(other.catalogName)) {
            return false;
        }
        if ((this.tableName == null) ? (other.tableName != null) : !this.tableName.equals(other.tableName)) {
            return false;
        }
        if ((this.catalogName == null) ? (other.catalogName != null) : !this.catalogName.equals(other.catalogName)) {
            return false;
        }
        return true;
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        if (!NBlankable.isBlank(catalogName)) {
            if (first) {
                first = false;
            } else {
                sb.append(".");
            }
            sb.append(catalogName);
        }
        if (!NBlankable.isBlank(schemaName)) {
            if (first) {
                first = false;
            } else {
                sb.append(".");
            }
            sb.append(schemaName);
        }
        if (!NBlankable.isBlank(tableName)) {
            if (first) {
                first = false;
            } else {
                sb.append(".");
            }
            sb.append(tableName);
        }
        if (!NBlankable.isBlank(columnName)) {
            if (first) {
                first = false;
            } else {
                sb.append("::");
            }
            sb.append(columnName);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Column(");
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
        if (!NBlankable.isBlank(columnName)) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("column='" + columnName + '\'');
        }
        sb.append(")");
        return sb.toString();
    }

    public NSqlTableId getStructId() {
        return getTableId();
    }

    public NSqlTableId getTableId() {
        return new NSqlTableId(catalogName, schemaName, tableName);
    }

    public NSqlSchemaId getSchemaId() {
        return new NSqlSchemaId(catalogName, schemaName);
    }

    public NSqlCatalogId getCatalogId() {
        return new NSqlCatalogId(catalogName);
    }

}
