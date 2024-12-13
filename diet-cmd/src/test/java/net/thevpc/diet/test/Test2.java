package net.thevpc.diet.test;

import net.thevpc.diet.cmd.options.DietOptions;
import net.thevpc.diet.cmd.options.DietOptionsParser;
import net.thevpc.nsql.NSqlDialect;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Test2 {

    @Test
    public void test1() {
        DietOptions z = DietOptionsParser.parse("--db=sqlserver://sa:hoho@localhost/FleetView");
        Assertions.assertEquals(null,z.cnx.getUrl());
        Assertions.assertEquals(NSqlDialect.MSSQLSERVER,z.cnx.getType());
        Assertions.assertEquals("sa",z.cnx.getUser());
        Assertions.assertEquals("hoho",z.cnx.getPassword());
        Assertions.assertEquals("FleetView",z.cnx.getDbName());
        Assertions.assertEquals("localhost",z.cnx.getHost());
        Assertions.assertEquals(null,z.cnx.getPort());
    }

    @Test
    public void test2() {
        DietOptions z = DietOptionsParser.parse("--db=sqlserver://sa:hoho@/FleetView");
        Assertions.assertEquals(NSqlDialect.MSSQLSERVER,z.cnx.getType());
        Assertions.assertEquals("sa",z.cnx.getUser());
        Assertions.assertEquals("hoho",z.cnx.getPassword());
        Assertions.assertEquals("FleetView",z.cnx.getDbName());
        Assertions.assertEquals("",z.cnx.getHost());
        Assertions.assertEquals(null,z.cnx.getPort());
    }

    @Test
    public void test3() {
        DietOptions z = DietOptionsParser.parse("--db=sqlserver:///FleetView");
        Assertions.assertEquals(NSqlDialect.MSSQLSERVER,z.cnx.getType());
        Assertions.assertEquals(null,z.cnx.getUser());
        Assertions.assertEquals(null,z.cnx.getPassword());
        Assertions.assertEquals("FleetView",z.cnx.getDbName());
        Assertions.assertEquals("",z.cnx.getHost());
        Assertions.assertEquals(null,z.cnx.getPort());
    }


}
