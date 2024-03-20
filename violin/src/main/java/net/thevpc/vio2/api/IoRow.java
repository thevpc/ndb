package net.thevpc.vio2.api;

import net.thevpc.vio2.model.StoreStructDefinition;

import java.io.Closeable;

/**
 * @author vpc
 */
public interface IoRow extends Closeable {
    IoCell[] getColumns();

    StoreStructDefinition getDefinition();
    IoRow repeatable();

    default IoCell findColumn(String name){
        for (IoCell cell : getColumns()) {
            if(cell.getDefinition().getFieldName().equalsIgnoreCase(name)){
                return cell;
            }
        }
        return null;
    }

    @Override
    default void close() {
        for (IoCell column : getColumns()) {
            column.close();
        }
    }
}
