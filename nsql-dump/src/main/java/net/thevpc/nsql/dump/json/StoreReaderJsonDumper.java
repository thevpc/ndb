/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nsql.dump.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.thevpc.nsql.dump.io.Out;
import net.thevpc.nsql.dump.options.DumpToJsonOptions;
import net.thevpc.nsql.dump.util.DbrIoHelper;
import net.thevpc.nsql.dump.util.FileUtils;
import net.thevpc.nsql.NLobUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NCreated;
import net.thevpc.lib.nserializer.api.IoCell;
import net.thevpc.lib.nserializer.api.IoRow;
import net.thevpc.lib.nserializer.api.StoreRows;
import net.thevpc.lib.nserializer.api.StoreVisitor;
import net.thevpc.lib.nserializer.impl.StoreReader;

import net.thevpc.lib.nserializer.model.StoreStructDefinition;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vpc
 */
public class StoreReaderJsonDumper {
    public StoreReaderJsonDumper() {
    }

    public void dumpFileToJson(DumpToJsonOptions options) {
        DbrIoHelper.check(options.getIn());
        if (options.getIn().getInputStream() != null) {
            dumpFileToJson(options, new StoreReader(options.getIn().getInputStream()), options.getOut());
        } else if (options.getIn().getFile() != null) {
            dumpFileToJson(options, options.getIn().getFile().toString(), options.getOut());
        }
    }

    public void dumpFileToJson(DumpToJsonOptions options, String file, Out out) {
        if (NBlankable.isBlank(file)) {
            file = ".";
        }
        File[] files = FileUtils.expandExistingFiles(file, ".dump");
        if (files.length == 0) {
            throw new IllegalArgumentException("missing files from : " + file);
        }
        for (File f : files) {
            dumpFileToJson(options, new StoreReader(f), out);
        }
    }

    public void dumpFileToJson(DumpToJsonOptions options, StoreReader r, Out oo) {
        if (oo == null) {
            oo = new Out(System.out);
        }
        NCreated<PrintStream> ps = oo.toPrintStream();
        if (ps == null) {
            ps = NCreated.ofExisting(System.out);
        }
        try {
            PrintStream out = ps.get();
            GsonBuilder gsonBuilder = new GsonBuilder();
            if (options.isPretty()) {
                gsonBuilder.setPrettyPrinting();
            }
            Gson gson = gsonBuilder.create();
            r.visit(new StoreVisitor() {

                @Override
                public void visitSchema(List<StoreStructDefinition> md) {
                    if (options.isPrintSeparators()) {
                        out.println("{'**SEPARATOR**':'SECTION_SCHEMA'}");
                    }
                    out.println(gson.toJson(md));
                }

                @Override
                public void visitData(StoreRows r) {
                    try {
                        if (options.isData()) {
                            if (options.isPrintSeparators()) {
                                out.println("{'**SEPARATOR**':'SECTION_DATA'}");
                            }
                            Out o = options.getOut();
                            File parentFile = new File(".");
                            if (options.getLobFolder() != null) {
                                parentFile = options.getLobFolder();
                            } else if (o != null) {
                                if (o.getFile() != null) {
                                    if (o.getFile().getParentFile() != null) {
                                        parentFile = o.getFile().getParentFile();
                                    }
                                }
                            }
                            parentFile.mkdirs();
                            StoreStructDefinition md = r.getDefinition();
                            out.println(gson.toJson(md));
                            IoRow c;
                            while ((c = r.nextRow()) != null) {
                                List<Object> someRow = new ArrayList<>();
                                for (IoCell ioCell : c.getColumns()) {
                                    Object object = ioCell.getObject();
                                    Object u = NLobUtils.toLobFile(object, parentFile.toPath());
                                    if (NLobUtils.isLobPointer(u)) {
                                        someRow.add(u.toString());
                                    } else {
                                        someRow.add(u);
                                    }
                                }
                                out.println(gson.toJson(someRow));
                            }
                        }
                    } catch (UncheckedIOException ex) {
                        throw new UncheckedIOException(
                                "Error loading " + r.getDefinition().toStoreStructId()
                                        + ": " + ex.getMessage(),
                                ex.getCause()
                        );
                    }

                }

                @Override
                public void visitEnd() {
                    if (options.isPrintSeparators()) {
                        out.println("{'**SEPARATOR**':'SECTION_END'}");
                    }
                }
            });
        } finally {
            if (ps.isNew()) {
                ps.get().close();
            }
        }
    }
}
