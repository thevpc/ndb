package net.thevpc.diet.desktop.panels;

import net.thevpc.diet.desktop.DietInfo;
import net.thevpc.nsql.dump.api.NSqlDump;
import net.thevpc.nsql.CnxInfo;
import net.thevpc.nsql.SqlDialect;
import net.thevpc.nsql.dump.model.DbStore;
import net.thevpc.nsql.dump.model.DbStoreWriter;
import net.thevpc.nsql.dump.model.TableIdAsStoreStructId;
import net.thevpc.nsql.model.TableId;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.violin.api.StoreWriter;
import net.thevpc.violin.api.StoreProgressMonitor;
import net.thevpc.diet.desktop.util.GBC;
import net.thevpc.diet.desktop.util.UI;
import net.thevpc.violin.impl.IOLogger;
import net.thevpc.violin.model.StoreStructId;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

public class DietExportPanel extends JPanel {
    JButton startButton = new JButton("Commencer...");
    JRadioButton oneFile = new JRadioButton("One File Per DB");
    JRadioButton exploded = new JRadioButton("One File Per Table");
    Properties conf;
    boolean workInProgress;
    File outFile;
    boolean optionCompress = true;
    boolean optionData = true;
    long optionMaxRows = -1;
    DietConnexionPanel cnxPanel = new DietConnexionPanel();
    ProgressPanel progressPanel = new ProgressPanel();

    public DietExportPanel() {
        super(new GridBagLayout());
        int line = 0;
        updateStartButtonState();
        add(startButton, GBC.of(0, line++).colspanReminder().anchorCenter().insets(20, 3).build());
        Box pp = Box.createHorizontalBox();
        pp.add(oneFile);
        pp.add(exploded);
        {
            ButtonGroup bg = new ButtonGroup();
            bg.add(oneFile);
            bg.add(exploded);
            oneFile.setSelected(true);
        }
        add(pp, GBC.of(0, line++).fillHorizontal().anchorWest().insets(3).colspanReminder().build());

        add(cnxPanel, GBC.of(0, line).fillHorizontal().anchorWest().insets(3).colspanReminder().build());

        add(progressPanel, GBC.of(0, line++).colspanReminder().anchorSouth().fillHorizontal().insets(3).weightx(2).weighty(1000).build());
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UI.async(() -> onStartExport());
            }
        });
        cnxPanel.addConnexionStatusListener(new DietConnexionPanel.ConnexionStatusListener() {
            @Override
            public void onConnectionCheckStart(CnxInfo info) {
                progressPanel.updateStatus( NMsg.ofC("Checking connection..."));
                updateStartButtonState();
            }

            @Override
            public void onConnectionSuccess(CnxInfo info) {
                progressPanel.updateStatus( NMsg.ofC("Successful connection"));
                updateStartButtonState();
            }

            @Override
            public void onConnectionFailure(CnxInfo info, Throwable ex) {
                progressPanel.updateStatus( NMsg.ofC("Connection failed : %s", ex.getMessage()));
                updateStartButtonState();
            }
        });
    }

    private void onStartExport() {
        if (!workInProgress) {
            workInProgress = true;
            updateStartButtonState();
            progressPanel.updateStatus(Integer.MIN_VALUE, NMsg.ofC("Starting..."));
            try {
                File file = outFile;

//                SchemaId schemaId =
//                        cnxPanel.selectedCatalog==null?null:new SchemaId(
//                                cnxPanel.selectedCatalog.getCatalogName(),
//                                null
//                        );
//                String schemaName = schemaId == null ? "unknown" :
//                        (schemaId.getSchemaName() != null && schemaId.getCatalogName() != null) ? (schemaId.getCatalogName() + "." + schemaId.getSchemaName()) :
//                                (schemaId.getSchemaName() != null) ? (schemaId.getSchemaName()) :
//                                        (schemaId.getCatalogName() != null) ? (schemaId.getCatalogName()) :
//                                                "unknown";

                String dbName = cnxPanel.selectedDatabaseId() == null ? null : cnxPanel.selectedDatabaseId().getDatabaseName();//cnxPanel.getSelectedCatalog().getCatalogName();
                if (cnxPanel.selectedDatabase == null) {
                    return;
                }
                if (file == null) {
                    file = new File(dbName + ".dump");
                }
                if (file.isDirectory()) {
                    file = new File(file, dbName + ".dump");
                }
                JFileChooser jfc = new JFileChooser();
                jfc.setSelectedFile(file);
                if (jfc.showSaveDialog(DietExportPanel.this) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    if (selectedFile != null) {
                        if (!selectedFile.exists()) {
                            if (!selectedFile.getName().endsWith(".dump")) {
                                selectedFile = new File(selectedFile.getParent(), selectedFile.getName() + ".dump");
                            }
                        }
                        if (selectedFile.isDirectory()) {
                            selectedFile = new File(selectedFile, dbName + ".dump");
                        }
                        DbStore s = new DbStore();
                        s.setOut(selectedFile);
                        CnxInfo cnx = cnxPanel.cnxInfo();
                        if (cnx.getDbName() == null) {
                            SqlDialect dbType = cnx.getType();
                            if (
                                    dbType == SqlDialect.MSSQLSERVER
//                                            || dbType == SqlDialect.JTDS_SQLSERVER
                            ) {
                                if (dbName != null) {
                                    cnx.setDbName(dbName);
                                }
                            }

                        }
                        s.setDb(cnx);
                        doExport(cnx, selectedFile);
                    }
                }
            } finally {
                workInProgress = false;
                progressPanel.updateStatus();
                updateStartButtonState();
            }
        }
    }

    private void exportExplodedFileOne(TableId table, File nf, int i, int len) {
        try (NSqlDump db = cnxPanel.createDriver()) {
            try (StoreWriter w = new DbStoreWriter(nf, db)) {
                progressPanel.updateStatus((i) * 100 / len, NMsg.ofC("Table %s...",table.getTableName()));
                w.setCompress(optionCompress);
                w.setData(optionData);
                w.setMaxRows(optionMaxRows);
                w.addStructs(new TableIdAsStoreStructId(table));
                w.write();
                w.flush();
                progressPanel.updateStatus((i + 1) * 100 / len, NMsg.ofC("Table %s" , table.getTableName()));
            }
        }
    }

    private void exportExplodedFile(File selectedFile) {
        TableId[] tables;
        try (NSqlDump db = cnxPanel.createDriver()) {
            tables = db.getConnection().getTableIds(cnxPanel.selectedDatabaseId()).stream()
                    .filter(x -> cnxPanel.acceptTable(x, db))
                    .toArray(TableId[]::new);
        }
        int errorsCount=0;
        for (int i = 0, tablesLength = tables.length; i < tablesLength; i++) {
            TableId table = tables[i];
            try {
                String n = selectedFile.getName();
                if (n.endsWith(".dump")) {
                    n = n.substring(0, n.length() - ".dump".length());
                }
                n = n + "-" + table.getTableName() + ".dump";
                selectedFile.getParentFile().mkdirs();
                File nf = new File(selectedFile.getParent(), n);
                exportExplodedFileOne(table, nf, i, tables.length);
                progressPanel.updateStatus(100, NMsg.ofC("%s Tables exportées",tables.length));
            } catch (Exception ex) {
                ex.printStackTrace();
                progressPanel.updateStatus( NMsg.ofC("Export Echoué : %s", ex.getMessage()));
                JOptionPane.showMessageDialog(DietExportPanel.this, "Erreur " + table.getTableName(), "Error", JOptionPane.ERROR_MESSAGE);
                errorsCount++;
            }
        }
        if(errorsCount==0) {
            progressPanel.updateStatus( NMsg.ofC("Export réussi"));
            JOptionPane.showMessageDialog(DietExportPanel.this, "Export réussi", "Succès", JOptionPane.INFORMATION_MESSAGE);
        }else{
            progressPanel.updateStatus( NMsg.ofC("Export Echoué"));
            JOptionPane.showMessageDialog(DietExportPanel.this, "Export Echoué", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doExport(CnxInfo cnx, File selectedFile) {
        IOLogger.runWith(new IOLogger() {
                             @Override
                             public void log(NMsg msg) {
                                 progressPanel.updateStatus(-1, msg);
                             }
                         },
                () -> {
                    if (oneFile.isSelected()) {
                        exportOneFile(cnx, selectedFile);
                    } else {
                        exportExplodedFile(selectedFile);
                    }
                });
    }

    private void exportOneFile(CnxInfo cnx, File selectedFile) {
        try (NSqlDump db = cnxPanel.createDriver()) {
            try (StoreWriter w = new DbStoreWriter(selectedFile, db)) {
                w.addProgressMonitor(new StoreProgressMonitor() {
                    @Override
                    public void onProgress(double progress, NMsg message) {
                        UI.withinGUI(() -> {
                            progressPanel.updateStatus((int) progress, message);
                        });
//                                    try {
//                                        Thread.sleep(1000);
//                                    } catch (InterruptedException e) {
//                                        e.printStackTrace();
//                                    }
                    }
                });
                w.setCompress(optionCompress);
                w.setData(optionData);
                w.setMaxRows(optionMaxRows);
                StoreStructId[] array = db.getConnection().getTableIds(cnxPanel.selectedDatabaseId()).stream()
                        .filter(x -> cnxPanel.acceptTable(x, db))
                        .map(x -> new TableIdAsStoreStructId(x))
                        .toArray(StoreStructId[]::new);
                w.addStructs(
                        array
                );
                w.write();
                w.flush();
                JOptionPane.showMessageDialog(DietExportPanel.this, "Export réussi", "Succès", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void updateStartButtonState() {
        boolean a = cnxPanel.cnxInfo() != null
//                && selectedSchema != null
                && !workInProgress;
        startButton.setEnabled(a);
        oneFile.setEnabled(a);
        exploded.setEnabled(a);
        cnxPanel.setEnabled(a);
    }


    public void loadConf(Properties conf) {
        this.conf = conf;
        cnxPanel.loadConf(conf);
    }
}
