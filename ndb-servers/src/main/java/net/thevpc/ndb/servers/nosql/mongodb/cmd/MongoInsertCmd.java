package net.thevpc.ndb.servers.nosql.mongodb.cmd;

import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.ndb.servers.ExtendedQuery;
import net.thevpc.ndb.servers.base.cmd.InsertCmd;
import net.thevpc.ndb.servers.nosql.mongodb.NMongoConfig;
import net.thevpc.ndb.servers.nosql.mongodb.NMongoSupport;
import org.bson.Document;

public class MongoInsertCmd extends InsertCmd<NMongoConfig> {
    public MongoInsertCmd(NMongoSupport support) {
        super(support);
    }

    @Override
    public NMongoSupport getSupport() {
        return (NMongoSupport) super.getSupport();
    }

    @Override
    protected void runInsert(ExtendedQuery eq, NMongoConfig options) {
        getSupport().doWithMongoCollection(options, eq.getTable(), mongoCollection -> {
            Document d = Document.parse("{}");
            for (String s : eq.getSet()) {
                if (!NBlankable.isBlank(s)) {
                    d.putAll(Document.parse(s));
                }
            }
            NOut.println(mongoCollection.insertOne(d));
        });
    }
}
