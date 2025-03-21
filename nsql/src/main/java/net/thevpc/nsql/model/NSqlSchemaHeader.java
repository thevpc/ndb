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
public class NSqlSchemaHeader implements WithFullName {
    private String catalogName;
    private String schemaName;

    public NSqlSchemaHeader(String catalogName, String schemaName) {
        this.schemaName = schemaName;
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public NSqlSchemaId toSchemaId() {
        return new NSqlSchemaId(catalogName, schemaName);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NSqlSchemaHeader that = (NSqlSchemaHeader) o;
        return Objects.equals(catalogName, that.catalogName) && Objects.equals(schemaName, that.schemaName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catalogName, schemaName);
    }

    @Override
    public String getFullName() {
        return toSchemaId().getFullName();
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
