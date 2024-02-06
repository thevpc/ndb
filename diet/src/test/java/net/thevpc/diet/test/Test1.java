package net.thevpc.diet.test;

import net.thevpc.dbinfo.model.ColumnDefinition;
import net.thevpc.dbinfo.model.TableDefinition;
import net.thevpc.vio2.api.*;
import net.thevpc.vio2.impl.DefaultStoreRows;
import net.thevpc.vio2.impl.StoreInputStreamImpl;
import net.thevpc.vio2.impl.StoreOutputStreamImpl;
import net.thevpc.vio2.impl.StoreReaderConf;

import net.thevpc.vio2.model.StoreDataType;
import net.thevpc.vio2.model.StoreStructDefinition;
import net.thevpc.vio2.model.YesNo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class Test1 {

    @Test
    public void test1() {
        ColumnDefinition c = getStoreColumnDefinition1();
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        StoreOutputStream sw = new StoreOutputStreamImpl(s, StoreReaderConf.get(1));
        sw.writeNonNullableStruct(ColumnDefinition.class, c);
        sw.writeNonNullableStruct(ColumnDefinition.class, c);
        sw.flush();

        StoreInputStream sr = new StoreInputStreamImpl(new ByteArrayInputStream(s.toByteArray()), StoreReaderConf.get(1));
        ColumnDefinition e1 = sr.readNonNullableStruct(ColumnDefinition.class);
        ColumnDefinition e2 = sr.readNonNullableStruct(ColumnDefinition.class);
        Assertions.assertEquals(c, e1);
        Assertions.assertEquals(c, e2);
    }

    @Test
    public void test2() {
        StoreStructDefinition c = getStoreTableDefinition1();
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        StoreOutputStream sw = new StoreOutputStreamImpl(s, StoreReaderConf.get(1));
        sw.writeNonNullableStruct(StoreStructDefinition.class, c);
        sw.flush();

        StoreInputStream sr = new StoreInputStreamImpl(new ByteArrayInputStream(s.toByteArray()), StoreReaderConf.get(1));
        StoreStructDefinition e1 = sr.readNonNullableStruct(StoreStructDefinition.class);
        Assertions.assertEquals(c, e1);
    }

    @Test
    public void test2b() {
        StoreStructDefinition c = getStoreTableDefinition1();
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        StoreOutputStream sw = new StoreOutputStreamImpl(s, StoreReaderConf.get(1));
        sw.writeNullableStruct(StoreStructDefinition.class, c);
        sw.flush();

        StoreInputStream sr = new StoreInputStreamImpl(new ByteArrayInputStream(s.toByteArray()), StoreReaderConf.get(1));
        StoreStructDefinition e1 = sr.readNullableStruct(StoreStructDefinition.class);
        Assertions.assertEquals(c, e1);
    }

    @Test
    public void test3() {
        StoreStructDefinition c = getStoreTableDefinition1();
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        StoreOutputStream sw = new StoreOutputStreamImpl(s, StoreReaderConf.get(1));
        sw.writeNonNullableStruct(StoreStructDefinition.class, c);
        sw.writeNonNullableStruct(StoreStructDefinition.class, c);
        sw.flush();

        StoreInputStream sr = new StoreInputStreamImpl(new ByteArrayInputStream(s.toByteArray()), StoreReaderConf.get(1));
        StoreStructDefinition e1 = sr.readNonNullableStruct(StoreStructDefinition.class);
        Assertions.assertEquals(c, e1);
        StoreStructDefinition e2 = sr.readNonNullableStruct(StoreStructDefinition.class);
        Assertions.assertEquals(c, e2);
    }

    @Test
    public void test4() {
        StoreStructDefinition[] c = new StoreStructDefinition[]{getStoreTableDefinition1()};
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        StoreOutputStream sw = new StoreOutputStreamImpl(s, StoreReaderConf.get(1));
        sw.writeNonNullableStruct(StoreStructDefinition[].class, c);
        sw.writeNonNullableStruct(StoreStructDefinition[].class, c);
        sw.flush();

        StoreInputStream sr = new StoreInputStreamImpl(new ByteArrayInputStream(s.toByteArray()), StoreReaderConf.get(1));
        StoreStructDefinition[] e1 = sr.readNonNullableStruct(StoreStructDefinition[].class);
        StoreStructDefinition[] e2 = sr.readNonNullableStruct(StoreStructDefinition[].class);
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
        StoreOutputStream sw = new StoreOutputStreamImpl(s, StoreReaderConf.get(1));
        sw.writeNonNullableStruct(StoreRows.class, r);
        sw.flush();

        StoreInputStream sr = new StoreInputStreamImpl(new ByteArrayInputStream(s.toByteArray()), StoreReaderConf.get(1));
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
                new TableDefinition()
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
        StoreOutputStream sw = new StoreOutputStreamImpl(s, StoreReaderConf.get(1));
        sw.writeNonNullableStruct(StoreRows.class, r);
        sw.flush();
        StoreInputStream sr = new StoreInputStreamImpl(new ByteArrayInputStream(s.toByteArray()), StoreReaderConf.get(1));
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


    private static StoreStructDefinition getStoreTableDefinition1() {
        TableDefinition t = new TableDefinition();
        t.setCatalogName(null);
        t.setSchemaName("schem");
        t.setTableName("tab");
        t.setColumns(new ColumnDefinition[]{
                getStoreColumnDefinition1()
        });
        return t;
    }

    private static ColumnDefinition getStoreColumnDefinition1() {
        ColumnDefinition c = new ColumnDefinition();
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

    private static ColumnDefinition getStoreColumnDefinition2(String name, StoreDataType t) {
        ColumnDefinition c = new ColumnDefinition();
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
