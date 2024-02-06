package net.thevpc.dbinfo.util;

import net.thevpc.dbinfo.model.*;
import net.thevpc.dbinfo.store.ser1.*;
import net.thevpc.vio2.api.ObjectSerializer;
import net.thevpc.vio2.impl.Sers;
import net.thevpc.vio2.impl.StoreReaderConf;
import net.thevpc.vio2.api.StoreRows;
import net.thevpc.vio2.model.StoreFieldDefinition;
import net.thevpc.vio2.model.StoreRowsDefinition;
import net.thevpc.vio2.model.StoreStructDefinition;
import net.thevpc.vio2.model.StoreStructId;

public class DbInfoModuleInstaller {
    static {
        Sers sers = StoreReaderConf.get(DbrmanStoreVersions.V1);

        sers.addWriterAndReader(TableId.class, new TableIdSer());
        sers.addWriterAndReader(StoreStructId.class, (ObjectSerializer) new TableIdSer());

        sers.addWriterAndReader(ColumnDefinition.class, new ColumnDefinitionSer());
        sers.addWriterAndReader(StoreFieldDefinition.class, (ObjectSerializer) new ColumnDefinitionSer());

        sers.addWriterAndReader(StoreRows.class, new StoreRowsSer());

        sers.addWriterAndReader(TableDefinition.class, new TableDefinitionSer());
        sers.addWriterAndReader(StoreStructDefinition.class, (ObjectSerializer) new TableDefinitionSer());

        sers.addWriterAndReader(TableRowsDefinition.class, new TableRowsDefinitionSer());
        sers.addWriterAndReader(StoreRowsDefinition.class, (ObjectSerializer) new TableRowsDefinitionSer());
    }
    public static void init(){

    }
}
