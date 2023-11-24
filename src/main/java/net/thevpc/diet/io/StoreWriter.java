package net.thevpc.diet.io;

import net.thevpc.diet.model.*;
import net.thevpc.diet.model.TableHeader;

import java.io.Closeable;
import java.util.function.Predicate;

public interface StoreWriter extends Closeable {

    StoreWriter write();

    StoreWriter flush();

    StoreWriter setData(boolean data);

    StoreWriter setMaxRows(long maxRows);
    StoreWriter setCompress(boolean compress);

    StoreWriter addTables(Predicate<TableHeader> table);
}
