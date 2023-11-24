package net.thevpc.diet.test;

import net.thevpc.diet.cmd.options.DietOptionsParser;
import net.thevpc.diet.cmd.DietRunner;

public class DietTestSimple {
    public static void main(String[] args) {
        DietRunner.run(DietOptionsParser.parse(
                "dump"
                , "--db=postgres://postgres:postgres@/digiclaim_test"
                , "--max-rows=1"
                , "--debug"
                , "--table=accident_photo"
                , "--file=/home/vpc/trash/test-simple.db"
        ));
        DietRunner.run(DietOptionsParser.parse(
                "json"
                , "--debug"
                , "--file=/home/vpc/trash/test-simple.db"
        ));

    }
}
