package net.thevpc.dbrman.util;

import java.io.File;
import java.net.URL;

public class Utils {
    public static boolean isOSWindows(){
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
    public static File getBinFile(){
        URL location = Utils.class.getProtectionDomain().getCodeSource().getLocation();
        String path = location.getPath();
        if(isOSWindows() && path.startsWith("/")){
            path=path.substring(1).replace("/","\\");
        }
        return new File(path);
    }

    public static void loadSharedLibrary(String lib) {
        for (String s : new String[]{
                Utils.getBinFile().toString(),
                System.getProperty("user.home"),
                System.getProperty("user.dir"),
        }) {
            File base = new File(s);
            if (base.exists()) {
                if (base.isFile()) {
                    base = base.getParentFile();
                }
                String ext=isOSWindows()?".x64.dll":".x64.so";
                File file = new File(base, lib+ext);
                System.out.println("Look for "+file);
                if (file.exists()) {
                    try {
                        System.load(file.toString());
                        return;
                    } catch (UnsatisfiedLinkError e) {
                        //
                    }
                }
            }
        }
        throw new UnsatisfiedLinkError(lib);
    }
}
