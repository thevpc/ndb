/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.diet.io.v1;

import net.thevpc.diet.io.*;
import net.thevpc.diet.sql.DatabaseDriver;
import net.thevpc.diet.model.StoreTableDefinition;
import net.thevpc.diet.model.TableHeader;
import net.thevpc.diet.model.TableId;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

/**
 * @author vpc
 */
public class StoreWriterV1 implements StoreWriter {
    public static Logger LOG = Logger.getLogger(StoreWriterV1.class.getName());

    private StoreOutputStream dos;
    private Sers sers = new SersV1();
    private DatabaseDriver db;
    private LinkedHashSet<TableId> tables = new LinkedHashSet<>();
    private boolean compress = false;
    private boolean data = true;
    private long maxRows = -1;
    private boolean closeOut = false;
    private OutputStream out0;

    public StoreWriterV1(OutputStream out, DatabaseDriver db) {
        this.db = db;
        this.out0 = out;
    }

    public StoreWriterV1(File out, DatabaseDriver db) {
        this.closeOut = true;
        this.db = db;
        try {
            this.out0 = new FileOutputStream(out);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean isCompress() {
        return compress;
    }

    public StoreWriterV1 setCompress(boolean compress) {
        this.compress = compress;
        return this;
    }

    public boolean isData() {
        return data;
    }

    public StoreWriter setData(boolean data) {
        this.data = data;
        return this;
    }

    public long getMaxRows() {
        return maxRows;
    }

    public StoreWriterV1 setMaxRows(long maxRows) {
        this.maxRows = maxRows;
        return this;
    }

    public StoreWriter addTables(Predicate<TableHeader> pred) {
        for (TableHeader table : db.getTables().stream()
                .filter(x -> "TABLE".equals(x.getTableType()))
                .filter(x -> {
                    if (pred == null) {
                        return true;
                    }
                    return pred.test(x);
                })
                .collect(Collectors.toList())
        ) {
            tables.add(table.toTableId());
        }
        return this;
    }

    private void writeHeader() {
        try {
            LOG.log(Level.FINE, "write header (compress=" + compress + ")");
            StoreOutputStream hos = new StoreOutputStream(out0, sers);
            hos.writeNonNullableLong(DietProtocol.BURST);
            hos.writeNonNullableLong(DietProtocol.V1);
            hos.writeNonNullableLong(System.currentTimeMillis());
            hos.writeNonNullableBoolean(compress);
            if (compress) {
                hos.writeUTF("gzip");
            }
            hos.flush();
            if (compress) {
                this.dos = new StoreOutputStream(new GZIPOutputStream(out0), sers);
            } else {
                this.dos = new StoreOutputStream(out0, sers);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public StoreWriter write() {
        writeHeader();
        LOG.log(Level.FINE, "[SECTION_SCHEMA] write section schema");
        this.startSection(DietProtocol.SECTION_SCHEMA);
        List<StoreTableDefinition> tablesMd = new ArrayList<>();
        for (TableHeader table : db.getTables().stream().filter(x -> "TABLE".equals(x.getTableType()))
                .filter(x -> tables.contains(x.toTableId()))
                .collect(Collectors.toList())) {
            StoreTableDefinition md = db.getTableMetaData(table.toTableId());
            tablesMd.add(md);
        }
        LOG.log(Level.FINE, "[SECTION_SCHEMA] write schema for " + tablesMd.size() + " tables");
        for (StoreTableDefinition storeTableDefinition : tablesMd) {
            LOG.log(Level.FINE, "[SECTION_SCHEMA] " + storeTableDefinition.getTableName() + " (" + storeTableDefinition.getColumns().length + " columns)");
        }
        dos.writeNonNullableStruct(StoreTableDefinition[].class, tablesMd.toArray(new StoreTableDefinition[0]));
        if (data) {
            for (StoreTableDefinition tableMd : tablesMd) {
                this.startSection(DietProtocol.SECTION_DATA);
                LOG.log(Level.FINE, "["+tableMd.toTableId().toStringId()+"] start section data (limit " + maxRows + ")");
                try (StoreRows rs = db.getTableRows(tableMd.toTableId())) {
                    dos.writeNonNullableStruct(StoreRows.class, rs.limit(maxRows));
                }
            }
        }
        this.startSection(DietProtocol.STORE_END);
        this.flush();
        return this;
    }

    private void startSection(int sectionId) {
        dos.writeNonNullableInt(sectionId);
    }


    @Override
    public void close() {
        try {
            this.flush();
            this.dos.close();
            if (closeOut) {
                this.out0.close();
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public StoreWriter flush() {
        this.dos.flush();
        return this;
    }
}
