/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.dbrman.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.thevpc.dbrman.io.Out;
import net.thevpc.dbrman.options.DumpToJsonOptions;
import net.thevpc.dbrman.util.DbInfoModuleInstaller;
import net.thevpc.dbrman.util.DbrIoHelper;
import net.thevpc.vio2.api.IoCell;
import net.thevpc.vio2.api.IoRow;
import net.thevpc.vio2.api.StoreRows;
import net.thevpc.vio2.api.StoreVisitor;
import net.thevpc.vio2.impl.AbstractIoCell;
import net.thevpc.vio2.impl.StoreReader;

import net.thevpc.vio2.model.StoreStructDefinition;
import net.thevpc.vio2.util.FileUtils;
import net.thevpc.vio2.util.IOUtils;
import net.thevpc.vio2.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        if (StringUtils.isBlank(file)) {
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
        IOUtils.Creatable<PrintStream> ps = oo.toPrintStream();
        if (ps == null) {
            ps = new IOUtils.Creatable<>(System.out, false);
        }
        try {
            PrintStream out = ps.getValue();
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
                                    Object u = AbstractIoCell.toLobFile(object, parentFile);
                                    if (AbstractIoCell.isLobPointer(u)) {
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
            if (ps.isCreated()) {
                ps.getValue().close();
            }
        }
    }
}
