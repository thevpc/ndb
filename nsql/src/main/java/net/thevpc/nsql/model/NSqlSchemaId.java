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
public class NSqlSchemaId implements WithFullName {
//    static {
//        DbInfoModuleInstaller.init();
//    }
    private String catalogName;
    private String schemaName;
    public NSqlSchemaId(String catalogName, String schemaName) {
        this.schemaName = schemaName;
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.schemaName != null ? this.schemaName.hashCode() : 0);
        hash = 67 * hash + (this.catalogName != null ? this.catalogName.hashCode() : 0);
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
        final NSqlSchemaId other = (NSqlSchemaId) obj;
        if ((this.schemaName == null) ? (other.schemaName != null) : !this.schemaName.equals(other.schemaName)) {
            return false;
        }
        if ((this.catalogName == null) ? (other.catalogName != null) : !this.catalogName.equals(other.catalogName)) {
            return false;
        }
        return true;
    }

    public NSqlCatalogId toCatalogId() {
        return new NSqlCatalogId(catalogName);
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
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Schema(");
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
        sb.append(")");
        return sb.toString();
    }
}
