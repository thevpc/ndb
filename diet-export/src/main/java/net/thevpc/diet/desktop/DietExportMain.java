package net.thevpc.diet.desktop;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import net.thevpc.diet.desktop.panels.DietPanel;
import net.thevpc.diet.desktop.util.UI;
import net.thevpc.nsql.dump.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class DietExportMain {
    public static void main(String[] args) {
        UI.createFrame("Diet (Db Export)",args,(a)->new DietPanel(true,false));
    }
}