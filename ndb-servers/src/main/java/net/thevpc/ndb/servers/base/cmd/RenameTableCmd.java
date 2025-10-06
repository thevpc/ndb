package net.thevpc.ndb.servers.base.cmd;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.ndb.servers.ExtendedQuery;
import net.thevpc.ndb.servers.NdbConfig;
import net.thevpc.ndb.servers.base.NdbCmd;
import net.thevpc.ndb.servers.base.NdbSupportBase;
import net.thevpc.ndb.servers.sql.nmysql.util.AtName;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NRef;

import java.util.Arrays;

public class RenameTableCmd<C extends NdbConfig> extends NdbCmd<C> {
    public RenameTableCmd(NdbSupportBase<C> support, String... names) {
        super(support,"rename-table","rename-collection","rename-entity");
        this.names.addAll(Arrays.asList(names));
    }

    @Override
    public void run(NCmdLine cmdLine) {
        NRef<AtName> name = NRef.ofNull(AtName.class);
        C otherOptions = createConfigInstance();
        ExtendedQuery eq = new ExtendedQuery(getName());
        NRef<String> table = NRef.ofNull();
        while (cmdLine.hasNext()) {
            NArg arg = cmdLine.peek().get();
            switch (arg.key()) {
                case "--config": {
                    readConfigNameOption(cmdLine, name);
                    break;
                }
                case "--entity":
                case "--table":
                case "--collection": {
                    cmdLine.matcher().matchEntry((v) -> table.set(v.stringValue())).anyMatch();
                    break;
                }
                default: {
                    if (arg.isOption()) {
                        fillOptionLast(cmdLine, otherOptions);
                    } else {
                        eq.setNewName(arg.toString());
                    }
                }
            }
        }

        if (table.isBlank()) {
            cmdLine.throwMissingArgument("--entity");
        }
        if (NBlankable.isBlank(otherOptions.getDatabaseName())) {
            cmdLine.throwMissingArgument("--dbname");
        }
        C options = loadFromName(name, otherOptions);
        getSupport().revalidateOptions(options);
        if (NBlankable.isBlank(otherOptions.getDatabaseName())) {
            cmdLine.throwMissingArgument("--dbname");
        }
        runRenameTable(eq, options);
    }



    protected void runRenameTable(ExtendedQuery eq, C options) {
        throw new NIllegalArgumentException(NMsg.ofPlain("invalid"));
    }

}
