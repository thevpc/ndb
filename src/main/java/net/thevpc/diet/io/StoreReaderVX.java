/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.diet.io;

import java.io.Closeable;

/**
 * @author vpc
 */
public interface StoreReaderVX extends Closeable {

    void visit(StoreVisitor visitor);
}
