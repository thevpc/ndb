/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.vio2.util;

import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Pattern;

/**
 * @author vpc
 */
public class StringUtils {

    public static boolean isBlank(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static String litString(Object s) {
        if (s == null) {
            return "null";
        }
        if (
                s instanceof Number
                        || s instanceof Boolean
        ) {
            return String.valueOf(s);
        }
        if (
                s instanceof Reader
                        || s instanceof char[]
        ) {
            return "char[...]";
        }
        if (
                s instanceof InputStream
                        || s instanceof byte[]
        ) {
            return "byte[...]";
        }
        if (s instanceof String) {
            StringBuilder sb = new StringBuilder();
            for (char c : s.toString().toCharArray()) {
                switch (c) {
                    case '\\':
                    case '\"': {
                        sb.append('\\').append(c);
                        break;
                    }
                    case '\n': {
                        sb.append('\\').append('n');
                        break;
                    }
                    case '\r': {
                        sb.append('\\').append('r');
                        break;
                    }
                    default: {
                        sb.append(c);
                    }
                }
            }
            return "\"" + sb.toString() + "\"";
        }
        return "\"" + s + "\"";
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

    public static String emptyIfNull(String s) {
        if (s == null) {
            return "";
        }
        return s;
    }

    public static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        if (s.isEmpty()) {
            return null;
        }
        return s;
    }
}
