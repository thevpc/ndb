package net.thevpc.diet.test;

import net.thevpc.diet.io.*;
import net.thevpc.diet.io.v1.SersV1;
import net.thevpc.diet.model.StoreColumnDefinition;
import net.thevpc.diet.model.StoreDataType;
import net.thevpc.diet.model.StoreTableDefinition;
import net.thevpc.diet.model.YesNo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class Test1 {

    @Test
    public void test1() {
        StoreColumnDefinition c = getStoreColumnDefinition1();
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        StoreOutputStream sw = new StoreOutputStream(s, new SersV1());
        sw.writeNonNullableStruct(StoreColumnDefinition.class, c);
        sw.writeNonNullableStruct(StoreColumnDefinition.class, c);
        sw.flush();

        StoreInputStream sr = new StoreInputStream(new ByteArrayInputStream(s.toByteArray()), new SersV1());
        StoreColumnDefinition e1 = sr.readNonNullableStruct(StoreColumnDefinition.class);
        StoreColumnDefinition e2 = sr.readNonNullableStruct(StoreColumnDefinition.class);
        Assertions.assertEquals(c, e1);
        Assertions.assertEquals(c, e2);
    }

    @Test
    public void test2() {
        StoreTableDefinition c = getStoreTableDefinition1();
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        StoreOutputStream sw = new StoreOutputStream(s, new SersV1());
        sw.writeNonNullableStruct(StoreTableDefinition.class, c);
        sw.flush();

        StoreInputStream sr = new StoreInputStream(new ByteArrayInputStream(s.toByteArray()), new SersV1());
        StoreTableDefinition e1 = sr.readNonNullableStruct(StoreTableDefinition.class);
        Assertions.assertEquals(c, e1);
    }

    @Test
    public void test2b() {
        StoreTableDefinition c = getStoreTableDefinition1();
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        StoreOutputStream sw = new StoreOutputStream(s, new SersV1());
        sw.writeNullableStruct(StoreTableDefinition.class, c);
        sw.flush();

        StoreInputStream sr = new StoreInputStream(new ByteArrayInputStream(s.toByteArray()), new SersV1());
        StoreTableDefinition e1 = sr.readNullableStruct(StoreTableDefinition.class);
        Assertions.assertEquals(c, e1);
    }

    @Test
    public void test3() {
        StoreTableDefinition c = getStoreTableDefinition1();
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        StoreOutputStream sw = new StoreOutputStream(s, new SersV1());
        sw.writeNonNullableStruct(StoreTableDefinition.class, c);
        sw.writeNonNullableStruct(StoreTableDefinition.class, c);
        sw.flush();

        StoreInputStream sr = new StoreInputStream(new ByteArrayInputStream(s.toByteArray()), new SersV1());
        StoreTableDefinition e1 = sr.readNonNullableStruct(StoreTableDefinition.class);
        Assertions.assertEquals(c, e1);
        StoreTableDefinition e2 = sr.readNonNullableStruct(StoreTableDefinition.class);
        Assertions.assertEquals(c, e2);
    }

    @Test
    public void test4() {
        StoreTableDefinition[] c = new StoreTableDefinition[]{getStoreTableDefinition1()};
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        StoreOutputStream sw = new StoreOutputStream(s, new SersV1());
        sw.writeNonNullableStruct(StoreTableDefinition[].class, c);
        sw.writeNonNullableStruct(StoreTableDefinition[].class, c);
        sw.flush();

        StoreInputStream sr = new StoreInputStream(new ByteArrayInputStream(s.toByteArray()), new SersV1());
        StoreTableDefinition[] e1 = sr.readNonNullableStruct(StoreTableDefinition[].class);
        StoreTableDefinition[] e2 = sr.readNonNullableStruct(StoreTableDefinition[].class);
        Assertions.assertArrayEquals(c, e1);
        Assertions.assertArrayEquals(c, e2);
    }

    @Test
    public void test5() {
        StoreRows r = new DefaultStoreRows(
                getStoreTableDefinition1(),
                new Object[][]{
                        {
                                "val1"
                        },
                        {
                                "val2"
                        },
                }
        );

        ByteArrayOutputStream s = new ByteArrayOutputStream();
        StoreOutputStream sw = new StoreOutputStream(s, new SersV1());
        sw.writeNonNullableStruct(StoreRows.class, r);
        sw.flush();

        StoreInputStream sr = new StoreInputStream(new ByteArrayInputStream(s.toByteArray()), new SersV1());
        StoreRows e1 = sr.readNonNullableStruct(StoreRows.class);
        Assertions.assertEquals(r.getDefinition(), e1.getDefinition());
        for (IoRow rr : e1.rowsIterable()) {
            for (IoCell ioCell : rr.columnsIterable()) {
                System.out.print(ioCell.getObject());
            }
            System.out.println();
        }
    }

    @Test
    public void test6() {
        StoreRows r = new DefaultStoreRows(
                new StoreTableDefinition()
                        .setCatalogName(null)
                        .setSchemaName("schem")
                        .setTableName("tab")
                        .setColumns(
                                getStoreColumnDefinition2("val1", StoreDataType.BYTE_STREAM),
                                getStoreColumnDefinition2("val2", StoreDataType.BYTES)
                        ),
                new Object[][]{
                        {
                                new ByteArrayInputStream("val1".getBytes()),
                                "val2".getBytes()
                        }
                }
        );
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        StoreOutputStream sw = new StoreOutputStream(s, new SersV1());
        sw.writeNonNullableStruct(StoreRows.class, r);
        sw.flush();
        StoreInputStream sr = new StoreInputStream(new ByteArrayInputStream(s.toByteArray()), new SersV1());
        StoreRows e1 = sr.readNonNullableStruct(StoreRows.class);
        e1.consume();
//        Assertions.assertEquals(r.getDefinition(), e1.getDefinition());
//        for (IoRow rr : e1.rowsIterable()) {
//            for (IoCell ioCell : rr.columnsIterable()) {
//                System.out.print(ioCell.getObject());
//            }
//            System.out.println();
//        }
    }


    private static StoreTableDefinition getStoreTableDefinition1() {
        StoreTableDefinition t = new StoreTableDefinition();
        t.setCatalogName(null);
        t.setSchemaName("schem");
        t.setTableName("tab");
        t.setColumns(new StoreColumnDefinition[]{
                getStoreColumnDefinition1()
        });
        return t;
    }

    private static StoreColumnDefinition getStoreColumnDefinition1() {
        StoreColumnDefinition c = new StoreColumnDefinition();
        c.setIndex(1);
        c.setColumnName("a");
        c.setLabel("b");
        c.setStoreType(StoreDataType.STRING);
        c.setSqlType(4);
        c.setSqlTypeName("hoho");
        c.setDisplaySize(4);
        c.setPrecision(5);
        c.setScale(6);
        c.setJavaClassName("toto");
        c.setRadix(10);
        c.setNullable(YesNo.YES);
        c.setColumnDef("test2");
        c.setSchemaName("ScopeSchema");
        c.setCatalogName("ScopeCatalog");
        c.setTableName("ScopeTable");
        c.setSourceDataType(4);
        c.setAutoIncrement(YesNo.NO);
        c.setGeneratedColumns(YesNo.NO);
        return c;
    }

    private static StoreColumnDefinition getStoreColumnDefinition2(String name, StoreDataType t) {
        StoreColumnDefinition c = new StoreColumnDefinition();
        c.setIndex(1);
        c.setColumnName(name);
        c.setLabel("b");
        c.setStoreType(t);
        c.setSqlType(4);
        c.setSqlTypeName("hoho");
        c.setDisplaySize(4);
        c.setPrecision(5);
        c.setScale(6);
        c.setJavaClassName("toto");
        c.setRadix(10);
        c.setNullable(YesNo.YES);
        c.setColumnDef("test2");
        c.setSchemaName("ScopeSchema");
        c.setCatalogName("ScopeCatalog");
        c.setTableName("ScopeTable");
        c.setSourceDataType(4);
        c.setAutoIncrement(YesNo.NO);
        c.setGeneratedColumns(YesNo.NO);
        return c;
    }
}
