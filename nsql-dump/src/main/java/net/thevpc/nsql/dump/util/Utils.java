package net.thevpc.nsql.dump.util;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Utils {
    public static final <T> Stream<T> concatStreams(List<Stream<T>> all){
        if(all.isEmpty()){
            return (Stream)Collections.emptyList().stream();
        }
        Stream<T> s=all.get(0);
        for (int i = 1; i <all.size(); i++) {
            s=Stream.concat(s,all.get(i));
        }
        return s;
    }
    public static final <T> Stream<T> toStream(Iterator<T> sourceIterator){
        return  StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(sourceIterator, Spliterator.ORDERED),
                false);
    }

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

    public static Pattern glob(String s, boolean caseSensitive) {
        if (s == null || s.length() == 0) {
            return Pattern.compile(".*");
        }
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '*': {
                    sb.append(".*");
                    break;
                }
                case '?': {
                    sb.append(".");
                    break;
                }
                case '+':
                case '.':
                case '[':
                case ']':
                case '(':
                case ')': {
                    sb.append("\\").append(c);
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        if (caseSensitive) {
            return Pattern.compile(sb.toString());
        } else {
            return Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
        }
    }
}
