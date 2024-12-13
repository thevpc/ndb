package net.thevpc.diet.desktop;

import net.thevpc.diet.desktop.panels.DietPanel;
import net.thevpc.diet.desktop.util.UI;

public class DietExportMain {
    public static void main(String[] args) {
        UI.createFrame("Diet (Db Export)",args,(a)->new DietPanel(true,false));
    }
}