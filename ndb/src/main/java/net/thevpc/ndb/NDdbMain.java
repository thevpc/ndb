package net.thevpc.ndb;

import net.thevpc.ndb.cmd.options.NDdbOptionsParser;
import net.thevpc.ndb.cmd.NDdbRunner;
import net.thevpc.nuts.app.NApplication;
import net.thevpc.nuts.app.NApp;
import net.thevpc.nuts.app.NAppRun;

@NApp
public class NDdbMain  {
    public static void main(String[] args) {
        if(System.getProperty("java.util.logging.SimpleFormatter.format")==null) {
            System.setProperty(
                    "java.util.logging.SimpleFormatter.format",
                    "[%1$tF %1$tT] [%4$s] %2$s - %5$s%6$s%n"
            );
        }
        NApplication.builder(args).run();
    }

    @NAppRun
    public void run() {
        NDdbRunner.run(NDdbOptionsParser.parse(NApplication.of().cmdLine()));
    }
}
