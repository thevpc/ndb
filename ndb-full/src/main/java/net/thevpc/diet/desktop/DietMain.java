package net.thevpc.diet.desktop;

import net.thevpc.diet.desktop.panels.DietPanel;
import net.thevpc.diet.desktop.util.UI;

public class DietMain {
    public static void main(String[] args) {
        UI.createFrame("Diet (Db Import/Export Tool)",args,(a)->new DietPanel(true,true));
    }
}