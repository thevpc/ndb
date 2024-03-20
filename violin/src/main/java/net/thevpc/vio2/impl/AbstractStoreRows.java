package net.thevpc.vio2.impl;

import net.thevpc.vio2.api.IoRow;
import net.thevpc.vio2.api.StoreRowAction;
import net.thevpc.vio2.api.StoreRowFilter;
import net.thevpc.vio2.api.StoreRows;

import java.util.Iterator;

public abstract class AbstractStoreRows implements StoreRows {
    public void close() {
        while (true) {
            IoRow r = null;
            try {
                r = nextRow();
                if (r == null) {
                    return;
                }
            } finally {
                if (r != null) {
                    r.close();
                }
            }
        }
    }

    public Iterable<IoRow> rowsIterable() {
        return new Iterable<IoRow>() {
            @Override
            public Iterator<IoRow> iterator() {
                return AbstractStoreRows.this.rowsIterator();
            }
        };
    }

    public Iterator<IoRow> rowsIterator() {
        return new Iterator<IoRow>() {
            IoRow curr;

            @Override
            public boolean hasNext() {
                curr = AbstractStoreRows.this.nextRow();
                return curr != null;
            }

            @Override
            public IoRow next() {
                return curr;
            }
        };
    }

    public StoreRows skip(long skip) {
        if (skip <= 0) {
            return this;
        }
        return filter(new StoreRowFilter() {
            @Override
            public StoreRowAction accept(IoRow row, long index) {
                if (index <= skip) {
                    return StoreRowAction.SKIP;
                }
                return StoreRowAction.ACCEPT;
            }
        });
    }

    public StoreRows limit(long limit) {
        return filter(new StoreRowFilter() {
            @Override
            public StoreRowAction accept(IoRow row, long index) {
                if (limit >= 0 && index >= limit) {
                    return StoreRowAction.STOP;
                }
                return StoreRowAction.ACCEPT;
            }
        });
    }

    public StoreRows filter(StoreRowFilter filter) {
        return new StoreRowsFilter(this, filter);
    }

}
