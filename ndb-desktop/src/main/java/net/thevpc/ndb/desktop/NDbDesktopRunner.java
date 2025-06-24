package net.thevpc.ndb.desktop;

import net.thevpc.ndb.desktop.panels.NDdbPanel;
import net.thevpc.ndb.desktop.util.UI;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementParser;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.UncheckedException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class NDbDesktopRunner implements Runnable {
    @Override
    public void run() {
        NElement conf = null;
        NPath confPath = NPath.of("ndb.conf.tson");
        if (!confPath.isRegularFile()) {
            conf = NElementParser.ofTson().parse(confPath);
        }
        if (conf == null) {
            conf = NElement.ofObject();
        } else if (!conf.isAnyObject()) {
            conf = conf.wrapIntoObject();
        }
        NObjectElement confObj = conf.asObject().get();
        boolean withExport = confObj.getBooleanValue("export").orElse(true);
        boolean withImport = confObj.getBooleanValue("import").orElse(true);
        JComponent contentPane = new NDdbPanel(
                withExport,
                withImport
        );
        if (!withImport && !withExport) {
            withExport = true;
        }
        String title;
        if (withImport && !withExport) {
            title="NDdb (Db Import)";
        }else if(!withImport && withExport){
            title="NDdb (Export Tool)";
        } else {
            title = "NDdb (Db Import/Export Tool)";
        }
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame(title+" v" + NDdbInfo.VERSION);
                frame.setIconImage(ImageIO.read(UI.class.getResource("/database-svgrepo-com.png")));
                frame.setContentPane(contentPane);
                frame.setMinimumSize(new Dimension(600, 400));
                frame.setVisible(true);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
            } catch (IOException ex) {
                throw new UncheckedException(ex);
            }
        });
    }
}
