/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.diet.model;

import java.util.Optional;

/**
 *
 * @author vpc
 */
public enum StoreDataType {
    NULL(0),
    STRING(1),
    NSTRING(2),
    BYTE(3),
    NBYTE(4),
    BOOLEAN(5),
    NBOOLEAN(6),
    SHORT(7),
    NSHORT(8),
    INT(9),
    NINT(10),
    LONG(11),
    NLONG(12),
    FLOAT(13),
    NFLOAT(14),
    DOUBLE(15),
    NDOUBLE(16),
    BYTES(17),
    NBYTES(18),
    BIG_INT(19),
    NBIG_INT(20),
    BIG_DECIMAL(21),
    NBIG_DECIMAL(12),
    DATE(23),
    NDATE(24),
    TIME(25),
    NTIME(26),
    TIMESTAMP(27),
    NTIMESTAMP(28),
    BYTE_STREAM(29),
    NBYTE_STREAM(30),
    CHAR_STREAM(31),
    NCHAR_STREAM(32),
    DOCUMENT(33),
    NDOCUMENT(34),
//    ARRAY(19),
    JAVA_OBJECT(-126),
    NJAVA_OBJECT(-127),
    ;
    private int id;

    public int id() {
        return id;
    }

    private StoreDataType(int id) {
        this.id = id;
    }

    public static Optional<StoreDataType> ofId(int id) {
        for (StoreDataType value : StoreDataType.values()) {
            if (value.id() == id) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

}
