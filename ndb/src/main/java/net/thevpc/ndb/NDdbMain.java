package net.thevpc.ndb;

import net.thevpc.ndb.cmd.options.NDdbOptionsParser;
import net.thevpc.ndb.cmd.NDdbRunner;
import net.thevpc.nuts.NApp;
import net.thevpc.nuts.NApplication;
import net.thevpc.nuts.NMainArgs;

public class NDdbMain implements NApplication {
    public static void main(String[] args) {
        NApp.builder(args).run();
    }

    @Override
    public void run() {
        NDdbRunner.run(NDdbOptionsParser.parse(NApp.of().getCmdLine()));
    }
}
