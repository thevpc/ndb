/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.dbrman.model;

import net.thevpc.dbrman.util.DbInfoModuleInstaller;
import net.thevpc.vio2.util.StringUtils;

/**
 *
 * @author vpc
 */
public class SchemaHeader {
    static {
        DbInfoModuleInstaller.init();
    }
    private String catalogName;
    private String schemaName;
    public SchemaHeader(String catalogName, String schemaName) {
        this.schemaName = schemaName;
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public SchemaId toSchemaId() {
        return new SchemaId(catalogName, schemaName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Schema(");
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
        sb.append(")");
        return sb.toString();
    }
}
