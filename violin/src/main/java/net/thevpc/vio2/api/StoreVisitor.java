package net.thevpc.vio2.api;

import net.thevpc.vio2.model.StoreStructDefinition;

import java.util.List;

public interface StoreVisitor {
    void visitSchema(List<StoreStructDefinition> md);

    void visitData(StoreRows md);
    void visitEnd();
}
