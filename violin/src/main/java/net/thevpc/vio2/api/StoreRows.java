/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.vio2.api;

import net.thevpc.vio2.model.StoreStructDefinition;

import java.io.Closeable;
import java.util.Iterator;

/**
 * @author vpc
 */
public interface StoreRows extends Closeable {

    StoreStructDefinition getDefinition();

    IoRow nextRow();

    Iterable<IoRow> rowsIterable() ;

    Iterator<IoRow> rowsIterator() ;

    StoreRows skip(long skip) ;

    StoreRows limit(long limit) ;

    StoreRows filter(StoreRowFilter filter);

    void close();


}
