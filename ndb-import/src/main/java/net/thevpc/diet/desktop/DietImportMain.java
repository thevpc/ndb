package net.thevpc.diet.desktop;

import net.thevpc.diet.desktop.panels.DietPanel;
import net.thevpc.diet.desktop.util.UI;

public class DietImportMain {
    public static void main(String[] args) {
        UI.createFrame("Diet (Db Import)",args,(a)->new DietPanel(false,true));
    }
}