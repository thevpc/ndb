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

public class FindCmd<C extends NdbConfig> extends NdbCmd<C> {
    public FindCmd(NdbSupportBase<C> support, String... names) {
        super(support,"find");
        this.names.addAll(Arrays.asList(names));
    }

    @Override
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
                        case "--sort": {
                            status = "--sort";
                            cmdLine.matcher().matchFlag((v) -> {
                            }).anyMatch();
                            break;
                        }
                        case "--limit": {
                            cmdLine.matcher().matchEntry((v) -> eq.setLimit(v.intValue())).anyMatch();
                            break;
                        }
                        case "--skip": {
                            cmdLine.matcher().matchEntry((v) -> eq.setSkip(v.intValue())).anyMatch();
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
                        case "--sort": {
                            status = "--sort";
                            cmdLine.matcher().matchFlag((v) -> {
                            }).anyMatch();
                            break;
                        }
                        default: {
                            eq.getWhere().add(cmdLine.next().get().toString());
                        }
                    }
                    break;
                }
                case "--sort": {
                    switch (cmdLine.peek().get().key()) {
                        case "--where": {
                            status = "--where";
                            cmdLine.matcher().matchFlag((v) -> {
                            }).anyMatch();
                            break;
                        }
                        default: {
                            eq.getSort().add(cmdLine.next().get().toString());
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
        support.revalidateOptions(options);
        if (NBlankable.isBlank(otherOptions.getDatabaseName())) {
            cmdLine.throwMissingArgument("--dbname");
        }
        run(eq, options);
    }

    protected void run(ExtendedQuery eq, C options) {
        throw new NIllegalArgumentException(NMsg.ofPlain("invalid"));
    }


}
