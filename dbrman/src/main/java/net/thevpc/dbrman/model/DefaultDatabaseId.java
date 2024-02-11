package net.thevpc.dbrman.model;

import net.thevpc.dbrman.api.DatabaseId;
import net.thevpc.dbrman.util.DbInfoModuleInstaller;
import net.thevpc.vio2.util.StringUtils;

public class DefaultDatabaseId implements DatabaseId {
    static {
        DbInfoModuleInstaller.init();
    }

    private String catalogName;
    private String schemaName;
    private String databaseName;

    public DefaultDatabaseId(String catalogName, String schemaName, String databaseName) {
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

    public CatalogId toCatalogId() {
        return new CatalogId(catalogName);
    }

    public SchemaId toSchemaId() {
        return new SchemaId(catalogName, schemaName);
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
        final DefaultDatabaseId other = (DefaultDatabaseId) obj;
        if ((this.schemaName == null) ? (other.schemaName != null) : !this.schemaName.equals(other.schemaName)) {
            return false;
        }
        if ((this.catalogName == null) ? (other.catalogName != null) : !this.catalogName.equals(other.catalogName)) {
            return false;
        }
        return true;
    }

    public CatalogId getCatalogId() {
        return new CatalogId(catalogName);
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
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
        if (!StringUtils.isBlank(databaseName)) {
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
        if (!StringUtils.isBlank(databaseName)) {
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
