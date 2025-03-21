/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nsql.dump.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.thevpc.lib.nserializer.model.StoreDataType;
import net.thevpc.lib.nserializer.model.StoreStructDefinition;
import net.thevpc.lib.nserializer.model.StoreValue;
import net.thevpc.nsql.dump.api.NSqlDump;
import net.thevpc.nsql.dump.model.TableDefinitionAsStoreStructDefinition;
import net.thevpc.nsql.dump.model.TableHeaderAsStoreStructHeader;
import net.thevpc.nsql.dump.model.TableIdAsStoreStructId;
import net.thevpc.nsql.model.NSqlTableDefinition;
import net.thevpc.nsql.model.NSqlTableHeader;
import net.thevpc.lib.nserializer.api.IoRow;
import net.thevpc.lib.nserializer.api.StoreRows;
import net.thevpc.lib.nserializer.api.StoreWriter;
import net.thevpc.lib.nserializer.impl.AbstractStoreWriter;


import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author vpc
 */
public class JsonStoreWriter extends AbstractStoreWriter {
    public static Logger LOG = Logger.getLogger(JsonStoreWriter.class.getName());

    private NSqlDump db;
    private boolean closeOut = false;
    private OutputStream out0;
    private PrintStream out;

    public JsonStoreWriter(OutputStream out, NSqlDump db) {
        this.db = db;
        this.out0 = out;
        this.out = new PrintStream(out0);
    }

    public JsonStoreWriter(File out, NSqlDump db) {
        this.closeOut = true;
        this.db = db;
        try {
            this.out0 = new FileOutputStream(out);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
        this.out = new PrintStream(out0);
    }


    private void writeHeader() {
    }

    public StoreWriter write() {
        Gson multiLineGson = new GsonBuilder().setPrettyPrinting().create();
        Gson compactLineGson = new GsonBuilder().create();
        writeHeader();
        this.startSection("SCHEMA", null, compactLineGson);
        List<StoreStructDefinition> tablesMd = new ArrayList<>();
        for (NSqlTableHeader table : db.getConnection().getAnyTables().stream().filter(x -> "TABLE".equals(x.getTableType()))
                .filter(x -> getStructs().contains(new TableHeaderAsStoreStructHeader(x).toStructId()))
                .collect(Collectors.toList())) {
            NSqlTableDefinition md = db.getConnection().getTableDefinition(table.toTableId());
            tablesMd.add(new TableDefinitionAsStoreStructDefinition(md));
        }
        out.println(multiLineGson.toJson(tablesMd));
        if (isData()) {
            for (StoreStructDefinition tableMd : tablesMd) {
                TableIdAsStoreStructId storeStructId = (TableIdAsStoreStructId) tableMd.toStoreStructId();
                this.startSection("DATA", storeStructId.getFullName(), compactLineGson);
                long index = 0;
                try (StoreRows rs = db.getStoreRows(storeStructId.getTableId())) {
                    StoreRows sr = rs.limit(getMaxRows());
                    for (IoRow r : sr.rowsIterable()) {
                        out.println(compactLineGson.toJson(new NumberedRow(index++, r)));
                    }
                }
            }
        }
        this.startSection("STORE_END",null,compactLineGson);
        this.flush();
        return this;
    }

    private Object simpleCell(StoreValue value) {
        SimpleCell u = new SimpleCell(value);
        if(u.type== StoreDataType.NULL){
            return null;// "null";
        }
        Map<String,Object> v=new HashMap<>();
        v.put(u.type.name().toLowerCase(),u.value);
        return v;
    }

    private class SimpleCell {
        private StoreDataType type;
        private Object value;

        public SimpleCell(StoreValue value) {
            this.type = value.getType();
            this.value = value.getValue();
            if (this.value instanceof InputStream) {
                this.value = "<...InputStream...>";
            } else if (this.value instanceof Reader) {
                this.value = "<...Reader...>";
            }
        }

        public StoreDataType getType() {
            return type;
        }

        public Object getValue() {
            return value;
        }
    }

    private class NumberedRow {
        long index;
        List<Object> columns;

        public NumberedRow(long index, IoRow sr) {
            this.index = index;
            this.columns = Arrays.stream(sr.getColumns()).map(x ->
                    simpleCell(x.repeatable().getValue())
            ).collect(Collectors.toList());
        }
    }

    private void startSection(String sectionId, String name, Gson gson) {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        m.put("section", sectionId);
        if (name != null) {
            m.put("name", name);

        }
        out.println(gson.toJson(m));
    }


    @Override
    public void close() {
        try {
            this.flush();
            if (closeOut) {
                this.out0.close();
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public StoreWriter flush() {
        try {
            this.out.flush();
            this.out0.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }
}
