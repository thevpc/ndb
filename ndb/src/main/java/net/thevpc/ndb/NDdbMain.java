package net.thevpc.ndb;

import net.thevpc.ndb.cmd.options.NDdbOptionsParser;
import net.thevpc.ndb.cmd.NDdbRunner;
import net.thevpc.nuts.NApp;
import net.thevpc.nuts.NAppDefinition;
import net.thevpc.nuts.NAppRunner;

@NAppDefinition
public class NDdbMain  {
    public static void main(String[] args) {
        NApp.builder(args).run();
    }

    @NAppRunner
    public void run() {
        NDdbRunner.run(NDdbOptionsParser.parse(NApp.of().getCmdLine()));
    }
}
