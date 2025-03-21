package net.thevpc.diet.test;

import net.thevpc.diet.cmd.DietRunner;
import net.thevpc.diet.cmd.options.DietOptionsParser;

public class DietTestExplodedFleetView {
    public static void main(String[] args) {
        DietRunner.run(DietOptionsParser.parse(
                "dump"
                , "--db=jtds-sqlserver://sa:Veoni2014@41.226.27.228/FleetView"
                , "--debug"
                , "--tables=QRTZ_*"
                , "--exploded"
                , "--file=/home/vpc/trash/db/2023-01-10"
        ));
//        DietRunner.run(DietOptionsParser.parse(
//                "json"
//                , "--debug"
////                , "--file=/home/vpc/trash/db"
//        ));

    }
}
