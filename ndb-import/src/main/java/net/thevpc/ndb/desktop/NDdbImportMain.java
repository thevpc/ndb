package net.thevpc.ndb.desktop;

import net.thevpc.ndb.desktop.panels.NDdbPanel;
import net.thevpc.ndb.desktop.util.UI;

public class NDdbImportMain {
    public static void main(String[] args) {
        UI.createFrame("NDdb (Db Import)",args,(a)->new NDdbPanel(false,true));
    }
}