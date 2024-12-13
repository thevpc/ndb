package net.thevpc.diet.desktop.panels;

import net.thevpc.nsql.UncheckedSqlException;
import net.thevpc.nsql.dump.api.NSqlDump;
import net.thevpc.nsql.dump.util.DatabaseDriverFactories;
import net.thevpc.diet.desktop.util.GBC;
import net.thevpc.diet.desktop.util.UI;
import net.thevpc.nsql.CnxInfo;
import net.thevpc.nsql.model.*;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.thevpc.nsql.NSqlDialect;

public class DietConnexionPanel extends JPanel {

    public static final Logger LOGGER = Logger.getLogger(DietConnexionPanel.class.getName());
    private final JLabel dbLabel;
    JTextField hostField = new JTextField();
    JSpinner portField = new JSpinner(new SpinnerNumberModel());
    JTextField loginField = new JTextField();
    JTextField instanceField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JLabel loginLabel = new JLabel("Login");
    JLabel passwordLabel = new JLabel("Mot de Passe");
    JLabel instanceLabel = new JLabel("Instance");
    JCheckBox integratedSecurity = new JCheckBox("Sécurité Integrée");
    JComboBox serverType = new JComboBox(new Object[0]);
    JComboBox dbList = new JComboBox(new Object[0]);
    Properties conf;
    DatabaseId selectedDatabase;
    PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    List<ConnexionStatusListener> connexionStatusListeners = new ArrayList<>();
    private String currentConnexionAttemptId;

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        hostField.setEnabled(enabled);
        portField.setEnabled(enabled);
        instanceField.setEnabled(enabled);
        loginField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        integratedSecurity.setEnabled(enabled);
        serverType.setEnabled(enabled);
        dbList.setEnabled(enabled);
    }

    public DietConnexionPanel() {
        super(new GridBagLayout());
        int line = 0;
        dbLabel = new JLabel("Database");

        serverType = new JComboBox(
                Arrays.stream(NSqlDialect.values()).toArray()
        );
        add(new JLabel("Type de Serveur"), GBC.of(0, line).fillHorizontal().anchorWest().insets(3).build());
        add(serverType, GBC.of(1, line++).fillHorizontal().anchorWest().insets(3).weightx(2).colspanReminder().build());

        add(new JLabel("Adresse"), GBC.of(0, line).fillHorizontal().anchorWest().insets(3).build());
        add(hostField, GBC.of(1, line).fillHorizontal().anchorWest().insets(3).weightx(2).colspan(2).build());
        add(new JLabel("Port"), GBC.of(3, line).fillHorizontal().anchorWest().insets(3).build());
        add(portField, GBC.of(4, line++).fillHorizontal().anchorWest().insets(3).weightx(2).build());

        add(instanceLabel, GBC.of(3, line).fillHorizontal().anchorWest().insets(3).build());
        add(instanceField, GBC.of(4, line++).fillHorizontal().anchorWest().insets(3).weightx(2).build());

        add(integratedSecurity, GBC.of(0, line).fillHorizontal().anchorWest().insets(3).weightx(2).build());

        add(loginLabel, GBC.of(1, line).fillHorizontal().anchorWest().insets(3).build());
        add(loginField, GBC.of(2, line).fillHorizontal().anchorWest().insets(3).weightx(2).build());

        add(passwordLabel, GBC.of(3, line).fillHorizontal().anchorWest().insets(3).build());
        add(passwordField, GBC.of(4, line++).fillHorizontal().anchorWest().insets(3).weightx(2).build());

        add(dbLabel, GBC.of(0, line).fillHorizontal().anchorWest().insets(3).build());
        add(dbList, GBC.of(1, line++).fillHorizontal().anchorWest().insets(3).weightx(2).colspanReminder().build());

        hostField.addFocusListener(new MyFocusListener("host"));
        portField.addFocusListener(new MyFocusListener("port"));
        loginField.addFocusListener(new MyFocusListener("login"));
        passwordField.addFocusListener(new MyFocusListener("password"));
        integratedSecurity.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean b = e.getStateChange() == ItemEvent.SELECTED;
                pcs.firePropertyChange("integratedSecurity", !b, b);
                passwordField.setVisible(!b);
                passwordLabel.setVisible(!b);
                loginField.setVisible(!b);
                loginLabel.setVisible(!b);
                onChangeServerConnexion();
            }
        });
        serverType.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                resetDbList();
            }
        });
        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                resetDbList();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                resetDbList();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                resetDbList();
            }
        };
        hostField.getDocument().addDocumentListener(documentListener);
        portField.getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                resetDbList();
            }
        });
        dbList.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String s = value == null ? null : ((DatabaseHeader) value).getDatabaseName();
                return super.getListCellRendererComponent(list, s, index, isSelected, cellHasFocus);
            }
        });

        dbList.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    DatabaseHeader item = (DatabaseHeader) e.getItem();
                    onChangeDatabase(item == null ? null : item.toDatabaseId());
                }
            }
        });
        serverType.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    onChangeServerConnexion();
                }
            }
        });
        onChangeServerConnexion();
    }

    private void resetDbList() {
        dbList.setModel(new DefaultComboBoxModel());
    }

    public boolean acceptTable(TableHeader th, NSqlDump d) {
        DatabaseId catalogId = selectedDatabaseId();
        TableId tid = th.toTableId();
//        if (schemaId.getSchemaName() != null) {
//            if (!Objects.equals(tid.getSchemaName(), schemaId.getSchemaName())) {
//                return false;
//            }
//        }
        NSqlDialect a = getSelectedSqlDialect().orNull();
        if (th.isSystemTable()) {
            return false;
        }
        if (a == NSqlDialect.MSSQLSERVER || a == NSqlDialect.MSSQLSERVER_JTDS) {
            if ("sys".equals(tid.getSchemaId().getSchemaName())) {
                return false;
            }
            if ("INFORMATION_SCHEMA".equals(tid.getSchemaId().getSchemaName())) {
                return false;
            }
        }
        if (catalogId != null) {
            if (catalogId.getCatalogName() != null && tid.getCatalogName() != null) {
                if (!Objects.equals(tid.getCatalogName(), catalogId.getCatalogName())) {
                    return false;
                }
            }
        }
        if (d.getConnection().isSpecialTable(tid)) {
            return false;
        }
        return true;
    }

    private NOptional<NSqlDialect> getSelectedSqlDialect() {
        return NOptional.of((NSqlDialect) serverType.getSelectedItem());
    }

    public CnxInfo cnxInfo() {
        Object value = portField.getValue();
        if (value instanceof Number) {
            int i = ((Number) value).intValue();
            if (i <= 0) {
                value = null;
            }
        }
        String dbName = null;
        NSqlDialect dbType = getSelectedSqlDialect().orElse(NSqlDialect.MSSQLSERVER);

        {
            dbName = selectedDatabaseId() == null ? null : selectedDatabaseId().getDatabaseName();
        }
        CnxInfo c = new CnxInfo()
                .setType(dbType)
                .setDbName(dbName)
                .setUser(loginField.getText())
                .setPassword(new String(passwordField.getPassword()))
                .setHost(hostField.getText())
                .setPort(value == null ? null : value.toString())
                .setIntegrationSecurity(integratedSecurity.isSelected())
                .setInstanceName(NStringUtils.trimToNull(instanceField.getName()));

        if (c.getDbName() == null) {
            NSqlDialect dbType2 = c.getType();
            if (dbType2 == NSqlDialect.MSSQLSERVER
                    || dbType2 == NSqlDialect.MSSQLSERVER_JTDS) {
                if (dbName != null) {
                    c.setDbName(dbName);
                }
            }

        }
        return c;
    }

    void onChangeServerConnexion() {
        UI.async(() -> {
            NSqlDialect selectedItem = getSelectedSqlDialect().orNull();
            instanceField.setVisible(
                    selectedItem == NSqlDialect.MSSQLSERVER
                    || selectedItem == NSqlDialect.MSSQLSERVER_JTDS
            );
            instanceLabel.setVisible(
                    selectedItem == NSqlDialect.MSSQLSERVER
                    || selectedItem == NSqlDialect.MSSQLSERVER_JTDS
            );
            try (NSqlDump d = createDump()) {
                List<DatabaseHeader> catalogs = d.getConnection().getDatabases();
                catalogs.sort(Comparator.comparing(x -> x.getDatabaseName()));
                UI.withinGUI(() -> {
                    dbList.setModel(new DefaultComboBoxModel(
                            catalogs.toArray(new Object[0])
                    ));
                    dbList.setSelectedItem(selectedDatabase);
                    DatabaseHeader v = (DatabaseHeader) dbList.getSelectedItem();
                    onChangeDatabase(v == null ? null : v.toDatabaseId());
                });
            } catch (Exception ex) {
                System.err.println(ex);
            }
        });
    }

    private void onChangeDatabase(DatabaseId item) {
        DatabaseId old = selectedDatabase;
        selectedDatabase = item;
        pcs.firePropertyChange("database", old, item);
        updateTablesCount();
    }

    public NSqlDump createDumpSilently() {
        CnxInfo cnxInfo = cnxInfo();
        return DatabaseDriverFactories.createSqlDump(cnxInfo);
    }

    public NSqlDump createDump() {
        pcs.firePropertyChange("connexion.status", null, "start");
        CnxInfo cnxInfo = null;
        String currentConnexionAttemptId = UUID.randomUUID().toString();
        try {
            this.currentConnexionAttemptId = currentConnexionAttemptId;
            cnxInfo = cnxInfo();
            for (ConnexionStatusListener listener : connexionStatusListeners) {
                listener.onConnectionCheckStart(cnxInfo);
            }
            NSqlDump r = DatabaseDriverFactories.createSqlDump(cnxInfo);
            for (ConnexionStatusListener listener : connexionStatusListeners) {
                listener.onConnectionSuccess(cnxInfo, r);
            }
            return r;
        } catch (RuntimeException e) {
            Throwable ee = e;
            if (ee instanceof UncheckedSqlException) {
                ee = ee.getCause();
            }
            //ignore alder attempts!
            if (Objects.equals(currentConnexionAttemptId, this.currentConnexionAttemptId)) {
                for (ConnexionStatusListener listener : connexionStatusListeners) {
                    listener.onConnectionFailure(cnxInfo, ee);
                }
            }
            throw e;
        }
    }

    public void checkConnexion() {
        UI.async(() -> {
            try (NSqlDump d = createDump()) {

            } catch (Exception ex) {
                //
            }
        });
    }

    public DatabaseId selectedDatabaseId() {
        if (selectedDatabase != null) {
            return selectedDatabase;
        }
        return null;
    }

    private String capitalize(String a) {
        if (a != null && a.length() > 0) {
            return Character.toUpperCase(a.charAt(0)) + a.substring(1);
        }
        return a;
    }

    private void updateTablesCount() {
        try (NSqlDump d = createDump()) {
            DatabaseId databaseId = selectedDatabaseId();
//            for (CatalogHeader catalog : d.getConnection().getCatalogs()) {
//                System.out.println("CATALOG : " + catalog);
//                for (SchemaHeader schema : d.getConnection().getSchemas(catalog.toCatalogId())) {
//                    System.out.println("\tSCHEMA : " + schema);
//                }
//            }
            List<TableHeader> tables = d.getConnection().getAnyTables(databaseId).stream().filter(x -> x.isTable()
                    && !d.getConnection().isSpecialTable(x.toTableId())
            ).collect(Collectors.toList());
            List<TableHeader> filtered = tables.stream().filter(x -> acceptTable(x, d)).collect(Collectors.toList());
//            LOGGER.log(Level.FINEST, "LOG updateTablesCount : " +
//                    databaseId + " : " +
//                    filtered.size() + " : " + filtered);
//            System.out.println("updateTablesCount : " +
//                    databaseId + " : " +
//                    filtered.size() + " : " + filtered);
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    public void loadConf(Properties conf) {
        this.conf = conf;
        if (conf.getProperty("serverType") != null) {
            try {
                serverType.setSelectedItem(
                        NSqlDialect.parse(conf.getProperty("serverType")).orNull()
                );
                pcs.firePropertyChange("serverType", null, getSelectedSqlDialect());
            } catch (Exception ex) {
                //
            }
        }
        onChangeServerConnexion();
    }

    public JLabel getDbLabel() {
        return dbLabel;
    }

    public JCheckBox getIntegratedSecurity() {
        return integratedSecurity;
    }

    public JComboBox getServerType() {
        return serverType;
    }

    public JComboBox getDbList() {
        return dbList;
    }

    public Properties getConf() {
        return conf;
    }

    public void addPropertyChangerListener(PropertyChangeListener li, String... a) {
        if (a.length == 0) {
            pcs.addPropertyChangeListener(li);
        } else {
            for (String s : a) {
                pcs.addPropertyChangeListener(s, li);
            }
        }
    }

    public void addConnexionStatusListener(ConnexionStatusListener item) {
        if (item != null) {
            connexionStatusListeners.add(item);
        }
    }

    public void removeConnexionStatusListener(ConnexionStatusListener item) {
        if (item != null) {
            connexionStatusListeners.remove(item);
        }
    }

    public interface ConnexionStatusListener {

        void onConnectionCheckStart(CnxInfo info);

        void onConnectionSuccess(CnxInfo info, NSqlDump r);

        void onConnectionFailure(CnxInfo info, Throwable ex);
    }

    private class MyFocusListener implements FocusListener {

        private String name;

        public MyFocusListener(String name) {
            this.name = name;
        }

        @Override
        public void focusGained(FocusEvent e) {

        }

        @Override
        public void focusLost(FocusEvent e) {
            onChangeServerConnexion();
            Object value = null;
            switch (name) {
                case "login": {
                    value = loginField.getText();
                    break;
                }
                case "password": {
                    value = new String(passwordField.getPassword());
                    break;
                }
                case "host": {
                    value = hostField.getText();
                    break;
                }
                case "port": {
                    value = portField.getValue();
                    break;
                }
            }
            pcs.firePropertyChange(name, null, value);
        }
    }
}
