package net.thevpc.ndb.desktop;

import net.thevpc.ndb.desktop.panels.NDdbPanel;
import net.thevpc.ndb.desktop.util.UI;

public class NDdbExportMain {
    public static void main(String[] args) {
        UI.createFrame("NDdb (Db Export)",args,(a)->new NDdbPanel(true,false));
    }
}