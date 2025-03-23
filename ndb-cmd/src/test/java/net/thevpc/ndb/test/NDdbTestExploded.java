package net.thevpc.ndb.test;

import net.thevpc.ndb.cmd.options.NDdbOptionsParser;
import net.thevpc.ndb.cmd.NDdbRunner;

public class NDdbTestExploded {
    public static void main(String[] args) {
        NDdbRunner.run(NDdbOptionsParser.parse(
                "dump"
                , "--db=postgres://postgres:postgres@/digiclaim_test"
                , "--debug"
                , "--exploded"
                , "--max-rows=10"
//                , "--file=/home/vpc/trash/db"
        ));
//        NDdbRunner.run(NDbOptionsParser.parse(
//                "json"
//                , "--debug"
////                , "--file=/home/vpc/trash/db"
//        ));

    }
}
