package net.thevpc.ndb.desktop;

import net.thevpc.ndb.desktop.panels.NDdbPanel;
import net.thevpc.ndb.desktop.util.UI;

public class NDdbMain {
    public static void main(String[] args) {
        UI.createFrame("NDdb (Db Import/Export Tool)",args,(a)->new NDdbPanel(true,true));
    }
}