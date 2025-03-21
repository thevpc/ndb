/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nsql.model;


import net.thevpc.nsql.util.WithFullName;

/**
 * @author vpc
 */
public interface NSqlDatabaseId extends WithFullName {
    String getCatalogName();
    String getSchemaName();
    String getDatabaseName();
    NSqlSchemaId toSchemaId();
    NSqlCatalogId toCatalogId();
}
