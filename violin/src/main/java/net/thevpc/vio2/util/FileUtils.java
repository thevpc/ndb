package net.thevpc.vio2.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class FileUtils {
    public static File[] expandExistingFiles(String sFile, String extension) {
        File file = new File(sFile);
        List<File> allFiles = new ArrayList<>();
        if (file.isDirectory()) {
            File[] files = file.listFiles(x -> x.isFile() && (extension == null || x.getName().endsWith(extension)));
            if (files != null) {
                allFiles.addAll(Arrays.asList(files));
            }
        } else if (file.getName().indexOf("*") >= 0) {
            File parentFile = file.getParentFile();
            if (parentFile == null) {
                parentFile = new File(".");
            }
            Pattern glob = StringUtils.glob(file.getName(), true);
            File[] files = parentFile.listFiles(x -> {
                return x.isFile() && glob.matcher(x.getName()).matches();
            });
            if (files != null) {
                allFiles.addAll(Arrays.asList(files));
            }
        } else {
            allFiles.add(file);
        }
        return allFiles.toArray(new File[0]);
    }
}
