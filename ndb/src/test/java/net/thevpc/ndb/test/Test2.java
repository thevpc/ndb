package net.thevpc.ndb.test;

import net.thevpc.ndb.cmd.options.NDdbOptions;
import net.thevpc.ndb.cmd.options.NDdbOptionsParser;
import net.thevpc.nsql.NSqlDialect;
import net.thevpc.nuts.cmdline.NCmdLine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Test2 {

    @Test
    public void test1() {
        NDdbOptions z = NDdbOptionsParser.parse(NCmdLine.ofArgs("--db=sqlserver://sa:hoho@localhost/FleetView"));
        Assertions.assertEquals(null,z.cnx.getUrl());
        Assertions.assertEquals(NSqlDialect.MSSQLSERVER,z.cnx.getDialect());
        Assertions.assertEquals("sa",z.cnx.getUsername());
        Assertions.assertEquals("hoho",z.cnx.getPassword());
        Assertions.assertEquals("FleetView",z.cnx.getDbName());
        Assertions.assertEquals("localhost",z.cnx.getHost());
        Assertions.assertEquals(null,z.cnx.getPort());
    }

    @Test
    public void test2() {
        NDdbOptions z = NDdbOptionsParser.parse(NCmdLine.ofArgs("--db=sqlserver://sa:hoho@/FleetView"));
        Assertions.assertEquals(NSqlDialect.MSSQLSERVER,z.cnx.getDialect());
        Assertions.assertEquals("sa",z.cnx.getUsername());
        Assertions.assertEquals("hoho",z.cnx.getPassword());
        Assertions.assertEquals("FleetView",z.cnx.getDbName());
        Assertions.assertEquals("",z.cnx.getHost());
        Assertions.assertEquals(null,z.cnx.getPort());
    }

    @Test
    public void test3() {
        NDdbOptions z = NDdbOptionsParser.parse(NCmdLine.ofArgs("--db=sqlserver:///FleetView"));
        Assertions.assertEquals(NSqlDialect.MSSQLSERVER,z.cnx.getDialect());
        Assertions.assertEquals(null,z.cnx.getUsername());
        Assertions.assertEquals(null,z.cnx.getPassword());
        Assertions.assertEquals("FleetView",z.cnx.getDbName());
        Assertions.assertEquals("",z.cnx.getHost());
        Assertions.assertEquals(null,z.cnx.getPort());
    }


}
