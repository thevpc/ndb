/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nsql.dump.impl;

import net.thevpc.nsql.dump.common.DefaultNSqlDump;
import net.thevpc.nsql.NSqlConnection;

import java.util.logging.Logger;

/**
 * @author vpc
 */
public class SqlServerDatabaseDriver extends DefaultNSqlDump {
    public static Logger LOG = Logger.getLogger(SqlServerDatabaseDriver.class.getName());

    public SqlServerDatabaseDriver(NSqlConnection connection) {
        super(connection);
    }


}
