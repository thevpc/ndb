package net.thevpc.ndb.servers.nosql.mongodb.cmd;

import com.mongodb.client.result.DeleteResult;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.ndb.servers.ExtendedQuery;
import net.thevpc.ndb.servers.base.cmd.DeleteCmd;
import net.thevpc.ndb.servers.nosql.mongodb.NMongoConfig;
import net.thevpc.ndb.servers.nosql.mongodb.NMongoSupport;
import org.bson.Document;

public class MongoDeleteCmd extends DeleteCmd<NMongoConfig> {
    public MongoDeleteCmd(NMongoSupport support) {
        super(support);
    }

    @Override
    public NMongoSupport getSupport() {
        return (NMongoSupport) super.getSupport();
    }

    @Override
    protected void runDelete(ExtendedQuery eq, NMongoConfig options) {
        getSupport().doWithMongoCollection(options, eq.getTable(), mongoCollection -> {
            Document docWhere = Document.parse("{}");
            for (String s : eq.getWhere()) {
                if (!NBlankable.isBlank(s)) {
                    docWhere.putAll(Document.parse(s));
                }
            }
            DeleteResult r = eq.getOne() ?
                    mongoCollection.deleteOne(docWhere)
                    : mongoCollection.deleteMany(docWhere);
            NOut.println(r);
        });
    }

}
