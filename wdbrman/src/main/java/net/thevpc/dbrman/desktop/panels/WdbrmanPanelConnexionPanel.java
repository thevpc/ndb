package net.thevpc.dbrman.desktop.panels;

import net.thevpc.dbinfo.api.DatabaseDriver;
import net.thevpc.dbinfo.model.*;
import net.thevpc.dbinfo.util.DatabaseDriverFactories;
import net.thevpc.dbrman.desktop.util.GBC;
import net.thevpc.dbrman.desktop.util.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class WdbrmanPanelConnexionPanel extends JPanel {
    private final JLabel catalogLabel;
    private final JLabel schemaLabel;
    JTextField hostField = new JTextField();
    JSpinner portField = new JSpinner(new SpinnerNumberModel());
    JTextField loginField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JLabel loginLabel = new JLabel("Login");
    JLabel passwordLabel = new JLabel("Mot de Passe");
    JCheckBox integratedSecurity = new JCheckBox("Sécurité Integrée");
    JComboBox serverType = new JComboBox(new Object[0]);
    JComboBox schemaList = new JComboBox(new Object[0]);
    JComboBox catalogList = new JComboBox(new Object[0]);
    Properties conf;
    SchemaId selectedSchema;
    CatalogId selectedCatalog;
    String catalogTerm = "Catalog";
    String schemaTerm = "Schema";
    PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public WdbrmanPanelConnexionPanel() {
        super(new GridBagLayout());
        int line = 0;
        catalogLabel = new JLabel("Catalogue");
        schemaLabel = new JLabel("Schema");

        serverType = new JComboBox(
                Arrays.stream(DbType.values()).map(x -> x.name()).toArray()
        );
        add(new JLabel("Type de Serveur"), GBC.of(0, line).fillHorizontal().anchorWest().insets(3).build());
        add(serverType, GBC.of(1, line++).fillHorizontal().anchorWest().insets(3).weightx(2).build());

        add(new JLabel("Adresse"), GBC.of(0, line).fillHorizontal().anchorWest().insets(3).build());
        add(hostField, GBC.of(1, line++).fillHorizontal().anchorWest().insets(3).weightx(2).build());
        add(new JLabel("Port"), GBC.of(0, line).fillHorizontal().anchorWest().insets(3).build());
        add(portField, GBC.of(1, line++).fillHorizontal().anchorWest().insets(3).weightx(2).build());

        add(integratedSecurity, GBC.of(0, line++).fillHorizontal().anchorWest().insets(3).weightx(2).build());

        add(loginLabel, GBC.of(0, line).fillHorizontal().anchorWest().insets(3).build());
        add(loginField, GBC.of(1, line++).fillHorizontal().anchorWest().insets(3).weightx(2).build());

        add(passwordLabel, GBC.of(0, line).fillHorizontal().anchorWest().insets(3).build());
        add(passwordField, GBC.of(1, line++).fillHorizontal().anchorWest().insets(3).weightx(2).build());

        add(catalogLabel, GBC.of(0, line).fillHorizontal().anchorWest().insets(3).build());
        add(catalogList, GBC.of(1, line++).fillHorizontal().anchorWest().insets(3).weightx(2).build());

        add(schemaLabel, GBC.of(0, line).fillHorizontal().anchorWest().insets(3).build());
        add(schemaList, GBC.of(1, line++).fillHorizontal().anchorWest().insets(3).weightx(2).build());

        passwordField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {

            }

            @Override
            public void focusLost(FocusEvent e) {
                onChangeServerConnexion();
                pcs.firePropertyChange("password", null, new String(passwordField.getPassword()));
            }
        });
        loginField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {

            }

            @Override
            public void focusLost(FocusEvent e) {
                onChangeServerConnexion();
                pcs.firePropertyChange("login", null, loginField.getText());
            }
        });
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
        catalogList.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String s = value == null ? null : ((CatalogHeader) value).getCatalogName();
                return super.getListCellRendererComponent(list, s, index, isSelected, cellHasFocus);
            }
        });

        schemaList.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String s = value == null ? null : ((SchemaHeader) value).getSchemaName();
                return super.getListCellRendererComponent(list, s, index, isSelected, cellHasFocus);
            }
        });
        schemaList.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                SchemaHeader item = (SchemaHeader) e.getItem();
                onChangeSchema(item.toSchemaId());
            }
        });
        catalogList.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                CatalogHeader item = (CatalogHeader) e.getItem();
                onChangeCatalog(item == null ? null : item.toCatalogId());
            }
        });
        serverType.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                onChangeServerConnexion();
            }
        });
        onChangeServerConnexion();
    }


    public boolean acceptTable(TableId tid,DatabaseDriver d) {
        SchemaId schemaId = selectedSchemaId();
        if (schemaId == null) {
            return true;
        }
//        if (schemaId.getSchemaName() != null) {
//            if (!Objects.equals(tid.getSchemaName(), schemaId.getSchemaName())) {
//                return false;
//            }
//        }
        if (schemaId.getCatalogName() != null) {
            if (!Objects.equals(tid.getCatalogName(), schemaId.getCatalogName())) {
                return false;
            }
        }
        if(d.isSpecialTable(tid)) {
            return false;
        }
        return true;
    }


    public CnxInfo cnxInfo() {
        Object value = portField.getValue();
        String dbName=null;
        String dbType = serverType.getSelectedItem() == null ? DbType.SQLSERVER.name() : (String) serverType.getSelectedItem();

        {
            SchemaId schemaId = selectedSchemaId();
            String schemaName = schemaId == null ? "unknown" :
                    (schemaId.getSchemaName() != null && schemaId.getCatalogName() != null) ? (schemaId.getCatalogName() + "." + schemaId.getSchemaName()) :
                            (schemaId.getSchemaName() != null) ? (schemaId.getSchemaName()) :
                                    (schemaId.getCatalogName() != null) ? (schemaId.getCatalogName()) :
                                            "unknown";
            if (
                    DbType.SQLSERVER.name().equals(dbType)
                            || DbType.JTDS_SQLSERVER.name().equals(dbType)
            ) {
                if (schemaId != null) {
                    dbName=(schemaId.getCatalogName());
                }
            }
        }
        CnxInfo c = new CnxInfo()
//                        .setType("postgresql")
//                        .setUser("postgres")
//                        .setPassword("postgres")

                .setType(dbType)
                .setDbName(dbName)
//                        .setHost("192.168.1.22")
                .setUser(loginField.getText())
                .setPassword(new String(passwordField.getPassword()))
                .setHost(hostField.getText())
                .setPort(value == null ? null : value.toString())
//                .setPassword("Rombatakaya")
                .setIntegrationSecurity(true);
        return c;
    }

    void onChangeServerConnexion() {
        UI.async(() -> {
            try (DatabaseDriver d = createDriver()) {
                List<CatalogHeader> catalogs = d.getCatalogs();
                catalogs.sort(Comparator.comparing(x -> x.getCatalogName()));
                UI.withinGUI(() -> {
                    catalogList.setModel(new DefaultComboBoxModel(
                            catalogs.toArray(new Object[0])
                    ));
                    if (selectedCatalog == null) {
                        CatalogHeader v = (CatalogHeader) catalogList.getSelectedItem();
                        selectedCatalog = v == null ? null : v.toCatalogId();
                    }
                    onChangeCatalog(selectedCatalog);
                });
            }
        });
    }

    private void onChangeSchema(SchemaId item) {
        SchemaId old = selectedSchema;
        selectedSchema = item;
        pcs.firePropertyChange("schema", old, item);
        updateTablesCount();
    }

    private void onChangeCatalog(CatalogId item) {
        CatalogId old = selectedCatalog;
        selectedCatalog = item;
        pcs.firePropertyChange("catalog", old, item);
        updateTablesCount();

        try (DatabaseDriver d = createDriver()) {

            String catalogTerm = null;
            try {
                catalogTerm = d.getConnection().getMetaData().getCatalogTerm();
            } catch (SQLException e) {
                //
            }
            if (catalogTerm != null) {
                this.catalogTerm = catalogTerm;
                catalogLabel.setText(capitalize(catalogTerm));
            }

            String schemaTerm = null;
            try {
                schemaTerm = d.getConnection().getMetaData().getSchemaTerm();
            } catch (SQLException e) {
                //
            }
            if (schemaTerm != null) {
                this.schemaTerm = schemaTerm;
                schemaLabel.setText(capitalize(schemaTerm));
            }

            List<SchemaHeader> schemas = d.getSchemas(selectedCatalog == null ? null : selectedCatalog.getCatalogName());
            schemas.sort(Comparator.comparing(x -> x.getSchemaName()));
            UI.withinGUI(() -> {
                schemaList.setModel(new DefaultComboBoxModel(
                        schemas.toArray(new SchemaHeader[0])
                ));
                if (selectedSchema == null) {
                    SchemaHeader v = (SchemaHeader) schemaList.getSelectedItem();
                    selectedSchema = v == null ? null : v.toSchemaId();
                    onChangeSchema(selectedSchema);
                }
            });
        }
    }

    public DatabaseDriver createDriver() {
        pcs.firePropertyChange("connexion.status", null, "start");
        try {
            DatabaseDriver r = DatabaseDriverFactories.createDatabaseDriver(cnxInfo());
            pcs.firePropertyChange("connexion.status", null, "success");
            return r;
        } catch (RuntimeException e) {
            pcs.firePropertyChange("connexion.status", null, "failure");
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

    public SchemaId selectedSchemaId() {
        if (selectedSchema != null) {
            return selectedSchema;
        }
        if (selectedCatalog != null) {
            return new SchemaId(selectedCatalog.getCatalogName(), null);
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
            SchemaId schemaId = selectedSchemaId();
            List<TableHeader> tables = d.getAnyTables(schemaId).stream().filter(x -> x.isTable()
                    && !d.isSpecialTable(x.toTableId())
            ).collect(Collectors.toList());
            List<TableHeader> filtered = tables.stream().filter(x -> acceptTable(x.toTableId(),d)).collect(Collectors.toList());
            System.out.println(
                    schemaId + " : " +
                            filtered.size() + " : " + filtered);
        }
    }

    public void loadConf(Properties conf) {
        this.conf = conf;
        if (conf.getProperty("serverType") != null) {
            try {
                serverType.setSelectedItem(
                        DbType.valueOf(conf.getProperty("serverType"))
                );
                pcs.firePropertyChange("serverType", null, serverType.getSelectedItem());
            } catch (Exception ex) {
                //
            }
        }
        onChangeServerConnexion();
    }

    public JLabel getCatalogLabel() {
        return catalogLabel;
    }

    public JLabel getSchemaLabel() {
        return schemaLabel;
    }

    public JCheckBox getIntegratedSecurity() {
        return integratedSecurity;
    }

    public JComboBox getServerType() {
        return serverType;
    }

    public JComboBox getSchemaList() {
        return schemaList;
    }

    public JComboBox getCatalogList() {
        return catalogList;
    }

    public Properties getConf() {
        return conf;
    }

    public SchemaId getSelectedSchema() {
        return selectedSchema;
    }

    public CatalogId getSelectedCatalog() {
        return selectedCatalog;
    }

    public String getCatalogTerm() {
        return catalogTerm;
    }

    public String getSchemaTerm() {
        return schemaTerm;
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
}
