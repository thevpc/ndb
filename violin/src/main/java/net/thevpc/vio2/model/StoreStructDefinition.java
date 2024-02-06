/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.vio2.model;

import java.util.List;

/**
 * @author vpc
 */
public interface StoreStructDefinition {
    List<? extends StoreFieldDefinition> getColumns();

    StoreStructId toStoreStructId();

    StoreStructHeader toTableHeader();
}
