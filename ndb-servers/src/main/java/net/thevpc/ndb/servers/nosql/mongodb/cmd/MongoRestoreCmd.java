package net.thevpc.ndb.servers.nosql.mongodb.cmd;

import net.thevpc.ndb.servers.base.cmd.RestoreCmd;
import net.thevpc.ndb.servers.nosql.mongodb.NMongoConfig;
import net.thevpc.ndb.servers.nosql.mongodb.NMongoSupport;

public class MongoRestoreCmd extends RestoreCmd<NMongoConfig> {
    public MongoRestoreCmd(NMongoSupport support) {
        super(support);
    }

    @Override
    public NMongoSupport getSupport() {
        return (NMongoSupport) super.getSupport();
    }


}
