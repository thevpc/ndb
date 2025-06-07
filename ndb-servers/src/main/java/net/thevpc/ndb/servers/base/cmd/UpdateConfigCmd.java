package net.thevpc.ndb.servers.base.cmd;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NElementParser;
import net.thevpc.nuts.elem.NElementWriter;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.ndb.servers.NdbConfig;
import net.thevpc.ndb.servers.base.NdbCmd;
import net.thevpc.ndb.servers.base.NdbSupportBase;
import net.thevpc.ndb.servers.util.NdbUtils;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Arrays;

public class UpdateConfigCmd<C extends NdbConfig> extends NdbCmd<C> {
    public UpdateConfigCmd(NdbSupportBase<C> support, String... names) {
        super(support,"update-config");
        this.names.addAll(Arrays.asList(names));
    }

    @Override
    public void run(NCmdLine cmdLine) {
        C options = createConfigInstance();
        NSession session = NSession.of();
        while (cmdLine.hasNext()) {
            if (fillOption(cmdLine, options)) {
                //
            } else if (session.configureFirst(cmdLine)) {

            } else {
                cmdLine.throwUnexpectedArgument();
            }
        }
        options.setName(NStringUtils.trimToNull(options.getName()));
        if (NBlankable.isBlank(options.getName())) {
            options.setName("default");
        }

        NPath file = getSharedConfigFolder().resolve(asFullName(options.getName()) + NdbUtils.SERVER_CONFIG_EXT);
        if (!file.exists()) {
            throw new RuntimeException("not found");
        }
        C old = NElementParser.ofJson().parse(file, getConfigClass());
        String oldName = old.getName();
        old.setNonNull(options);
        old.setName(oldName);
        NElementWriter.ofJson().write(options,file);
    }

}
