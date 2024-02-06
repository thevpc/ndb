package net.thevpc.dbrman.desktop;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import net.thevpc.dbrman.desktop.panels.WdbrmanPanel;
import net.thevpc.dbrman.desktop.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        FlatMacDarkLaf.setup();
        try {
            Utils.loadSharedLibrary("mssql-jdbc_auth-12.4.2");
        }catch (UnsatisfiedLinkError | Exception ex) {
            JOptionPane.showMessageDialog(null, "Impossible de charger la librairie native");
        }
        JFrame frame = new JFrame("WDbrman -v 1.1");
        WdbrmanPanel contentPane = new WdbrmanPanel();

        File conf=new File(Utils.getBinFile(),"ini.conf");
        if(conf.exists()){
            contentPane.loadConf(conf);
        }
        frame.setContentPane(contentPane);
        frame.setMinimumSize(new Dimension(600, 400));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
    }
}