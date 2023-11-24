package net.thevpc.diet.io;

import net.thevpc.diet.model.StoreColumnDefinition;
import net.thevpc.diet.model.StoreTableDefinition;

import java.util.Iterator;

/**
 * @author vpc
 */
public interface IoRow {
    IoCell nextColumn();

    StoreTableDefinition getDefinition();

    default Iterator<IoCell> columnsIterator() {
        return new Iterator<IoCell>() {
            IoCell curr;

            @Override
            public boolean hasNext() {
                curr = nextColumn();
                return curr != null;
            }

            @Override
            public IoCell next() {
                return curr;
            }
        };
    }

    default Iterable<IoCell> columnsIterable() {
        return new Iterable<IoCell>() {
            @Override
            public Iterator<IoCell> iterator() {
                return columnsIterator();
            }
        };
    }

    default void consume(){
        for (StoreColumnDefinition column : getDefinition().getColumns()) {
            IoCell o = nextColumn();
            if(o!=null){
                o.consume();
            }
        }
    }
}
