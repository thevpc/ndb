package net.thevpc.nsql.dump.util;

import net.thevpc.nsql.NSqlColumn;
import net.thevpc.nsql.dump.common.SqlColumnAsStoreFieldDefinition;
import net.thevpc.nsql.dump.model.DbrmanStoreVersions;
import net.thevpc.nsql.dump.model.TableDefinitionAsStoreStructDefinition;
import net.thevpc.nsql.dump.model.TableIdAsStoreStructId;
import net.thevpc.nsql.dump.model.TableRowsDefinitionAsStoreRowsDefinition;
import net.thevpc.nsql.dump.store.ser1.*;
import net.thevpc.lib.nserializer.api.ObjectSerializer;
import net.thevpc.lib.nserializer.impl.Sers;
import net.thevpc.lib.nserializer.impl.StoreReaderConf;
import net.thevpc.lib.nserializer.api.StoreRows;
import net.thevpc.lib.nserializer.model.StoreFieldDefinition;
import net.thevpc.lib.nserializer.model.StoreRowsDefinition;
import net.thevpc.lib.nserializer.model.StoreStructDefinition;
import net.thevpc.lib.nserializer.model.StoreStructId;

public class NSqlDumpModuleInstaller {
    static {
        Sers sers = StoreReaderConf.get(DbrmanStoreVersions.V1);

        sers.addWriterAndReader(TableIdAsStoreStructId.class, new TableIdSer());
        sers.addWriterAndReader(StoreStructId.class, (ObjectSerializer) new TableIdSer());

        sers.addWriterAndReader(SqlColumnAsStoreFieldDefinition.class, new SqlColumn2Ser());
        sers.addWriterAndReader(NSqlColumn.class, new SqlColumnSer());
        sers.addWriterAndReader(StoreFieldDefinition.class, (ObjectSerializer) new SqlColumnSer());

        sers.addWriterAndReader(StoreRows.class, new StoreRowsSer());

        sers.addWriterAndReader(TableDefinitionAsStoreStructDefinition.class, new TableDefinitionSer());
        sers.addWriterAndReader(StoreStructDefinition.class, (ObjectSerializer) new TableDefinitionSer());

        sers.addWriterAndReader(TableRowsDefinitionAsStoreRowsDefinition.class, new TableRowsDefinitionSer());
        sers.addWriterAndReader(StoreRowsDefinition.class, (ObjectSerializer) new TableRowsDefinitionSer());
    }
    public static void init(){

    }
}
