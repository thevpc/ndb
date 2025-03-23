package net.thevpc.ndb;

import net.thevpc.ndb.cmd.options.NDdbOptionsParser;
import net.thevpc.ndb.cmd.NDdbRunner;

public class NDdbMain {
    public static void main(String[] args) {
        NDdbRunner.run(NDdbOptionsParser.parse(args));
    }
}
