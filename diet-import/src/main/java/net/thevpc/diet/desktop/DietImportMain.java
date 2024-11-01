package net.thevpc.diet.desktop;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import net.thevpc.diet.desktop.panels.DietPanel;
import net.thevpc.diet.desktop.util.UI;
import net.thevpc.nsql.dump.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class DietImportMain {
    public static void main(String[] args) {
        UI.createFrame("Diet (Db Import)",args,(a)->new DietPanel(false,true));
    }
}