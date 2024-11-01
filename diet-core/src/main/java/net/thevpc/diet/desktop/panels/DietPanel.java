package net.thevpc.diet.desktop.panels;

import net.thevpc.nsql.dump.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class DietPanel extends JPanel {

    DietExportPanel exportPanel;
    DietImportPanel importPanel;
    public DietPanel(boolean exportImport,boolean withImport) {
        super(new BorderLayout());
        JTabbedPane p=new JTabbedPane();
//        p.addTab("Import",new WdbrmanPanelImportPanel());
        if(exportImport) {
            p.addTab("Export", exportPanel=new DietExportPanel());
        }
        if(withImport) {
            p.addTab("Import", importPanel=new DietImportPanel());
        }
        add(p);
        reloadConfig();
    }

    private void reloadConfig(){
        File conf=new File(Utils.getBinFile(),"ini.conf");
        if(conf.exists()){
            this.loadConf(conf);
        }
    }

    public void loadConf(File conf) {
        Properties p=new Properties();
        try(FileReader r=new FileReader(conf)){
            p.load(r);
        }catch (IOException ex){
            throw new RuntimeException(ex);
        }
        if(exportPanel!=null) {
            exportPanel.loadConf(p);
        }
        if(importPanel!=null) {
            importPanel.loadConf(p);
        }
    }
}
