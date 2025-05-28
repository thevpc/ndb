package net.thevpc.ndb.desktop.panels;

import net.thevpc.nsql.NSqlConnectionString;
import net.thevpc.nsql.NSqlConnectionStringBuilder;
import net.thevpc.nsql.dump.api.NSqlDump;
import net.thevpc.nsql.dump.model.DbStore;
import net.thevpc.nsql.dump.model.DbStoreWriter;
import net.thevpc.nsql.dump.model.TableIdAsStoreStructId;
import net.thevpc.nsql.model.NSqlTableId;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.lib.nserializer.api.StoreWriter;
import net.thevpc.lib.nserializer.api.StoreProgressMonitor;
import net.thevpc.ndb.desktop.util.GBC;
import net.thevpc.ndb.desktop.util.UI;
import net.thevpc.lib.nserializer.api.IOLogger;
import net.thevpc.lib.nserializer.model.StoreStructId;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.function.Predicate;

import net.thevpc.common.swing.list.JCheckBoxList;
import net.thevpc.nsql.NSqlDialect;
import net.thevpc.nsql.model.NSqlTableHeader;

public class NDdbExportPanel extends JPanel {

    JButton startButton = new JButton("Commencer...");
    JRadioButton oneFile = new JRadioButton("One File Per DB");
    JRadioButton exploded = new JRadioButton("One File Per Table");
    Properties conf;
    boolean workInProgress;
    File outFile;
    boolean optionCompress = true;
    boolean optionData = true;
    long optionMaxRows = -1;
    NDdbConnexionPanel cnxPanel = new NDdbConnexionPanel();
    ProgressPanel progressPanel = new ProgressPanel();
    private JCheckBoxList selectedTables = new JCheckBoxList();
    private SimpleProgressLogger simpleProgressLogger = new SimpleProgressLogger();

    public NDdbExportPanel() {
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

        add(cnxPanel, GBC.of(0, line++).fillHorizontal().anchorWest().insets(3).colspanReminder().build());
        add(selectedTables, GBC.of(0, line++).fillBoth().anchorWest().weight(2, 2).insets(3).colspanReminder().build());

        add(progressPanel, GBC.of(0, line++).colspanReminder().anchorSouth().fillHorizontal().insets(3).weightx(2).weighty(1000).build());
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UI.async(() -> onStartExport());
            }
        });
        cnxPanel.addConnexionStatusListener(new NDdbConnexionPanel.ConnexionStatusListener() {
            @Override
            public void onConnectionCheckStart(NSqlConnectionString info) {
                progressPanel.updateStatus(NMsg.ofC("Checking connection..."));
                updateStartButtonState();
                selectedTables.resetModel();
            }

            @Override
            public void onConnectionSuccess(NSqlConnectionString info, NSqlDump r) {
                progressPanel.updateStatus(NMsg.ofC("Successful connection"));
                updateStartButtonState();
                updateTableList(r);
            }

            @Override
            public void onConnectionFailure(NSqlConnectionString info, Throwable ex) {
                progressPanel.updateStatus(NMsg.ofC("Connection failed : %s", ex.getMessage()));
                updateStartButtonState();
                selectedTables.resetModel();
            }
        });
    }

    private String selectedFilePreferredName() {
        String dbName = cnxPanel.selectedDatabaseId() == null ? null : cnxPanel.selectedDatabaseId().getDatabaseName();//cnxPanel.getSelectedCatalog().getCatalogName();
        boolean allSelected = selectedTables.isAllSelected();
        if (allSelected) {
            return dbName;
        }
        int s = selectedTables.getElementCount();
        ArrayList<NSqlTableHeader> selected = new ArrayList<>();
        int all = 0;
        for (int i = 0; i < s; i++) {
            NSqlTableHeader th = (NSqlTableHeader) selectedTables.getSelectedElementAt(i);
            if (th != null) {
                selected.add(th);
                all++;
            }
        }
        if (selected.size() == 1 && selected.size() < all) {
            return dbName + "." + selected.get(0).getTableName();
        }
        return dbName;
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
                String preferredname = selectedFilePreferredName();
                if (cnxPanel.selectedDatabase == null) {
                    return;
                }
                if (file == null) {
                    file = new File(preferredname + ".dump");
                }
                if (file.isDirectory()) {
                    file = new File(file, preferredname + ".dump");
                }
                JFileChooser jfc = new JFileChooser();
                jfc.setSelectedFile(file);
                if (jfc.showSaveDialog(NDdbExportPanel.this) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    if (selectedFile != null) {
                        if (!selectedFile.exists()) {
                            if (!selectedFile.getName().endsWith(".dump")) {
                                selectedFile = new File(selectedFile.getParent(), selectedFile.getName() + ".dump");
                            }
                        }
                        if (selectedFile.isDirectory()) {
                            selectedFile = new File(selectedFile, preferredname + ".dump");
                        }
                        DbStore s = new DbStore();
                        s.setOut(selectedFile);
                        NSqlConnectionStringBuilder cnx = cnxPanel.cnxInfo();
                        if (cnx.getDbName() == null) {
                            NSqlDialect dbType = cnx.getDialect();
                            if (dbType == NSqlDialect.MSSQLSERVER //                                            || dbType == SqlDialect.JTDS_SQLSERVER
                            ) {
                                if (dbName != null) {
                                    cnx.setDbName(dbName);
                                }
                            }

                        }
                        s.setDb(cnx);
                        File selectedFile0 = selectedFile;
                        IOLogger.get().add(simpleProgressLogger).runWith(
                                () -> {
                                    if (oneFile.isSelected()) {
                                        exportOneFile(cnx, selectedFile0);
                                    } else {
                                        exportExplodedFile(selectedFile0);
                                    }
                                });
                    }
                }
            } finally {
                workInProgress = false;
                progressPanel.updateStatus();
                updateStartButtonState();
            }
        }
    }

    private void exportExplodedFileOne(NSqlTableId table, File nf, int i, int len) {
        try (NSqlDump db = cnxPanel.createDumpSilently()) {
            try (StoreWriter w = new DbStoreWriter(nf, db)) {
                progressPanel.updateStatus((i) * 100 / len, NMsg.ofC("Table %s...", table.getTableName()));
                w.setCompress(optionCompress);
                w.setData(optionData);
                w.setMaxRows(optionMaxRows);
                w.addStructs(new TableIdAsStoreStructId(table));
                w.write();
                w.flush();
                progressPanel.updateStatus((i + 1) * 100 / len, NMsg.ofC("Table %s", table.getTableName()));
            }
        }
    }

    private Predicate<NSqlTableHeader> tableIdFilter(NSqlDump db) {
        boolean allSelected = selectedTables.isAllSelected();
        boolean noneSelected = selectedTables.isNoneSelected();
        return (allSelected || noneSelected) ? (x -> cnxPanel.acceptTable(x, db)) : (x -> {
            boolean b = cnxPanel.acceptTable(x, db);
            if (!b) {
                return false;
            }
            int s = selectedTables.getSelectedCount();
            for (int i = 0; i < s; i++) {
                NSqlTableHeader th = (NSqlTableHeader) selectedTables.getSelectedElementAt(i);
                if (th != null) {
                    if (th.equals(x)) {
                        return true;
                    }
                }
            }
            return false;
        });
    }

    private void exportExplodedFile(File selectedFile) {
        NSqlTableId[] tables;
        try (NSqlDump db = cnxPanel.createDumpSilently()) {
            tables = db.getConnection().getTableHeaders(cnxPanel.selectedDatabaseId()).stream()
                    .filter(tableIdFilter(db))
                    .map(x -> x.toTableId())
                    .toArray(NSqlTableId[]::new);
        }
        if (tables.length == 0) {
            JOptionPane.showMessageDialog(NDdbExportPanel.this, "Aucune table a exporter", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int errorsCount = 0;
        for (int i = 0, tablesLength = tables.length; i < tablesLength; i++) {
            NSqlTableId table = tables[i];
            try {
                String n = selectedFile.getName();
                if (n.endsWith(".dump")) {
                    n = n.substring(0, n.length() - ".dump".length());
                }
                n = n + "-" + table.getTableName() + ".dump";
                selectedFile.getParentFile().mkdirs();
                File nf = new File(selectedFile.getParent(), n);
                exportExplodedFileOne(table, nf, i, tables.length);
                progressPanel.updateStatus(100, NMsg.ofC("%s Tables exportées", tables.length));
            } catch (Exception ex) {
                ex.printStackTrace();
                progressPanel.updateStatus(NMsg.ofC("Export Echoué : %s", ex.getMessage()));
                JOptionPane.showMessageDialog(NDdbExportPanel.this, "Erreur " + table.getTableName(), "Error", JOptionPane.ERROR_MESSAGE);
                errorsCount++;
            }
        }
        if (errorsCount == 0) {
            progressPanel.updateStatus(NMsg.ofC("Export réussi"));
            JOptionPane.showMessageDialog(NDdbExportPanel.this, "Export réussi : " + tables.length + " Table(s)", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } else {
            progressPanel.updateStatus(NMsg.ofC("Export Echoué"));
            JOptionPane.showMessageDialog(NDdbExportPanel.this, "Export Echoué : " + tables.length + " Table(s)", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportOneFile(NSqlConnectionStringBuilder cnx, File selectedFile) {
        try (NSqlDump db = cnxPanel.createDumpSilently()) {
            StoreStructId[] array = db.getConnection().getTableHeaders(cnxPanel.selectedDatabaseId()).stream()
                    .filter(tableIdFilter(db))
                    .map(x -> new TableIdAsStoreStructId(x.toTableId()))
                    .toArray(StoreStructId[]::new);
            if (array.length == 0) {
                JOptionPane.showMessageDialog(NDdbExportPanel.this, "Aucune table a exporter", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
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

                w.addStructs(
                        array
                );
                w.write();
                w.flush();
                JOptionPane.showMessageDialog(NDdbExportPanel.this, "Export réussi : " + array.length + " Table(s)", "Succès", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void updateTableList(NSqlDump db) {
        NSqlConnectionStringBuilder cnxInfo = cnxPanel.cnxInfo();
        if (cnxInfo != null) {
            IOLogger.get().add(simpleProgressLogger).runWith(
                    () -> {
                        selectedTables.resetModel();
                        try {
                            // .toArray(JCheckBox[]::new)
                            db.getConnection().getAnyTables().stream()
                                    .filter(x -> cnxPanel.acceptTable(x, db))
                                    .forEach(x -> {
                                        System.out.println("addItem " + x);
                                        selectedTables.addItem(x, x.getTableName(), x.toString());
                                    });

                        } catch (Exception ex) {
                            selectedTables.resetModel();
                        }
                    });
        } else {
            selectedTables.resetModel();
        }
    }

    private void updateStartButtonState() {
        NSqlConnectionStringBuilder cnxInfo = cnxPanel.cnxInfo();
        boolean a = cnxInfo != null
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

    private class SimpleProgressLogger implements IOLogger {

        public SimpleProgressLogger() {
        }

        @Override
        public void log(NMsg msg) {
            progressPanel.updateStatus(-1, msg);
        }
    }
}
