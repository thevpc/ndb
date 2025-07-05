package net.thevpc.ndb;

import net.thevpc.ndb.cmd.options.NDdbOptionsParser;
import net.thevpc.ndb.cmd.NDdbRunner;
import net.thevpc.nuts.NApp;

@NApp.Info
public class NDdbMain  {
    public static void main(String[] args) {
        NApp.builder(args).run();
    }

    @NApp.Runner
    public void run() {
        NDdbRunner.run(NDdbOptionsParser.parse(NApp.of().getCmdLine()));
    }
}
