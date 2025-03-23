package net.thevpc.ndb.desktop.panels;

import net.thevpc.ndb.desktop.NDdbInfo;
import net.thevpc.ndb.desktop.util.GBC;
import net.thevpc.ndb.desktop.util.UI;
import net.thevpc.nuts.util.NMsg;

import javax.swing.*;
import java.awt.*;

public class ProgressPanel extends JPanel {
    JLabel statusLabel = new JLabel();
    JLabel percentLabel = new JLabel();
    Box statusBar = Box.createHorizontalBox();
    JProgressBar progressBar = new JProgressBar();
    int statusProgress = -1;
    NMsg statusMessage = null;
    public ProgressPanel() {
        super(new GridBagLayout());
        statusBar.add(Box.createHorizontalGlue());
        statusBar.add(statusLabel);
        statusBar.add(Box.createHorizontalStrut(10));
        percentLabel.setMinimumSize(new Dimension(50, 1));
        statusBar.add(percentLabel);
        statusBar.add(Box.createHorizontalStrut(10));
        statusBar.add(new JLabel(" | v" + NDdbInfo.VERSION));
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        int line=0;

        statusBar.setBorder(BorderFactory.createEtchedBorder());
        add(progressBar, GBC.of(0, line++).colspanReminder().anchorSouth().fillHorizontal().insets(3).weightx(2).weighty(1000).build());
        add(statusBar, GBC.of(0, line++).colspanReminder().anchorSouth().fillHorizontal().insets(3).weightx(2).build());
    }

    private boolean isIndeterminateProgress() {
        return statusProgress == Integer.MIN_VALUE;
    }

    public void updateStatus() {
        if (isIndeterminateProgress()) {
            updateStatus(-1, statusMessage);
        }
    }

    public void updateStatus(NMsg message) {
        updateStatus(statusProgress, message);
    }

    public void updateStatus(int progress, NMsg message) {
        statusProgress = progress;
        statusMessage = message;
        UI.withinGUI(() -> {
            if (isIndeterminateProgress()) {
                progressBar.setIndeterminate(true);
                statusLabel.setText(statusMessage == null ? "" : statusMessage.toString());
                percentLabel.setText("");
            } else {
                progressBar.setIndeterminate(false);
                if (statusProgress < 0 || statusProgress > 100) {
                    statusLabel.setText(statusMessage == null ? "" : statusMessage.toString());
                    percentLabel.setText("");
                } else {
                    statusLabel.setText(statusMessage == null ? "" : statusMessage.toString());
                    percentLabel.setText(statusProgress + "%");
                }
            }
        });
    }

}
