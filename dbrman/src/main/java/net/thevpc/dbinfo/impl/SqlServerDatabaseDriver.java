/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.dbinfo.impl;

import net.thevpc.dbinfo.common.AbstractDatabaseDriver;
import net.thevpc.dbinfo.model.ColumnDefinition;
import net.thevpc.dbinfo.model.TableId;
import net.thevpc.vio2.model.StoreDataType;

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
    protected StoreDataType createFileColType(ColumnDefinition c) {
        return createDefaultFileColType(c);
    }
    protected String getDefaultDatabaseName() {
        return "master";
    }

    @Override
    public boolean isSpecialTable(TableId tableId) {
        if(tableId.getTableName().equals("trace_xe_action_map")){
            return true;
        }
        if(tableId.getTableName().equals("trace_xe_event_map")){
            return true;
        }
        return super.isSpecialTable(tableId);
    }
}
