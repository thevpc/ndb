/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.vio2.model;

/**
 * @author vpc
 */
public interface StoreFieldDefinition {
    StoreDataType getStoreType();

    String getFullName();

    public StoreFieldId toFieldId();

}
