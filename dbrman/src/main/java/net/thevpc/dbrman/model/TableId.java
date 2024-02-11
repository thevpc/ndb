/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.dbrman.model;

import net.thevpc.dbrman.util.DbInfoModuleInstaller;
import net.thevpc.vio2.model.StoreStructId;
import net.thevpc.vio2.util.StringUtils;

/**
 * @author vpc
 */
public class TableId implements StoreStructId {
    static {
        DbInfoModuleInstaller.init();
    }
    private String catalogName;
    private String schemaName;
    private String tableName;

    public TableId(SchemaId s, String tableName) {
        this(s.getCatalogName(),s.getSchemaName(),tableName);
    }

    public TableId(String catalogName, String schemaName, String tableName) {
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

    public CatalogId getCatalogId() {
        return new CatalogId(catalogName);
    }
    public SchemaId getSchemaId() {
        return new SchemaId(catalogName,schemaName);
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
        final TableId other = (TableId) obj;
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
        if (!StringUtils.isBlank(catalogName)) {
            if (first) {
                first = false;
            } else {
                sb.append(".");
            }
            sb.append(catalogName);
        }
        if (!StringUtils.isBlank(schemaName)) {
            if (first) {
                first = false;
            } else {
                sb.append(".");
            }
            sb.append(schemaName);
        }
        if (!StringUtils.isBlank(tableName)) {
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
        sb.append(")");
        return sb.toString();
    }
}
