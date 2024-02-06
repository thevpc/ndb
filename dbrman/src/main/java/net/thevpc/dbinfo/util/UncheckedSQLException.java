/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.dbinfo.util;

import java.sql.SQLException;

/**
 *
 * @author vpc
 */
public class UncheckedSQLException extends RuntimeException{

    public UncheckedSQLException(SQLException cause) {
        super(cause);
    }
    
}
