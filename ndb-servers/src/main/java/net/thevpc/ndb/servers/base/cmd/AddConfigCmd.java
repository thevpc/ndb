package net.thevpc.ndb.servers.base.cmd;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NElementParser;
import net.thevpc.nuts.elem.NElementWriter;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.ndb.servers.NdbConfig;
import net.thevpc.ndb.servers.base.NdbCmd;
import net.thevpc.ndb.servers.base.NdbSupportBase;
import net.thevpc.ndb.servers.util.NdbUtils;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Arrays;

public class AddConfigCmd<C extends NdbConfig> extends NdbCmd<C> {
    public AddConfigCmd(NdbSupportBase<C> support, String... names) {
        super(support, "add-config");
        this.names.addAll(Arrays.asList(names));
    }

    @Override
    public void run(NCmdLine cmdLine) {
        C options = createConfigInstance();
        NRef<Boolean> update = NRef.of(false);
        NSession session = NSession.of();
        while (cmdLine.hasNext()) {
            if (!fillOption(cmdLine, options)) {
                cmdLine.selector().with("--update").nextFlag((v) -> {
                    update.set(v.booleanValue());
                }).requireWithDefault()
            }
        }
        options.setName(NStringUtils.trimToNull(options.getName()));
        if (NBlankable.isBlank(options.getName())) {
            options.setName("default");
        }

        NPath file = getSharedConfigFolder().resolve(asFullName(options.getName()) + NdbUtils.SERVER_CONFIG_EXT);
        if (file.exists()) {
            if (update.get()) {
                C old = NElementParser.ofJson().setNtf(false).parse(file, support.getConfigClass());
                String oldName = old.getName();
                old.setNonNull(options);
                old.setName(oldName);
                NElementWriter.ofJson().write(options, file);
            } else {
                throw new RuntimeException("already found");
            }
        } else {
            NElementWriter.ofJson().write(options, file);
        }
    }


}
