package net.thevpc.ndb.servers.base.cmd;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.ndb.servers.ExtendedQuery;
import net.thevpc.ndb.servers.NdbConfig;
import net.thevpc.ndb.servers.base.NdbCmd;
import net.thevpc.ndb.servers.base.NdbSupportBase;
import net.thevpc.ndb.servers.sql.nmysql.util.AtName;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NRef;

import java.util.Arrays;

public class CreateIndexCmd<C extends NdbConfig> extends NdbCmd<C> {
    public CreateIndexCmd(NdbSupportBase<C> support, String... names) {
        super(support,"create-index");
        this.names.addAll(Arrays.asList(names));
    }

    public void run(NCmdLine cmdLine) {
        NRef<AtName> name = NRef.ofNull(AtName.class);
        ExtendedQuery eq = new ExtendedQuery(getName());
        C otherOptions = createConfigInstance();

        NRef<String> status = NRef.of("");
        while (cmdLine.hasNext()) {
            switch (status.get()) {
                case "": {
                    switch (cmdLine.peek().get().key()) {
                        case "--config": {
                            readConfigNameOption(cmdLine, name);
                            break;
                        }
                        case "--entity":
                        case "--table":
                        case "--collection": {
                            cmdLine.matcher().matchEntry((v) -> eq.setTable(v.stringValue())).require();
                            break;
                        }
                        case "--one": {
                            cmdLine.matcher().matchFlag((v) -> eq.setOne(v.booleanValue())).anyMatch();
                            break;
                        }
                        case "--set": {
                            cmdLine.matcher().matchFlag((v) -> {
                                status.set("--set");
                            }).require();
                            break;
                        }
                        default: {
                            fillOptionLast(cmdLine, otherOptions);
                        }
                    }
                    break;
                }
                case "--set": {
                    switch (cmdLine.peek().get().key()) {
                        default: {
                            eq.getSet().add(cmdLine.next().get().toString());
                        }
                    }
                    break;
                }
            }
        }
        if (NBlankable.isBlank(eq.getTable())) {
            cmdLine.throwMissingArgument("--table");
        }

        C options = loadFromName(name, otherOptions);
        revalidateOptions(options);
        if (NBlankable.isBlank(otherOptions.getDatabaseName())) {
            cmdLine.throwMissingArgument("--dbname");
        }
        if (NBlankable.isBlank(otherOptions.getDatabaseName())) {
            cmdLine.throwMissingArgument("--dbname");
        }
        runCreateIndex(eq, options);
    }

    protected void runCreateIndex(ExtendedQuery eq, C options) {
        throw new NIllegalArgumentException(NMsg.ofPlain("invalid"));
    }

}
