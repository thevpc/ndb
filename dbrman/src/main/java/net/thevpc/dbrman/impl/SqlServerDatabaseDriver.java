/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.dbrman.impl;

import net.thevpc.dbrman.api.DatabaseHeader;
import net.thevpc.dbrman.api.DatabaseId;
import net.thevpc.dbrman.common.AbstractDatabaseDriver;
import net.thevpc.dbrman.model.ColumnDefinition;
import net.thevpc.dbrman.model.DefaultDatabaseHeader;
import net.thevpc.dbrman.model.TableId;
import net.thevpc.vio2.model.StoreDataType;

import java.sql.Connection;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author vpc
 */
public class SqlServerDatabaseDriver extends AbstractDatabaseDriver {
    public static Logger LOG = Logger.getLogger(SqlServerDatabaseDriver.class.getName());

    public SqlServerDatabaseDriver(Connection connection) {
        super(connection);
    }

    public String escapeIdentifier(String name) {
        return "[" + name + "]";
    }

    protected StoreDataType createFileColType(ColumnDefinition c) {
        return createDefaultFileColType(c);
    }

    protected String getDefaultDatabaseName() {
        return "master";
    }

    @Override
    public boolean isSpecialTable(TableId tableId) {
        if (tableId.getTableName().equals("trace_xe_action_map")) {
            return true;
        }
        if (tableId.getTableName().equals("trace_xe_event_map")) {
            return true;
        }
        return super.isSpecialTable(tableId);
    }

    @Override
    public List<DatabaseHeader> getDatabases() {
        return getCatalogs().stream().map(x -> new DefaultDatabaseHeader(
                x.getCatalogName(),
                null,
                x.getCatalogName()
        )).collect(Collectors.toList());
    }
}
