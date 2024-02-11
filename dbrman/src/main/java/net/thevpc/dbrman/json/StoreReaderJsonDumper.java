/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.dbrman.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.thevpc.dbrman.options.DumpToJsonOptions;
import net.thevpc.dbrman.util.DbrIoHelper;
import net.thevpc.vio2.api.IoCell;
import net.thevpc.vio2.api.IoRow;
import net.thevpc.vio2.api.StoreRows;
import net.thevpc.vio2.api.StoreVisitor;
import net.thevpc.vio2.impl.StoreReader;

import net.thevpc.vio2.model.StoreStructDefinition;
import net.thevpc.vio2.util.FileUtils;
import net.thevpc.vio2.util.StringUtils;

import java.io.File;
import java.io.PrintStream;
import java.io.UncheckedIOException;
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
        if(options.getIn().getInputStream()!=null){
            dumpFileToJson(options,new StoreReader(options.getIn().getInputStream()), options.getOut());
        }else if(options.getIn().getFile()!=null){
            dumpFileToJson(options,options.getIn().getFile().toString(), options.getOut());
        }
    }

    public void dumpFileToJson(DumpToJsonOptions options,String file, PrintStream out) {
        if(StringUtils.isBlank(file)){
            file=".";
        }
        File[] files = FileUtils.expandExistingFiles(file,".dump");
        if(files.length==0){
            throw new IllegalArgumentException("missing files from : "+file);
        }
        for (File f : files) {
            dumpFileToJson(options,new StoreReader(f),out);
        }
    }

    public void dumpFileToJson(DumpToJsonOptions options,StoreReader r, PrintStream out0) {
        if(out0==null){
            out0=System.out;
        }
        PrintStream out=out0;
        r.visit(new StoreVisitor() {
            private Gson gson;
            {
                GsonBuilder gsonBuilder = new GsonBuilder();
                if(options.isPretty()){
                    gsonBuilder.setPrettyPrinting();
                }
                gson = gsonBuilder.create();
            }

            @Override
            public void visitSchema(List<StoreStructDefinition> md) {
                if(options.isPrintSeparators()) {
                    out.println("{'**SEPARATOR**':'SECTION_SCHEMA'}");
                }
                out.println(gson.toJson(md));
            }

            @Override
            public void visitData(StoreRows r) {
                try {
                    if (options.isData()) {
                        if (true/*accepted*/) {
                            if(options.isPrintSeparators()) {
                                out.println("{'**SEPARATOR**':'SECTION_DATA'}");
                            }
                            StoreStructDefinition md = r.getDefinition();
                            out.println(gson.toJson(md));
                            IoRow c;
                            int colsCount = md.getColumns().size();
                            while ((c = r.nextRow()) != null) {
                                List<Object> someRow = new ArrayList<>();
                                for (int j = 0; j < colsCount; j++) {
                                    IoCell ioCell = c.nextColumn();
                                    System.out.println(ioCell.getDefinition().toString());
                                    someRow.add(ioCell.getObject());
                                }
                                out.println(gson.toJson(someRow));
                            }
                            return;
                        }
                    }
                    r.consume();
                }catch (UncheckedIOException ex){
                    throw new UncheckedIOException(
                            "Error loading "+r.getDefinition().toStoreStructId()
                                    +": "+ex.getMessage(),
                            ex.getCause()
                    );
                }

            }

            @Override
            public void visitEnd() {
                if(options.isPrintSeparators()) {
                    out.println("{'**SEPARATOR**':'SECTION_END'}");
                }
            }
        });
    }
}
