package net.thevpc.vio2.impl;

import net.thevpc.vio2.api.IoRow;
import net.thevpc.vio2.api.StoreRows;
import net.thevpc.vio2.model.StoreStructDefinition;

public class StoreRowsFilter implements StoreRows {
    private StoreRows item;
    private RowPredicate filter;
    private long currIndex;
    private boolean stopped;

    public StoreRowsFilter(StoreRows item, RowPredicate filter) {
        this.item = item;
        this.filter = filter;
    }

    @Override
    public StoreStructDefinition getDefinition() {
        return item.getDefinition();
    }

    @Override
    public IoRow nextRow() {
        if (stopped) {
            return null;
        }
        while (true) {
            IoRow u = item.nextRow();
            if (u == null) {
                return null;
            }
            currIndex++;
            if (filter == null) {
                return u;
            }
            RowPredicateResult z = filter.accept(u, currIndex - 1);
            switch (z) {
                case ACCEPT: {
                    return u;
                }
                case SKIP: {
                    break;
                }
                case STOP: {
                    return null;
                }
                case STOP_AFTER: {
                    stopped = true;
                    return u;
                }
                default: {
                    throw new IllegalArgumentException("unsupported");
                }
            }
        }
    }

    @Override
    public void close() {
        item.close();
    }
}
