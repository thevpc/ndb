package net.thevpc.dbrman.desktop.panels;

import net.thevpc.dbinfo.api.DatabaseDriver;
import net.thevpc.dbinfo.model.*;
import net.thevpc.dbinfo.util.DatabaseDriverFactories;
import net.thevpc.vio2.api.StoreWriter;
import net.thevpc.vio2.api.StoreProgressMonitor;
import net.thevpc.dbrman.desktop.util.GBC;
import net.thevpc.dbrman.desktop.util.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;

public class WdbrmanPanelExportPanel extends JPanel {
    JLabel statusLabel = new JLabel();
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
        statusBar.add(new JLabel("1.0"));
        statusBar.add(statusLabel);
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
        cnxPanel.addPropertyChangerListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println("propertyChange " + evt.getPropertyName());
                switch (evt.getPropertyName()) {
                    case "connexion.status": {
                        updateStatus(statusProgress, "Connexion : " + evt.getNewValue());
                        updateStartButtonState();
                        return;
                    }
                }
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

                SchemaId schemaId = cnxPanel.selectedSchemaId();
                String schemaName = schemaId == null ? "unknown" :
                        (schemaId.getSchemaName() != null && schemaId.getCatalogName() != null) ? (schemaId.getCatalogName() + "." + schemaId.getSchemaName()) :
                                (schemaId.getSchemaName() != null) ? (schemaId.getSchemaName()) :
                                        (schemaId.getCatalogName() != null) ? (schemaId.getCatalogName()) :
                                                "unknown";

                String catName = cnxPanel.getSelectedCatalog().getCatalogName();
                if (file == null) {
                    file = new File(schemaName + ".dump");
                }
                if (file.isDirectory()) {
                    file = new File(file, schemaName + ".dump");
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
                            selectedFile = new File(selectedFile, schemaName + ".dump");
                        }
                        DbStore s = new DbStore();
                        s.setOut(selectedFile);
                        CnxInfo cnx = cnxPanel.cnxInfo();
                        if (cnx.getDbName() == null) {
                            DbType dbType = DatabaseDriverFactories.parseDbType(cnx.getType());
                            if (
                                    dbType == DbType.SQLSERVER
                                            || dbType == DbType.JTDS_SQLSERVER
                            ) {
                                if (schemaId != null) {
                                    cnx.setDbName(schemaId.getCatalogName());
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

    private void exportExplodedFile(File selectedFile) {
        try (DatabaseDriver db = cnxPanel.createDriver()) {
            TableId[] tables = db.getTableIds(new SchemaId(
                    cnxPanel.selectedSchemaId().getCatalogName(),
                    null
            )).stream()
                    .filter(x -> cnxPanel.acceptTable(x, db))
                    .toArray(TableId[]::new);
            for (int i = 0, tablesLength = tables.length; i < tablesLength; i++) {
                TableId table = tables[i];
                System.out.println(table);
                try {
                    String n = selectedFile.getName();
                    if (n.endsWith(".dump")) {
                        n = n.substring(0, n.length() - ".dump".length());
                    }
                    n = n + "-" + table.getTableName() + ".dump";
                    selectedFile.getParentFile().mkdirs();
                    try (StoreWriter w = new DbStoreWriter(new File(selectedFile.getParent(), n), db)) {
                        updateStatus((i) * 100 / tables.length, "Table " + table.getTableName()+"...");
                        w.setCompress(optionCompress);
                        w.setData(optionData);
                        w.setMaxRows(optionMaxRows);
                        w.addStructs(table);
                        w.write();
                        w.flush();
                        updateStatus((i + 1) * 100 / tables.length, "Table " + table.getTableName());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(WdbrmanPanelExportPanel.this, "Erreur " + table.getTableName(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
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
                w.addStructs(db.getTableIds(cnxPanel.selectedSchemaId()).stream()
                        .filter(x -> cnxPanel.acceptTable(x, db))
                        .toArray(TableId[]::new)
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
            } else {
                progressBar.setIndeterminate(false);
                if (statusProgress < 0 || statusProgress > 100) {
                    statusLabel.setText((statusMessage == null ? "" : statusMessage));
                } else {
                    statusLabel.setText(statusProgress + "% | " + (statusMessage == null ? "" : statusMessage));
                }
            }
        });
    }

    private boolean isIndeterminateProgress() {
        return statusProgress == Integer.MIN_VALUE;
    }


    private void updateStartButtonState() {
        boolean a=cnxPanel.cnxInfo() != null
//                && selectedSchema != null
                && !workInProgress;
        System.out.println(a);
        startButton.setEnabled(a);
    }


    public void loadConf(Properties conf) {
        this.conf = conf;
        cnxPanel.loadConf(conf);
    }
}
