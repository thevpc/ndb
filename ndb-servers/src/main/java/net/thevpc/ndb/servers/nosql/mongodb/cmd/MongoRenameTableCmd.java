package net.thevpc.ndb.servers.nosql.mongodb.cmd;

import com.mongodb.MongoNamespace;
import net.thevpc.nuts.core.NSession;
import net.thevpc.ndb.servers.ExtendedQuery;
import net.thevpc.ndb.servers.base.cmd.RenameTableCmd;
import net.thevpc.ndb.servers.nosql.mongodb.NMongoConfig;
import net.thevpc.ndb.servers.nosql.mongodb.NMongoSupport;

public class MongoRenameTableCmd extends RenameTableCmd<NMongoConfig> {
    public MongoRenameTableCmd(NMongoSupport support) {
        super(support);
    }

    @Override
    public NMongoSupport getSupport() {
        return (NMongoSupport) super.getSupport();
    }


    @Override
    protected void runRenameTable(ExtendedQuery eq, NMongoConfig options) {
        getSupport().doWithMongoCollection(options, eq.getTable(), mongoCollection -> {
            String dbn = options.getDatabaseName();
            mongoCollection.renameCollection(new MongoNamespace(dbn, eq.getNewName()));
            NSession session = NSession.of();
            session.out().println(true);
        });
    }


}
