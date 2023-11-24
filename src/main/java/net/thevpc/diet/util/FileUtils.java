package net.thevpc.diet.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class FileUtils {
    public static File[] expandFile(String sFile) {
        File file = new File(sFile);
        List<File> allFiles = new ArrayList<>();
        if (file.isDirectory()) {
            File[] files = file.listFiles(x -> x.isFile() && x.getName().endsWith(".dump"));
            if (files != null) {
                allFiles.addAll(Arrays.asList(files));
            }
        } else if (file.getName().indexOf("*") >= 0) {
            File parentFile = file.getParentFile();
            if (parentFile == null) {
                parentFile = new File(".");
            }
            Pattern glob = StringUtils.glob(file.getName());
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
