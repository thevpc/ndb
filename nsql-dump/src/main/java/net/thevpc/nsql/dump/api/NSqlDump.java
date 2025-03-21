/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nsql.dump.api;


import net.thevpc.nsql.dump.options.TableRestoreOptions;
import net.thevpc.nsql.*;
import net.thevpc.nsql.model.*;
import net.thevpc.lib.nserializer.api.IoCell;
import net.thevpc.lib.nserializer.api.StoreRows;
import net.thevpc.lib.nserializer.model.StoreRowsDefinition;


import java.io.Closeable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

/**
 * @author vpc
 */
public interface NSqlDump extends Closeable {
    static NSqlDump of(String cnxInfo) {
        return NSqlDumpFactory.of(cnxInfo).createDatabaseDriver();
    }

    static NSqlDump of(NSqlConnectionStringBuilder cnxInfo) {
        return NSqlDumpFactory.of(cnxInfo).createDatabaseDriver();
    }

    static NSqlDump of(String url, String login, String pwd, NSqlDialect type) {
        return NSqlDumpFactory.of(url, login, pwd, type).createDatabaseDriver();
    }

    StoreRowsDefinition createRowsDefinition(ResultSetMetaData rs);

    NSqlConnection getConnection();

    StoreRows getStoreRows(NSqlTableId table);

    IoCell createCell(ResultSet rs, NSqlColumn column);

    void close();

    void importData(StoreRows rows, TableRestoreOptions options);

}
