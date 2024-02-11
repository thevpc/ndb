/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.dbrman.model;

import net.thevpc.dbrman.api.DatabaseHeader;
import net.thevpc.dbrman.api.DatabaseId;
import net.thevpc.dbrman.util.DbInfoModuleInstaller;
import net.thevpc.vio2.util.StringUtils;

/**
 *
 * @author vpc
 */
public class DefaultDatabaseHeader implements DatabaseHeader {
    static {
        DbInfoModuleInstaller.init();
    }
    private String catalogName;
    private String schemaName;
    private String databaseName;
    public DefaultDatabaseHeader(String catalogName, String schemaName,String databaseName) {
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

    public DatabaseId toDatabaseId() {
        return new DefaultDatabaseId(catalogName, schemaName,databaseName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Database(");
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
            sb.append("schema='" + databaseName + '\'');
        }
        sb.append(")");
        return sb.toString();
    }
}
