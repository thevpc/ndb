package net.thevpc.ndb.test;

import net.thevpc.ndb.cmd.NDdbRunner;
import net.thevpc.ndb.cmd.options.NDdbOptionsParser;

public class NDdbTestExplodedFleetView {
    public static void main(String[] args) {
        NDdbRunner.run(NDdbOptionsParser.parse(
                "dump"
                , "--db=jtds-sqlserver://sa:Veoni2014@41.226.27.228/FleetView"
                , "--debug"
                , "--tables=QRTZ_*"
                , "--exploded"
                , "--file=/home/vpc/trash/db/2023-01-10"
        ));
//        NDdbRunner.run(NDdbOptionsParser.parse(
//                "json"
//                , "--debug"
////                , "--file=/home/vpc/trash/db"
//        ));

    }
}
