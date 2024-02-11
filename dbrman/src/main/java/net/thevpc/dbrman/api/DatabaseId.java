/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.dbrman.api;

import net.thevpc.dbrman.model.CatalogId;
import net.thevpc.dbrman.model.SchemaId;
import net.thevpc.dbrman.util.DbInfoModuleInstaller;
import net.thevpc.vio2.util.StringUtils;

/**
 * @author vpc
 */
public interface DatabaseId {
    String getCatalogName();
    String getSchemaName();
    String getDatabaseName();
    SchemaId toSchemaId();
    CatalogId toCatalogId();
}
