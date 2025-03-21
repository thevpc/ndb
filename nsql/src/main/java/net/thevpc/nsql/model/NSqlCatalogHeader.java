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
public class NSqlCatalogHeader implements WithFullName {
    private String catalogName;

    public NSqlCatalogHeader(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public NSqlCatalogId toCatalogId() {
        return new NSqlCatalogId(catalogName);
    }

    @Override
    public String getFullName() {
        return toCatalogId().getFullName();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Catalog(");
        boolean first = true;
        if (!NBlankable.isBlank(catalogName)) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append("catalog='" + catalogName + '\'');
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NSqlCatalogHeader that = (NSqlCatalogHeader) o;
        return Objects.equals(catalogName, that.catalogName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(catalogName);
    }
}
