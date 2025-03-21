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
public class NSqlTableId implements WithFullName {
//    static {
//        DbInfoModuleInstaller.init();
//    }
    private String catalogName;
    private String schemaName;
    private String tableName;

    public NSqlTableId(NSqlSchemaId s, String tableName) {
        this(s.getCatalogName(),s.getSchemaName(),tableName);
    }

    public NSqlTableId(String catalogName, String schemaName, String tableName) {
        this.schemaName = schemaName;
        this.catalogName = catalogName;
        this.tableName = tableName;
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

    public NSqlCatalogId getCatalogId() {
        return new NSqlCatalogId(catalogName);
    }
    public NSqlSchemaId getSchemaId() {
        return new NSqlSchemaId(catalogName,schemaName);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.schemaName != null ? this.schemaName.hashCode() : 0);
        hash = 67 * hash + (this.catalogName != null ? this.catalogName.hashCode() : 0);
        hash = 67 * hash + (this.tableName != null ? this.tableName.hashCode() : 0);
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
        final NSqlTableId other = (NSqlTableId) obj;
        if ((this.schemaName == null) ? (other.schemaName != null) : !this.schemaName.equals(other.schemaName)) {
            return false;
        }
        if ((this.catalogName == null) ? (other.catalogName != null) : !this.catalogName.equals(other.catalogName)) {
            return false;
        }
        return (this.tableName == null) ? (other.tableName == null) : this.tableName.equals(other.tableName);
    }

    public String getFullName() {
        StringBuilder sb=new StringBuilder();
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
        return sb.toString();
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
        sb.append(")");
        return sb.toString();
    }
}
