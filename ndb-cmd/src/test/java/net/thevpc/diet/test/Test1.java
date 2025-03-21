package net.thevpc.diet.test;

import net.thevpc.lib.nserializer.api.*;
import net.thevpc.nsql.dump.model.TableDefinitionAsStoreStructDefinition;
import net.thevpc.nsql.NSqlColumn;
import net.thevpc.nsql.NSqlColumnType;
import net.thevpc.nsql.model.NSqlTableDefinition;
import net.thevpc.nsql.model.YesNo;
import net.thevpc.lib.nserializer.impl.DefaultStoreRows;
import net.thevpc.lib.nserializer.impl.StoreInputStreamImpl;
import net.thevpc.lib.nserializer.impl.StoreOutputStreamImpl;
import net.thevpc.lib.nserializer.impl.StoreReaderConf;

import net.thevpc.lib.nserializer.model.StoreStructDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class Test1 {

    @Test
    public void test1() {
        NSqlColumn c = getStoreSqlColumn1();
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        StoreOutputStream sw = new StoreOutputStreamImpl(s, StoreReaderConf.get(1));
        sw.writeNonNullableStruct(NSqlColumn.class, c);
        sw.writeNonNullableStruct(NSqlColumn.class, c);
        sw.flush();

        StoreInputStream sr = new StoreInputStreamImpl(new ByteArrayInputStream(s.toByteArray()), StoreReaderConf.get(1));
        NSqlColumn e1 = sr.readNonNullableStruct(NSqlColumn.class);
        NSqlColumn e2 = sr.readNonNullableStruct(NSqlColumn.class);
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
            for (IoCell ioCell : rr.getColumns()) {
                System.out.print(ioCell.getObject());
            }
            System.out.println();
        }
    }

    @Test
    public void test6() {
        NSqlTableDefinition tableDefinition = new NSqlTableDefinition()
                .setCatalogName(null)
                .setSchemaName("schem")
                .setTableName("tab")
                .setColumns(
                        getStoreSqlColumn2("val1", NSqlColumnType.BLOB),
                        getStoreSqlColumn2("val2", NSqlColumnType.BLOB)
                );
        StoreRows r = new DefaultStoreRows(
                new TableDefinitionAsStoreStructDefinition(tableDefinition),
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
        try(StoreRows e1 = sr.readNonNullableStruct(StoreRows.class)) {

        }
//        Assertions.assertEquals(r.getDefinition(), e1.getDefinition());
//        for (IoRow rr : e1.rowsIterable()) {
//            for (IoCell ioCell : rr.columnsIterable()) {
//                System.out.print(ioCell.getObject());
//            }
//            System.out.println();
//        }
    }


    private static StoreStructDefinition getStoreTableDefinition1() {
        NSqlTableDefinition t = new NSqlTableDefinition();
        t.setCatalogName(null);
        t.setSchemaName("schem");
        t.setTableName("tab");
        t.setColumns(new NSqlColumn[]{
                getStoreSqlColumn1()
        });
        return new TableDefinitionAsStoreStructDefinition(t);
    }

    private static NSqlColumn getStoreSqlColumn1() {
        NSqlColumn c = new NSqlColumn();
        c.setIndex(1);
        c.setColumnName("a");
        c.setLabel("b");
        c.setColumnType(NSqlColumnType.STRING);
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
        c.setGeneratedColumn(YesNo.NO);
        return c;
    }

    private static NSqlColumn getStoreSqlColumn2(String name, NSqlColumnType t) {
        NSqlColumn c = new NSqlColumn();
        c.setIndex(1);
        c.setColumnName(name);
        c.setLabel("b");
        c.setColumnType(t);
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
        c.setGeneratedColumn(YesNo.NO);
        return c;
    }
}
