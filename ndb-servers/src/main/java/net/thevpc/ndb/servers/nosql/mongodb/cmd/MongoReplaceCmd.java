package net.thevpc.ndb.servers.nosql.mongodb.cmd;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.ndb.servers.ExtendedQuery;
import net.thevpc.ndb.servers.base.cmd.ReplaceCmd;
import net.thevpc.ndb.servers.nosql.mongodb.NMongoConfig;
import net.thevpc.ndb.servers.nosql.mongodb.NMongoSupport;
import org.bson.Document;

public class MongoReplaceCmd extends ReplaceCmd<NMongoConfig> {
    public MongoReplaceCmd(NMongoSupport support) {
        super(support);
    }

    @Override
    public NMongoSupport getSupport() {
        return (NMongoSupport) super.getSupport();
    }

    protected void runReplace(ExtendedQuery eq, NMongoConfig options) {
        getSupport().doWithMongoCollection(options, eq.getTable(), mongoCollection -> {
            Document docWhere = Document.parse("{}");
            for (String s : eq.getWhere()) {
                if (!NBlankable.isBlank(s)) {
                    docWhere.putAll(Document.parse(s));
                }
            }
            Document docSet = Document.parse("{}");
            for (String s : eq.getSet()) {
                if (!NBlankable.isBlank(s)) {
                    docSet.putAll(Document.parse(s));
                }
            }
            Object z = mongoCollection.findOneAndReplace(
                    docWhere,
                    docSet
            );
            NSession session = NSession.of();
            session.out().println(z);
        });
    }


}
