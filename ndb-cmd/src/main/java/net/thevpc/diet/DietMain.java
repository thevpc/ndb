package net.thevpc.diet;

import net.thevpc.diet.cmd.options.DietOptionsParser;
import net.thevpc.diet.cmd.DietRunner;

public class DietMain {
    public static void main(String[] args) {
        DietRunner.run(DietOptionsParser.parse(args));
    }
}
