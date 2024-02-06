package net.thevpc.vio2.api;

import net.thevpc.vio2.model.StoreStructDefinition;
import net.thevpc.vio2.model.StoreStructHeader;
import net.thevpc.vio2.model.StoreStructId;

import java.util.List;
import java.util.function.Predicate;

public interface StoreWriterModel {
    StoreStructDefinition getDefinition(StoreStructId id);
    StoreRows getRows(StoreStructId id);

}
