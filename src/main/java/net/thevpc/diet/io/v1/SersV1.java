package net.thevpc.diet.io.v1;

import net.thevpc.diet.io.IoCell;
import net.thevpc.diet.io.Sers;
import net.thevpc.diet.io.StoreRows;
import net.thevpc.diet.io.v1.s.*;
import net.thevpc.diet.model.StoreColumnDefinition;
import net.thevpc.diet.model.StoreRowsDefinition;
import net.thevpc.diet.model.StoreTableDefinition;
import net.thevpc.diet.model.TableId;
import net.thevpc.diet.io.v1.s.*;

public class SersV1 extends Sers {
    public SersV1() {
        addWriterAndReader(TableId.class, new TableIdSer());
        addWriterAndReader(StoreColumnDefinition.class, new StoreColumnDefinitionSer());
        addWriterAndReader(StoreRows.class, new StoreRowsSer());
        addWriterAndReader(StoreTableDefinition.class, new StoreTableDefinitionSer());
        addWriterAndReader(StoreRowsDefinition.class, new StoreRowsDefinitionSer());
    }
}
