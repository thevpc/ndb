package net.thevpc.dbrman.desktop.panels;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class WdbrmanPanel extends JPanel {

    WdbrmanPanelExportPanel exportPanel;
    public WdbrmanPanel() {
        super(new BorderLayout());
        JTabbedPane p=new JTabbedPane();
//        p.addTab("Import",new WdbrmanPanelImportPanel());
        WdbrmanPanelExportPanel exportPanel = new WdbrmanPanelExportPanel();
        p.addTab("Export", exportPanel);
        add(p);
    }

    public void loadConf(File conf) {
        Properties p=new Properties();
        try(FileReader r=new FileReader(conf)){
            p.load(r);
        }catch (IOException ex){
            throw new RuntimeException(ex);
        }
        exportPanel.loadConf(p);
    }
}
