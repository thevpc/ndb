/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.vio2.impl;

import net.thevpc.vio2.api.*;
import net.thevpc.vio2.model.StoreStructDefinition;
import net.thevpc.vio2.model.StoreStructHeader;
import net.thevpc.vio2.model.StoreStructId;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

/**
 * @author vpc
 */
public class StoreWriterImpl extends AbstractStoreWriter {
    public static Logger LOG = Logger.getLogger(StoreWriterImpl.class.getName());

    private StoreOutputStream dos;
    private Sers sers;
    private StoreWriterModel db;
    private boolean closeOut = false;
    private OutputStream out0;

    public StoreWriterImpl(OutputStream out, StoreWriterModel db, long version) {
        this.db = db;
        this.out0 = out;
        this.sers = StoreReaderConf.get(version);
    }


    public StoreWriterImpl(File out, StoreWriterModel db, long version) {
        this.closeOut = true;
        this.db = db;
        this.sers = StoreReaderConf.get(version);
        try {
            LOG.log(Level.FINE, "writing to {0} ...", out);
            this.out0 = new FileOutputStream(out);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }


    private void writeHeader() {
        try {
            boolean compress = isCompress();
            LOG.log(Level.FINE, "write header (compress=" + compress + ")");
            StoreOutputStream hos = new StoreOutputStreamImpl(out0, sers);
            hos.writeNonNullableLong(DietProtocol.BURST);
            hos.writeNonNullableLong(DietProtocol.V1);
            hos.writeNonNullableLong(System.currentTimeMillis());
            hos.writeNonNullableBoolean(compress);
            if (compress) {
                hos.writeUTF("gzip");
            }
            hos.flush();
            if (compress) {
                this.dos = new StoreOutputStreamImpl(new GZIPOutputStream(out0), sers);
            } else {
                this.dos = new StoreOutputStreamImpl(out0, sers);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public StoreWriter write() {
        long maxProgress = 0;
        long[] currProgress = {0};
        writeHeader();
        LOG.log(Level.FINE, "[SECTION_SCHEMA] write section schema");
        this.startSection(DietProtocol.SECTION_SCHEMA);
        List<StoreStructDefinition> tablesMd = new ArrayList<>();
        for (StoreStructId table : getStructs()) {
            StoreStructDefinition definition = db.getDefinition(table);
            if (definition == null) {
                throw new IllegalArgumentException("unable to resolve " + table);
            }
            tablesMd.add(definition);
        }
        maxProgress = (isData() ? (1 + tablesMd.size()) : 0) + 3;
        incProgress(currProgress, maxProgress, "Start");
        LOG.log(Level.FINE, "[SECTION_SCHEMA] write schema for " + tablesMd.size() + " tables");
        IOLogger.current().log("[SECTION_SCHEMA] write schema for " + tablesMd.size() + " tables");
        for (StoreStructDefinition storeTableDefinition : tablesMd) {
            LOG.log(Level.FINE, "[SECTION_SCHEMA] " + storeTableDefinition.toStoreStructId().getFullName() + " (" + storeTableDefinition.getColumns().size() + " columns)");
            IOLogger.current().log("[SECTION_SCHEMA] " + storeTableDefinition.toStoreStructId().getFullName() + " (" + storeTableDefinition.getColumns().size() + " columns)");
        }
        incProgress(currProgress, maxProgress, "Write Definitions");
        dos.writeNonNullableStruct(StoreStructDefinition[].class, tablesMd.toArray(new StoreStructDefinition[0]));
        if (isData()) {
            incProgress(currProgress, maxProgress, "Write Data");
            for (StoreStructDefinition tableMd : tablesMd) {
                this.startSection(DietProtocol.SECTION_DATA);
                LOG.log(Level.FINE, "[" + tableMd.toStoreStructId().getFullName() + "] start section data (limit " + getMaxRows() + ")");
                IOLogger.current().log("[" + tableMd.toStoreStructId().getFullName() + "] start section data (limit " + getMaxRows() + ")");
                try (StoreRows rs = db.getRows(tableMd.toStoreStructId())) {
                    dos.writeNonNullableStruct(StoreRows.class, rs.limit(getMaxRows()));
                }
                incProgress(currProgress, maxProgress, "Write Data for " + tableMd.toStoreStructId().getFullName());
            }
        }
        this.startSection(DietProtocol.STORE_END);
        this.flush();
        incProgress(currProgress, maxProgress, "End");
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
