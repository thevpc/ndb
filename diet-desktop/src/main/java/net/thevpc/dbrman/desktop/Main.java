package net.thevpc.dbrman.desktop;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import net.thevpc.dbrman.desktop.panels.WdbrmanPanel;
import net.thevpc.dbrman.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Main {
    public static final String VERSION="1.2";
    public static void main(String[] args) {
        FlatMacDarkLaf.setup();
        JFrame frame = new JFrame("Diet (Db Import/Export Tool) v"+VERSION);
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