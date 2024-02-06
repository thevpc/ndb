/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.dbinfo.model;

import net.thevpc.dbinfo.util.DbInfoModuleInstaller;
import net.thevpc.vio2.model.StoreStructId;
import net.thevpc.vio2.util.StringUtils;

/**
 * @author vpc
 */
public class CatalogId{
    static {
        DbInfoModuleInstaller.init();
    }
    private String catalogName;
    public CatalogId(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    @Override
    public int hashCode() {
        int hash = 3;
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
        final CatalogId other = (CatalogId) obj;
        if ((this.catalogName == null) ? (other.catalogName != null) : !this.catalogName.equals(other.catalogName)) {
            return false;
        }
        return true;
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
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Catalog(");
        boolean first = true;
        if (!StringUtils.isBlank(catalogName)) {
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
}
