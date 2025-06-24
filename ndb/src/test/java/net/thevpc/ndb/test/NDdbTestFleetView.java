package net.thevpc.ndb.test;

import net.thevpc.ndb.cmd.options.NDdbOptionsParser;
import net.thevpc.ndb.cmd.NDdbRunner;
import net.thevpc.nuts.cmdline.NCmdLine;

public class NDdbTestFleetView {
    public static void main(String[] args) {
        NDdbRunner.run(NDdbOptionsParser.parse(
                NCmdLine.ofArgs(
                "import"
                , "--db=postgres://postgres:postgres@/fleetview"
                , "--clear-table"
                , "--data=false"
                , "--debug"
//                , "--file=/home/vpc/work/client-projects/icon/icon-veoni-spring-backend/dump/FleetView.dbo.Course.dump"
                , "--file=/home/vpc/work/client-projects/icon/icon-veoni-spring-backend/dump/*.dump"
            )));

    }
}
