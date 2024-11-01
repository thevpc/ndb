package net.thevpc.diet.desktop.util;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import net.thevpc.diet.desktop.DietInfo;
import net.thevpc.diet.desktop.panels.DietPanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.*;

public class UI {
    public static void async(Runnable r) {
        new Thread(r).start();
    }

    public static void prepareArgs(String[] args) {
        prepareLog(args);
        prepareUI(args);
    }

    public static void prepareLog(String[] args)  {
        Logger logger = Logger.getLogger("net.thevpc");
        Level level = Level.ALL;
        logger.setLevel(level);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(CustomFormatter.ofOneLine());
        consoleHandler.setLevel(level);
        File base = new File(".");
        new File(base, "log").mkdirs();
        try {
            System.err.println("loggin to "+new File(base, "log/log.log").getCanonicalPath());
        } catch (IOException e) {
            System.err.println("loggin to "+new File(base, "log/log.log").getAbsolutePath());
        }
        FileHandler fileHandler = null;
        try {
            fileHandler = new FileHandler(base.getPath() + "/log/log.log", 1024 * 1024 * 1025, 5, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileHandler.setFormatter(CustomFormatter.ofOneLine());
        fileHandler.setLevel(level);

        logger.addHandler(consoleHandler);
        logger.addHandler(fileHandler);
        /// jest test!!
        logger.log(Level.FINEST, "test");
    }

    public static void prepareUI(String[] args) {
        int scale = 0;
        for (String arg : args) {
            if (arg.equals("--scale")) {
                scale = 2;
            }
        }
        if (scale != 0) {
            System.setProperty("sun.java2d.uiScale", String.valueOf(scale));
        }
        FlatMacDarkLaf.setup();
    }

    public static void withinGUI(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(r);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void createFrame(String title,String[] args, Function<String[],JComponent> pane) {
        prepareArgs(args);
        JComponent contentPane = pane.apply(args);
        SwingUtilities.invokeLater(()->{
            JFrame frame = new JFrame(title+" v"+ DietInfo.VERSION);
            frame.setContentPane(contentPane);
            frame.setMinimumSize(new Dimension(600, 400));
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
        });
    }
}
