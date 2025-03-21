package net.thevpc.ndb.servers.nosql.mongodb.cmd;

import net.thevpc.ndb.servers.base.cmd.DumpCmd;
import net.thevpc.ndb.servers.nosql.mongodb.NMongoConfig;
import net.thevpc.ndb.servers.nosql.mongodb.NMongoSupport;

public class MongoDumpCmd extends DumpCmd<NMongoConfig> {
    public MongoDumpCmd(NMongoSupport support) {
        super(support);
    }

    @Override
    public NMongoSupport getSupport() {
        return (NMongoSupport) super.getSupport();
    }
}
