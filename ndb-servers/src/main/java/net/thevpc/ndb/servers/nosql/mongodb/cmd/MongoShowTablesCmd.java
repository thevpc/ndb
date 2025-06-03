package net.thevpc.ndb.servers.nosql.mongodb.cmd;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementParser;
import net.thevpc.ndb.servers.ExtendedQuery;
import net.thevpc.ndb.servers.base.cmd.ShowTablesCmd;
import net.thevpc.ndb.servers.nosql.mongodb.NMongoConfig;
import net.thevpc.ndb.servers.nosql.mongodb.NMongoSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MongoShowTablesCmd extends ShowTablesCmd<NMongoConfig> {
    public MongoShowTablesCmd(NMongoSupport support) {
        super(support);
    }

    @Override
    public NMongoSupport getSupport() {
        return (NMongoSupport) super.getSupport();
    }

    protected void runShowTables(ExtendedQuery eq, NMongoConfig options) {
        getSupport().doWithMongoClient(options, mongoClient -> {
            getSupport().doWithMongoDB(options, db -> {
                List<NElement> databases = db.listCollections()
                        .into(new ArrayList<>())
                        .stream().map(x -> NElementParser.ofJson().parse(x.toJson(), NElement.class))
                        .map(x->{
                            if(eq.isLongMode()){
                                return x;
                            }
                            return x.asObject().get().get("name").get();
                        })
                        .collect(Collectors.toList());
                NSession session = NSession.get().get();
                session.out().println(databases);
            });
        });
    }

}
