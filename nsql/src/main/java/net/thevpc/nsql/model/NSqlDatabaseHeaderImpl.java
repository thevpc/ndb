/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nsql.model;

import net.thevpc.nuts.util.NBlankable;

import java.util.Objects;

/**
 *
 * @author vpc
 */
public class NSqlDatabaseHeaderImpl implements NSqlDatabaseHeader {
//    static {
//        DbInfoModuleInstaller.init();
//    }
    private String catalogName;
    private String schemaName;
    private String databaseName;
    public NSqlDatabaseHeaderImpl(String catalogName, String schemaName, String databaseName) {
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

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    public NSqlDatabaseId toDatabaseId() {
        return new NSqlDatabaseIdImpl(catalogName, schemaName,databaseName);
    }

    @Override
    public String getFullName() {
        return toDatabaseId().getFullName();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Database(");
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
            sb.append("schema='" + databaseName + '\'');
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NSqlDatabaseHeaderImpl that = (NSqlDatabaseHeaderImpl) o;
        return Objects.equals(catalogName, that.catalogName) && Objects.equals(schemaName, that.schemaName) && Objects.equals(databaseName, that.databaseName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catalogName, schemaName, databaseName);
    }
}
