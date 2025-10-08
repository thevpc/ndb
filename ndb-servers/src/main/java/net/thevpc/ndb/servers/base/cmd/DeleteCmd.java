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

public class DeleteCmd<C extends NdbConfig> extends NdbCmd<C> {
    public DeleteCmd(NdbSupportBase<C> support, String... names) {
        super(support,"update");
        this.names.addAll(Arrays.asList(names));
    }


    public void run(NCmdLine cmdLine) {
        NRef<AtName> name = NRef.ofNull(AtName.class);
        ExtendedQuery eq = new ExtendedQuery(getName());
        C otherOptions = createConfigInstance();

        String status = "";
        while (cmdLine.hasNext()) {
            switch (status) {
                case "": {
                    switch (cmdLine.peek().get().key()) {
                        case "--config": {
                            readConfigNameOption(cmdLine, name);
                            break;
                        }
                        case "--entity":
                        case "--table":
                        case "--collection": {
                            cmdLine.matcher().matchEntry((v) -> eq.setTable(v.stringValue())).anyMatch();
                            break;
                        }
                        case "--where": {
                            status = "--where";
                            cmdLine.matcher().matchFlag((v) -> {
                            }).anyMatch();
                            break;
                        }
                        case "--one": {
                            cmdLine.matcher().matchFlag((v) -> eq.setOne(v.booleanValue())).anyMatch();
                            break;
                        }
                        default: {
                            fillOptionLast(cmdLine, otherOptions);
                        }
                    }
                    break;
                }
                case "--where": {
                    switch (cmdLine.peek().get().key()) {
                        default: {
                            eq.getWhere().add(cmdLine.next().get().toString());
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
        runDelete(eq, options);
    }


    protected void runDelete(ExtendedQuery eq, C options) {
        throw new NIllegalArgumentException(NMsg.ofPlain("invalid"));
    }

}
