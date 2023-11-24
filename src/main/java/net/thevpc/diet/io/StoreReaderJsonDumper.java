/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.diet.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.thevpc.diet.model.StoreTableDefinition;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vpc
 */
public class StoreReaderJsonDumper {

    private StoreReader r;

    public StoreReaderJsonDumper(StoreReader r) {
        this.r = r;
    }

    public void dump(PrintStream out) {
        r.visit(new StoreVisitor() {
            private Gson gson = new GsonBuilder()
                    .setPrettyPrinting().create();

            @Override
            public void visitSchema(StoreTableDefinition[] md) {
                out.println("read section SECTION_SCHEMA");
                out.println(gson.toJson(md));
            }

            @Override
            public void visitData(StoreRows r) {
                out.println("read section SECTION_DATA");
                StoreTableDefinition md = r.getDefinition();
                out.println(gson.toJson(md));
                IoRow c;
                int colsCount = md.getColumns().length;
                while ((c = r.nextRow()) != null) {
                    List<Object> someRow = new ArrayList<>();
                    for (int j = 0; j < colsCount; j++) {
                        IoCell ioCell = c.nextColumn();
                        System.out.println(ioCell.getMetaData().toString());
                        someRow.add(ioCell.getObject());
                    }
                    out.println(gson.toJson(someRow));
                }
            }

            @Override
            public void visitEnd() {
                out.println("read section SECTION_END");
            }
        });
    }
}
