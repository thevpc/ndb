/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.diet.sql.impl;

import net.thevpc.diet.model.StoreColumnDefinition;
import net.thevpc.diet.model.StoreDataType;
import net.thevpc.diet.sql.AbstractDatabaseDriver;

import java.sql.Connection;
import java.util.logging.Logger;

/**
 *
 * @author vpc
 */
public class SqlServerDatabaseDriver extends AbstractDatabaseDriver {
    public static Logger LOG = Logger.getLogger(SqlServerDatabaseDriver.class.getName());
    public SqlServerDatabaseDriver(Connection connection) {
        super(connection);
    }
    public String escapeIdentifier(String name) {
        return "["+name+"]";
    }
    protected StoreDataType createFileColType(StoreColumnDefinition c) {
        return createDefaultFileColType(c);
    }
    protected String getDefaultDatabaseName() {
        return "master";
    }

}
