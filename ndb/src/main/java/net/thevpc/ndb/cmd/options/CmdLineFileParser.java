//package net.thevpc.ndb.cmd.options;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.StringReader;
//import java.io.UncheckedIOException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class CmdLineFileParser {
//    public static String[] parseArgsFromLine(String line) {
//        BufferedReader br = new BufferedReader(new StringReader(line));
//        List<String> all = new ArrayList<>();
//        try {
//            while (true) {
//                String y = readOne(br);
//                if (y != null) {
//                    all.add(y);
//                } else {
//                    return all.toArray(new String[0]);
//                }
//            }
//        } catch (IOException e) {
//            throw new UncheckedIOException(e);
//        }
//    }
//
//    private static String readEscapedWord(BufferedReader br, char c) throws IOException {
//        StringBuilder sb = new StringBuilder();
//        while (true) {
//            if (Character.isWhitespace(c)) {
//                return sb.toString();
//            } else {
//                if (c == '\\') {
//                    int u = br.read();
//                    if (u == -1) {
//                        sb.append('\\');
//                        return sb.toString();
//                    }
//                    c = (char) u;
//                    sb.append(c);
//                } else {
//                    sb.append(c);
//                }
//            }
//            int u = br.read();
//            if (u == -1) {
//                return sb.toString();
//            }
//            c = (char) u;
//        }
//    }
//
//    private static String readOne(BufferedReader br) throws IOException {
//        int u = 0;
//        u = br.read();
//        if (u == -1) {
//            return null;
//        }
//        char c = (char) u;
//        if (Character.isWhitespace(c)) {
//            u = skipWhites(br);
//            if (u == -1) {
//                return null;
//            }
//            c = (char) u;
//        }
//        switch (c) {
//            case '\"': {
//                return readOpenedDblQuoted(br);
//            }
//            case '\'': {
//                return readOpenedSimplQuoted(br);
//            }
//            default: {
//                return readEscapedWord(br, c);
//            }
//        }
//    }
//
//    private static String readOpenedSimplQuoted(BufferedReader br) throws IOException {
//        StringBuilder sb = new StringBuilder();
//        while (true) {
//            int u = br.read();
//            if (u == -1) {
//                return sb.toString();
//            }
//            char c = (char) u;
//            switch (c) {
//                case '\'': {
//                    return sb.toString();
//                }
//                case '\\': {
//                    u = br.read();
//                    if (u == -1) {
//                        sb.append('\\');
//                        return sb.toString();
//                    }
//                    c = (char) u;
//                    switch (c) {
//                        case '\'':
//                        case '\\': {
//                            sb.append(c);
//                            break;
//                        }
//                        default: {
//                            sb.append('\\');
//                            sb.append(c);
//                        }
//                    }
//                }
//                default: {
//                    sb.append(c);
//                }
//            }
//        }
//    }
//
//    private static String readOpenedDblQuoted(BufferedReader br) throws IOException {
//        StringBuilder sb = new StringBuilder();
//        while (true) {
//            int u = br.read();
//            if (u == -1) {
//                return sb.toString();
//            }
//            char c = (char) u;
//            switch (c) {
//                case '\"': {
//                    return sb.toString();
//                }
//                case '\\': {
//                    u = br.read();
//                    if (u == -1) {
//                        sb.append('\\');
//                        return sb.toString();
//                    }
//                    c = (char) u;
//                    switch (c) {
//                        case '\"':
//                        case '\\': {
//                            sb.append(c);
//                            break;
//                        }
//                        default: {
//                            sb.append('\\');
//                            sb.append(c);
//                        }
//                    }
//                }
//                default: {
//                    sb.append(c);
//                }
//            }
//        }
//    }
//
//    private static int skipWhites(BufferedReader br) throws IOException {
//        while (true) {
//            int u = br.read();
//            if (u == -1) {
//                return -1;
//            }
//            char c = (char) u;
//            if (Character.isWhitespace(c)) {
//                while (true) {
//                    u = br.read();
//                    if (u == -1) {
//                        return -1;
//                    }
//                    c = (char) u;
//                    if (!Character.isWhitespace(c)) {
//                        return c;
//                    }
//                }
//            } else {
//                return c;
//            }
//        }
//    }
//
//}
