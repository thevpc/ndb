package net.thevpc.diet.test;

import net.thevpc.diet.cmd.options.DietOptionsParser;
import net.thevpc.diet.cmd.DietRunner;

public class DietTestExploded {
    public static void main(String[] args) {
        DietRunner.run(DietOptionsParser.parse(
                "dump"
                , "--db=postgres://postgres:postgres@/digiclaim_test"
                , "--debug"
                , "--exploded"
                , "--max-rows=10"
                , "--file=/home/vpc/trash/db"
        ));
        DietRunner.run(DietOptionsParser.parse(
                "json"
                , "--debug"
                , "--file=/home/vpc/trash/db"
        ));

    }
}
