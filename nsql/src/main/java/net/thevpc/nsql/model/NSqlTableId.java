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

    /**
     * A relation named by itself, with no catalog and no schema. Only useful with the callers that
     * treat blank parts as wildcards (matches, getTableCount, escapeIdentifier); a strict
     * {@code equals} comparison against a metadata result can never match it.
     */
    public static NSqlTableId of(String tableName) {
        return new NSqlTableId(null, null, tableName);
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

    /**
     * Match another identifier, treating the blank parts of this one as wildcards. JDBC metadata always
     * reports a full catalog and schema, so {@code equals} can never answer a caller that only knows a
     * table name - which silently turned every {@code ...IfExists} table helper into a no-op for a bare
     * identifier. Non-blank parts compare exactly, because identifier case is the database's decision:
     * getTableDefinition passes the name to DatabaseMetaData.getTables as a pattern, which PostgreSQL
     * matches case-sensitively, so folding here would let tableExists report true for a relation the
     * same connection cannot load or drop.
     */
    public boolean matches(NSqlTableId other) {
        if (other == null) {
            return false;
        }
        if (!NBlankable.isBlank(catalogName) && !Objects.equals(catalogName, other.catalogName)) {
            return false;
        }
        if (!NBlankable.isBlank(schemaName) && !Objects.equals(schemaName, other.schemaName)) {
            return false;
        }
        return NBlankable.isBlank(tableName) || Objects.equals(tableName, other.tableName);
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
