/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.dbinfo.impl;

import net.thevpc.dbinfo.common.AbstractDatabaseDriver;
import net.thevpc.dbinfo.model.ColumnDefinition;
import net.thevpc.vio2.model.StoreDataType;

import java.sql.Connection;
import java.util.logging.Logger;

/**
 *
 * @author vpc
 */
public class JtdsSqlServerDatabaseDriver extends SqlServerDatabaseDriver {
    public static Logger LOG = Logger.getLogger(JtdsSqlServerDatabaseDriver.class.getName());
    public JtdsSqlServerDatabaseDriver(Connection connection) {
        super(connection);
    }
}
