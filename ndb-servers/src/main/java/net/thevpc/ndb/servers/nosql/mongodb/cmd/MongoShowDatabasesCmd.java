package net.thevpc.ndb.servers.nosql.mongodb.cmd;

import net.thevpc.nuts.NOut;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementParser;
import net.thevpc.ndb.servers.ExtendedQuery;
import net.thevpc.ndb.servers.base.cmd.ShowDatabasesCmd;
import net.thevpc.ndb.servers.nosql.mongodb.NMongoConfig;
import net.thevpc.ndb.servers.nosql.mongodb.NMongoSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MongoShowDatabasesCmd extends ShowDatabasesCmd<NMongoConfig> {
    public MongoShowDatabasesCmd(NMongoSupport support) {
        super(support);
    }

    @Override
    public NMongoSupport getSupport() {
        return (NMongoSupport) super.getSupport();
    }

    protected void runShowDatabases(ExtendedQuery eq, NMongoConfig options) {
        getSupport().doWithMongoClient(options, mongoClient -> {
            List<NElement> databases = mongoClient.listDatabases()
                    .into(new ArrayList<>())
                    .stream().map(x -> NElementParser.ofJson().parse(x.toJson(), NElement.class))
                    .map(x->{
                        if(eq.isLongMode()){
                            return x;
                        }
                        return x.asObject().get().get("name").get();
                    })
                    .collect(Collectors.toList());
            NOut.println(databases);
        });
    }

}
