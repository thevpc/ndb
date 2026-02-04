package net.thevpc.ndb.test;

import net.thevpc.ndb.cmd.options.NDdbOptionsParser;
import net.thevpc.ndb.cmd.NDdbRunner;
import net.thevpc.nuts.cmdline.NCmdLine;

public class NDdbTestSimple {
    public static void main(String[] args) {
        NDdbRunner.run(NDdbOptionsParser.parse(
                NCmdLine.ofArgs(
                        "db2json"
                        , "--db=postgres://postgres:postgres@/digiclaim_test"
                        , "--max-rows=1"
                        , "--debug"
                        , "--table=accident_photo"
                )
        ));
//        NDdbRunner.run(NDdbOptionsParser.parse(
//                "dump"
//                , "--db=postgres://postgres:postgres@/digiclaim_test"
//                , "--max-rows=1"
//                , "--debug"
//                , "--table=accident_photo"
//                , "--file="+System.getProperty("user.home")+"/trash/test-simple.db"
//        ));
//        NDdbRunner.run(NDdbOptionsParser.parse(
//                "json"
//                , "--debug"
//                , "--file="+System.getProperty("user.home")+"/trash/test-simple.db"
//        ));

    }
}
