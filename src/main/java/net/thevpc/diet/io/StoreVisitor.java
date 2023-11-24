package net.thevpc.diet.io;

import net.thevpc.diet.model.StoreTableDefinition;

public interface StoreVisitor {
    void visitSchema(StoreTableDefinition[] md);

    void visitData(StoreRows md);
    void visitEnd();
}
