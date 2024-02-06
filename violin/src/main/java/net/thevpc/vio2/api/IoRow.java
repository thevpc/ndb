package net.thevpc.vio2.api;

import net.thevpc.vio2.impl.RepeatableReadIoCell;
import net.thevpc.vio2.model.StoreFieldDefinition;
import net.thevpc.vio2.model.StoreStructDefinition;
import net.thevpc.vio2.model.StoreValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author vpc
 */
public interface IoRow {
    IoCell nextColumn();

    StoreStructDefinition getDefinition();

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

    default List<StoreValue> columns() {
        List<StoreValue> a=new ArrayList<>();
        for (IoCell ioCell : columnsIterable()) {
            a.add(new RepeatableReadIoCell(ioCell).getValue());
        }
        return a;
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
        for (StoreFieldDefinition column : getDefinition().getColumns()) {
            IoCell o = nextColumn();
            if(o!=null){
                o.consume();
            }
        }
    }
}
