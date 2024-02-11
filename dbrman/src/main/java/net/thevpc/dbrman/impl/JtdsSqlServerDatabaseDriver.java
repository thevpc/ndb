/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.dbrman.impl;

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
