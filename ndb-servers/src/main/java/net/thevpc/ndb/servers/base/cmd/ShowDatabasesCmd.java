package net.thevpc.ndb.servers.base.cmd;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.ndb.servers.ExtendedQuery;
import net.thevpc.ndb.servers.NdbConfig;
import net.thevpc.ndb.servers.base.NdbCmd;
import net.thevpc.ndb.servers.base.NdbSupportBase;
import net.thevpc.ndb.servers.sql.nmysql.util.AtName;
import net.thevpc.nuts.util.NRef;

import java.util.Arrays;

public class ShowDatabasesCmd<C extends NdbConfig> extends NdbCmd<C> {
    public ShowDatabasesCmd(NdbSupportBase<C> support, String... names) {
        super(support,"show-dbs","show-db","show-databases","databases","db");
        this.names.addAll(Arrays.asList(names));
    }


    public void run(NCmdLine cmdLine) {
        NRef<AtName> name = NRef.ofNull(AtName.class);
        C otherOptions = createConfigInstance();
        ExtendedQuery eq = new ExtendedQuery(getName());
        while (cmdLine.hasNext()) {
            if (cmdLine.isNextOption()) {
                switch (cmdLine.peek().get().key()) {
                    case "--config": {
                        readConfigNameOption(cmdLine, name);
                        break;
                    }
                    case "--long": {
                        cmdLine.withNextFlag((v)-> eq.setLongMode(v.booleanValue()));
                        break;
                    }
                    default: {
                        fillOptionLast(cmdLine, otherOptions);
                    }
                }
            } else {
                cmdLine.throwUnexpectedArgument();
            }
        }

        C options = loadFromName(name, otherOptions);
        revalidateOptions(options);
        runShowDatabases(eq, options);
    }

    protected void runShowDatabases(ExtendedQuery eq, C options) {
        throw new NIllegalArgumentException(NMsg.ofPlain("invalid"));
    }


}
