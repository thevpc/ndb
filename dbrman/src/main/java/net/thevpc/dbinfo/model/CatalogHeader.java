/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.dbinfo.model;

import net.thevpc.dbinfo.util.DbInfoModuleInstaller;
import net.thevpc.vio2.util.StringUtils;

/**
 * @author vpc
 */
public class CatalogHeader {
    static {
        DbInfoModuleInstaller.init();
    }

    private String catalogName;

    public CatalogHeader(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public CatalogId toCatalogId() {
        return new CatalogId(catalogName);
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
