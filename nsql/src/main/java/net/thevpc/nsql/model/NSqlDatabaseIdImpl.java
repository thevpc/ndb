package net.thevpc.nsql.model;

import net.thevpc.nuts.util.NBlankable;

public class NSqlDatabaseIdImpl implements NSqlDatabaseId {


    private String catalogName;
    private String schemaName;
    private String databaseName;

    public NSqlDatabaseIdImpl(String catalogName, String schemaName, String databaseName) {
        this.schemaName = schemaName;
        this.catalogName = catalogName;
        this.databaseName = databaseName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public NSqlCatalogId toCatalogId() {
        return new NSqlCatalogId(catalogName);
    }

    public NSqlSchemaId toSchemaId() {
        return new NSqlSchemaId(catalogName, schemaName);
    }

    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.schemaName != null ? this.schemaName.hashCode() : 0);
        hash = 67 * hash + (this.catalogName != null ? this.catalogName.hashCode() : 0);
        hash = 67 * hash + (this.databaseName != null ? this.databaseName.hashCode() : 0);
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
        final NSqlDatabaseIdImpl other = (NSqlDatabaseIdImpl) obj;
        if ((this.schemaName == null) ? (other.schemaName != null) : !this.schemaName.equals(other.schemaName)) {
            return false;
        }
        if ((this.catalogName == null) ? (other.catalogName != null) : !this.catalogName.equals(other.catalogName)) {
            return false;
        }
        return true;
    }

    public NSqlCatalogId getCatalogId() {
        return new NSqlCatalogId(catalogName);
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
        if (!NBlankable.isBlank(databaseName)) {
            if (first) {
                first = false;
            } else {
                sb.append(".");
            }
            sb.append(databaseName);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DefaultDatabaseId(");
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
        if (!NBlankable.isBlank(databaseName)) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("database='" + databaseName + '\'');
        }
        sb.append(")");
        return sb.toString();
    }

}
