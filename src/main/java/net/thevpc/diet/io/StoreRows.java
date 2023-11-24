/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.diet.io;

import net.thevpc.diet.model.StoreColumnDefinition;
import net.thevpc.diet.model.StoreTableDefinition;

import java.io.Closeable;
import java.util.Iterator;

/**
 * @author vpc
 */
public interface StoreRows extends Closeable {

    StoreTableDefinition getDefinition();

    IoRow nextRow();

    default Iterable<IoRow> rowsIterable() {
        return new Iterable<IoRow>() {
            @Override
            public Iterator<IoRow> iterator() {
                return StoreRows.this.rowsIterator();
            }
        };
    }

    default Iterator<IoRow> rowsIterator() {
        return new Iterator<IoRow>() {
            IoRow curr;

            @Override
            public boolean hasNext() {
                curr = StoreRows.this.nextRow();
                return curr != null;
            }

            @Override
            public IoRow next() {
                return curr;
            }
        };
    }

    default StoreRows skip(long skip) {
        if (skip <= 0) {
            return this;
        }
        return filter(new RowPredicate() {
            @Override
            public RowPredicateResult accept(IoRow row, long index) {
                if (index <= skip) {
                    return RowPredicateResult.SKIP;
                }
                return RowPredicateResult.ACCEPT;
            }
        });
    }

    default StoreRows limit(long limit) {
        return filter(new RowPredicate() {
            @Override
            public RowPredicateResult accept(IoRow row, long index) {
                if (limit >= 0 && index >= limit) {
                    return RowPredicateResult.STOP;
                }
                return RowPredicateResult.ACCEPT;
            }
        });
    }

    default StoreRows filter(RowPredicate filter) {
        return new StoreRowsFilter(this, filter);
    }

    default void consume() {
        StoreColumnDefinition[] columns = getDefinition().getColumns();
        while (true) {
            IoRow r = nextRow();
            if (r != null) {
                r.consume();
            } else {
                return;
            }
        }
    }

    enum RowPredicateResult {
        SKIP,
        STOP,
        STOP_AFTER,
        ACCEPT,
    }

    interface RowPredicate {
        RowPredicateResult accept(IoRow row, long index);
    }

    @Override
    default void close() {

    }
}
