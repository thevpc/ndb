package net.thevpc.dbrman.desktop.panels;

import net.thevpc.dbrman.api.DatabaseDriver;
import net.thevpc.dbrman.util.DatabaseDriverFactories;
import net.thevpc.dbrman.desktop.util.GBC;
import net.thevpc.dbrman.desktop.util.UI;
import net.thevpc.nsql.CnxInfo;
import net.thevpc.nsql.SqlDialect;
import net.thevpc.nsql.UncheckedSQLException;
import net.thevpc.nsql.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class WdbrmanPanelConnexionPanel extends JPanel {
    private final JLabel dbLabel;
    JTextField hostField = new JTextField();
    JSpinner portField = new JSpinner(new SpinnerNumberModel());
    JTextField loginField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JLabel loginLabel = new JLabel("Login");
    JLabel passwordLabel = new JLabel("Mot de Passe");
    JCheckBox integratedSecurity = new JCheckBox("Sécurité Integrée");
    JComboBox serverType = new JComboBox(new Object[0]);
    JComboBox dbList = new JComboBox(new Object[0]);
    Properties conf;
    DatabaseId selectedDatabase;
    PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    List<ConnexionStatusListener> connexionStatusListeners = new ArrayList<>();
    private String currentConnexionAttemptId;

    public WdbrmanPanelConnexionPanel() {
        super(new GridBagLayout());
        int line = 0;
        dbLabel = new JLabel("Database");

        serverType = new JComboBox(
                Arrays.stream(SqlDialect.values()).map(x -> x.name()).toArray()
        );
        add(new JLabel("Type de Serveur"), GBC.of(0, line).fillHorizontal().anchorWest().insets(3).build());
        add(serverType, GBC.of(1, line++).fillHorizontal().anchorWest().insets(3).weightx(2).colspanReminder().build());

        add(new JLabel("Adresse"), GBC.of(0, line).fillHorizontal().anchorWest().insets(3).build());
        add(hostField, GBC.of(1, line).fillHorizontal().anchorWest().insets(3).weightx(2).colspan(2).build());
        add(new JLabel("Port"), GBC.of(3, line).fillHorizontal().anchorWest().insets(3).build());
        add(portField, GBC.of(4, line++).fillHorizontal().anchorWest().insets(3).weightx(2).build());

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


    public boolean acceptTable(TableId tid, DatabaseDriver d) {
        DatabaseId catalogId = selectedDatabaseId();

//        if (schemaId.getSchemaName() != null) {
//            if (!Objects.equals(tid.getSchemaName(), schemaId.getSchemaName())) {
//                return false;
//            }
//        }
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


    public CnxInfo cnxInfo() {
        Object value = portField.getValue();
        String dbName = null;
        String dbType = serverType.getSelectedItem() == null ? SqlDialect.MSSQLSERVER.name() : (String) serverType.getSelectedItem();

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
//                .setPassword("Rombatakaya")
                .setIntegrationSecurity(integratedSecurity.isSelected());
        return c;
    }

    void onChangeServerConnexion() {
        UI.async(() -> {
            try (DatabaseDriver d = createDriver()) {
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
            }catch (Exception ex){
                System.err.println(ex);
            }
        });
    }

    private void onChangeDatabase(DatabaseId item) {
        DatabaseId old = selectedDatabase;
        selectedDatabase = item;
        pcs.firePropertyChange("database", old, item);
        try (DatabaseDriver d = createDriver()) {
            updateTablesCount();
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    public DatabaseDriver createDriver() {
        pcs.firePropertyChange("connexion.status", null, "start");
        CnxInfo cnxInfo = null;
        String currentConnexionAttemptId=UUID.randomUUID().toString();
        try {
            this.currentConnexionAttemptId=currentConnexionAttemptId;
            cnxInfo = cnxInfo();
            for (ConnexionStatusListener listener : connexionStatusListeners) {
                listener.onConnectionCheckStart(cnxInfo);
            }
            DatabaseDriver r = DatabaseDriverFactories.createDatabaseDriver(cnxInfo);
            for (ConnexionStatusListener listener : connexionStatusListeners) {
                listener.onConnectionSuccess(cnxInfo);
            }
            return r;
        } catch (RuntimeException e) {
            Throwable ee = e;
            if (ee instanceof UncheckedSQLException) {
                ee = ee.getCause();
            }
            //ignore alder attempts!
            if(Objects.equals(currentConnexionAttemptId,this.currentConnexionAttemptId)) {
                for (ConnexionStatusListener listener : connexionStatusListeners) {
                    listener.onConnectionFailure(cnxInfo, ee);
                }
            }
            throw e;
        }
    }

    public void checkConnexion() {
        UI.async(() -> {
            try (DatabaseDriver d = createDriver()) {

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
        try (DatabaseDriver d = createDriver()) {
            DatabaseId databaseId = selectedDatabaseId();
            for (CatalogHeader catalog : d.getConnection().getCatalogs()) {
                System.out.println("CATALOG : "+catalog);
                for (SchemaHeader schema : d.getConnection().getSchemas(catalog.toCatalogId())) {
                    System.out.println("\tSCHEMA : "+schema);
                }
            }
            List<TableHeader> tables = d.getConnection().getAnyTables(databaseId).stream().filter(x -> x.isTable()
                    && !d.getConnection().isSpecialTable(x.toTableId())
            ).collect(Collectors.toList());
            List<TableHeader> filtered = tables.stream().filter(x -> acceptTable(x.toTableId(), d)).collect(Collectors.toList());
            System.out.println("updateTablesCount : " +
                    databaseId + " : " +
                    filtered.size() + " : " + filtered);
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    public void loadConf(Properties conf) {
        this.conf = conf;
        if (conf.getProperty("serverType") != null) {
            try {
                serverType.setSelectedItem(
                        SqlDialect.valueOf(conf.getProperty("serverType"))
                );
                pcs.firePropertyChange("serverType", null, serverType.getSelectedItem());
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

        void onConnectionSuccess(CnxInfo info);

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
            Object value=null;
            switch (name){
                case "login":{
                    value= loginField.getText();
                    break;
                }
                case "password":{
                    value=new String(passwordField.getPassword());
                    break;
                }
                case "host":{
                    value= hostField.getText();
                    break;
                }
                case "port":{
                    value= portField.getValue();
                    break;
                }
            }
            pcs.firePropertyChange(name, null, value);
        }
    }
}
