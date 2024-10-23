package net.thevpc.dbrman.desktop.panels;

import net.thevpc.dbrman.api.DatabaseDriver;
import net.thevpc.dbrman.desktop.Main;
import net.thevpc.dbrman.model.*;
import net.thevpc.dbrman.util.DatabaseDriverFactories;
import net.thevpc.nsql.CnxInfo;
import net.thevpc.nsql.SqlDialect;
import net.thevpc.nsql.model.TableId;
import net.thevpc.violin.api.StoreWriter;
import net.thevpc.violin.api.StoreProgressMonitor;
import net.thevpc.dbrman.desktop.util.GBC;
import net.thevpc.dbrman.desktop.util.UI;
import net.thevpc.violin.impl.IOLogger;
import net.thevpc.violin.model.StoreStructId;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

public class WdbrmanPanelExportPanel extends JPanel {
    JLabel statusLabel = new JLabel();
    JLabel percentLabel = new JLabel();
    Box statusBar = Box.createHorizontalBox();
    JProgressBar progressBar = new JProgressBar();
    JButton startButton = new JButton("Commencer...");
    Properties conf;
    boolean workInProgress;
    File outFile;
    boolean optionCompress = true;
    boolean optionData = true;
    long optionMaxRows = -1;
    int statusProgress = -1;
    String statusMessage = null;
    WdbrmanPanelConnexionPanel cnxPanel = new WdbrmanPanelConnexionPanel();

    public WdbrmanPanelExportPanel() {
        super(new GridBagLayout());
        int line = 0;
        updateStartButtonState();
        statusBar.add(Box.createHorizontalGlue());
        statusBar.add(statusLabel);
        statusBar.add(Box.createHorizontalStrut(10));
        percentLabel.setMinimumSize(new Dimension(50, 1));
        statusBar.add(percentLabel);
        statusBar.add(Box.createHorizontalStrut(10));
        statusBar.add(new JLabel(" | v" + Main.VERSION));
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);

        statusBar.setBorder(BorderFactory.createEtchedBorder());
        add(startButton, GBC.of(0, line++).colspanReminder().anchorCenter().insets(20, 3).build());
        add(cnxPanel, GBC.of(0, line).fillHorizontal().anchorWest().insets(3).colspanReminder().build());

        add(progressBar, GBC.of(0, line++).colspanReminder().anchorSouth().fillHorizontal().insets(3).weightx(2).weighty(1000).build());
        add(statusBar, GBC.of(0, line++).colspanReminder().anchorSouth().fillHorizontal().insets(3).weightx(2).build());
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UI.async(() -> onStartExport());
            }
        });
        cnxPanel.addConnexionStatusListener(new WdbrmanPanelConnexionPanel.ConnexionStatusListener() {
            @Override
            public void onConnectionCheckStart(CnxInfo info) {
                updateStatus(statusProgress, "Checking connection...");
                updateStartButtonState();
            }

            @Override
            public void onConnectionSuccess(CnxInfo info) {
                updateStatus(statusProgress, "Successful connection");
                updateStartButtonState();
            }

            @Override
            public void onConnectionFailure(CnxInfo info, Throwable ex) {
                updateStatus(statusProgress, "Connection failed : " + ex.getMessage());
                updateStartButtonState();
            }
        });
    }

    private void onStartExport() {
        if (!workInProgress) {
            workInProgress = true;
            updateStartButtonState();
            updateStatus(Integer.MIN_VALUE, "Starting...");
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
                if (jfc.showSaveDialog(WdbrmanPanelExportPanel.this) == JFileChooser.APPROVE_OPTION) {
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
                            SqlDialect dbType = SqlDialect.parse(cnx.getType()).orNull();
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
//                        exportOneFile(cnx, selectedFile);
                        exportExplodedFile(selectedFile);
                    }
                }
            } finally {
                workInProgress = false;
                if (isIndeterminateProgress()) {
                    updateStatus(-1, statusMessage);
                }
                updateStartButtonState();
            }
        }
    }

    private void exportExplodedFileOne(TableId table, File nf, int i, int len) {
        try (DatabaseDriver db = cnxPanel.createDriver()) {
            try (StoreWriter w = new DbStoreWriter(nf, db)) {
                updateStatus((i) * 100 / len, "Table " + table.getTableName() + "...");
                w.setCompress(optionCompress);
                w.setData(optionData);
                w.setMaxRows(optionMaxRows);
                w.addStructs(new TableIdAsStoreStructId(table));
                w.write();
                w.flush();
                updateStatus((i + 1) * 100 / len, "Table " + table.getTableName());
            }
        }
    }

    private void exportExplodedFile(File selectedFile) {

        IOLogger.runWith(new IOLogger() {
                             @Override
                             public void log(String msg) {
                                 updateStatus(-1, msg);
                             }
                         },
                () -> {
                    TableId[] tables;
                    try (DatabaseDriver db = cnxPanel.createDriver()) {
                        tables = db.getConnection().getTableIds(cnxPanel.selectedDatabaseId()).stream()
                                .filter(x -> cnxPanel.acceptTable(x, db))
                                .toArray(TableId[]::new);
                    }
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
                            updateStatus(100, tables.length + " Tables exportées");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(WdbrmanPanelExportPanel.this, "Erreur " + table.getTableName(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
        );
        JOptionPane.showMessageDialog(WdbrmanPanelExportPanel.this, "Export réussi", "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportOneFile(CnxInfo cnx, File selectedFile) {
        try (DatabaseDriver db = cnxPanel.createDriver()) {
            try (StoreWriter w = new DbStoreWriter(selectedFile, db)) {
                w.addProgressMonitor(new StoreProgressMonitor() {
                    @Override
                    public void onProgress(double progress, String message) {
                        UI.withinGUI(() -> {
                            updateStatus((int) progress, message);
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
                JOptionPane.showMessageDialog(WdbrmanPanelExportPanel.this, "Export réussi", "Succès", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void updateStatus(int progress, String message) {
        statusProgress = progress;
        statusMessage = message;
        UI.withinGUI(() -> {
            if (isIndeterminateProgress()) {
                progressBar.setIndeterminate(true);
                statusLabel.setText(statusMessage == null ? "" : statusMessage);
                percentLabel.setText("");
            } else {
                progressBar.setIndeterminate(false);
                if (statusProgress < 0 || statusProgress > 100) {
                    statusLabel.setText(statusMessage == null ? "" : statusMessage);
                    percentLabel.setText("");
                } else {
                    statusLabel.setText(statusMessage == null ? "" : statusMessage);
                    percentLabel.setText(statusProgress + "%");
                }
            }
        });
    }

    private boolean isIndeterminateProgress() {
        return statusProgress == Integer.MIN_VALUE;
    }


    private void updateStartButtonState() {
        boolean a = cnxPanel.cnxInfo() != null
//                && selectedSchema != null
                && !workInProgress;
        startButton.setEnabled(a);
    }


    public void loadConf(Properties conf) {
        this.conf = conf;
        cnxPanel.loadConf(conf);
    }
}
