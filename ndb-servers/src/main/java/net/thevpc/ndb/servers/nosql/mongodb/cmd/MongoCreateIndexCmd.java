package net.thevpc.ndb.servers.nosql.mongodb.cmd;

import net.thevpc.nuts.NOut;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.ndb.servers.ExtendedQuery;
import net.thevpc.ndb.servers.base.cmd.CreateIndexCmd;
import net.thevpc.ndb.servers.nosql.mongodb.NMongoConfig;
import net.thevpc.ndb.servers.nosql.mongodb.NMongoSupport;
import org.bson.Document;

public class MongoCreateIndexCmd extends CreateIndexCmd<NMongoConfig> {
    public MongoCreateIndexCmd(NMongoSupport support) {
        super(support);
    }

    @Override
    public NMongoSupport getSupport() {
        return (NMongoSupport) super.getSupport();
    }

    protected void runCreateIndex(ExtendedQuery eq, NMongoConfig options) {
        getSupport().doWithMongoCollection(options, eq.getTable(), mongoCollection -> {
            Document docSet = Document.parse("{}");
            for (String s : eq.getSet()) {
                if (!NBlankable.isBlank(s)) {
                    docSet.putAll(Document.parse(s));
                }
            }
            NSession session = NSession.get().get();
            NOut.println(mongoCollection.createIndex(docSet));
        });
    }

}
