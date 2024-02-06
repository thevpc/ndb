package net.thevpc.diet.test;

import net.thevpc.diet.cmd.options.DietOptionsParser;
import net.thevpc.diet.cmd.DietRunner;

public class DietTestFleetView {
    public static void main(String[] args) {
        DietRunner.run(DietOptionsParser.parse(
                "import"
                , "--db=postgres://postgres:postgres@/fleetview"
                , "--clear-table"
                , "--data=false"
                , "--debug"
//                , "--file=/home/vpc/work/client-projects/icon/icon-veoni-spring-backend/dump/FleetView.dbo.Course.dump"
                , "--file=/home/vpc/work/client-projects/icon/icon-veoni-spring-backend/dump/*.dump"
        ));

    }
}
