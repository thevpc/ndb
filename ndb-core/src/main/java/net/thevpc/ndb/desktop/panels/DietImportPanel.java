package net.thevpc.diet.desktop.panels;

import net.thevpc.diet.desktop.util.GBC;
import net.thevpc.diet.desktop.util.UI;
import net.thevpc.nsql.NSqlConnectionString;
import net.thevpc.nsql.dump.api.NSqlDump;
import net.thevpc.nsql.dump.io.In;
import net.thevpc.nsql.dump.options.DumpToDbOptions;
import net.thevpc.nsql.dump.options.TableRestoreOptions;
import net.thevpc.nsql.dump.store.NSqlDumpService;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.lib.nserializer.impl.IOLogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Properties;

public class DietImportPanel extends JPanel {
    JButton startButton = new JButton("Commencer...");
    Properties conf;
    boolean workInProgress;
    File outFile;
    boolean optionCompress = true;
    boolean optionData = true;
    long optionMaxRows = -1;
    DietConnexionPanel cnxPanel = new DietConnexionPanel();
    ProgressPanel progressPanel = new ProgressPanel();

    public DietImportPanel() {
        super(new GridBagLayout());
        int line = 0;
        updateStartButtonState();
        add(startButton, GBC.of(0, line++).colspanReminder().anchorCenter().insets(20, 3).build());
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
            public void onConnectionCheckStart(NSqlConnectionString info) {
                progressPanel.updateStatus( NMsg.ofC("Checking connection..."));
                updateStartButtonState();
            }

            @Override
            public void onConnectionSuccess(NSqlConnectionString info,NSqlDump r) {
                progressPanel.updateStatus( NMsg.ofC("Successful connection"));
                updateStartButtonState();
            }

            @Override
            public void onConnectionFailure(NSqlConnectionString info, Throwable ex) {
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
                jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                jfc.setSelectedFile(file);
                if (jfc.showOpenDialog(DietImportPanel.this) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    if (selectedFile != null) {
                        importExplodedFile(selectedFile);
                    }
                }
            } finally {
                workInProgress = false;
                progressPanel.updateStatus();
                updateStartButtonState();
            }
        }
    }

    private void importExplodedFile(File selectedFile) {

        IOLogger.runWith(new IOLogger() {
                             @Override
                             public void log(NMsg msg) {
                                 progressPanel.updateStatus(-1, msg);
                             }
                         },
                () -> {
                    try (NSqlDump db = cnxPanel.createDumpSilently()) {
                        NSqlDumpService s=new NSqlDumpService();
                        s.dumpToDb(
                                new DumpToDbOptions()
                                        .setData(true)
                                        .setIn(new In(selectedFile))
                                        .setSchemaMode(
                                                new TableRestoreOptions()
                                        )
                                ,db
                        );
                    }
                }
        );
        JOptionPane.showMessageDialog(DietImportPanel.this, "Import réussi", "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateStartButtonState() {
        boolean a = cnxPanel.cnxInfo() != null
//                && selectedSchema != null
                && !workInProgress;
        startButton.setEnabled(a);
        cnxPanel.setEnabled(a);
    }


    public void loadConf(Properties conf) {
        this.conf = conf;
        cnxPanel.loadConf(conf);
    }
}
