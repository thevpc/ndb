package net.thevpc.nsql.dump.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class NameFilter {
    public List<String> include = new ArrayList<>();
    public List<String> exclude = new ArrayList<>();

    public List<String> getInclude() {
        return include;
    }

    public NameFilter setInclude(List<String> include) {
        this.include = include;
        return this;
    }

    public List<String> getExclude() {
        return exclude;
    }

    public NameFilter setExclude(List<String> exclude) {
        this.exclude = exclude;
        return this;
    }

    public NameFilter add(String glob) {
        if (glob != null && glob.length() > 0) {
            if (glob.startsWith("-")) {
                if (exclude == null) {
                    exclude = new ArrayList<>();
                }
                exclude.add(glob.substring(1));
            } else if (glob.startsWith("+")) {
                if (exclude == null) {
                    include = new ArrayList<>();
                }
                include.add(glob.substring(1));
            } else {
                if (exclude == null) {
                    include = new ArrayList<>();
                }
                include.add(glob);
            }
        }
        return this;
    }

    public Predicate<String> asPredicate() {
        List<Pattern> sIncludeTables = new ArrayList<>();
        List<Pattern> sExcludeTables = new ArrayList<>();
        if (this.include != null) {
            for (String ss : this.include) {
                if (ss != null) {
                    for (String s : ss.split("[, ]")) {
                        if (s.length() > 0) {
                            sIncludeTables.add(Utils.glob(s,false));
                        }
                    }
                }
            }
        }
        if (this.exclude != null) {
            for (String ss : this.exclude) {
                if (ss != null) {
                    for (String s : ss.split("[, ]")) {
                        if (s.length() > 0) {
                            sExcludeTables.add(Utils.glob(s,false));
                        }
                    }
                }
            }
        }
        return x -> {
            if (sExcludeTables.isEmpty() && sIncludeTables.isEmpty()) {
                return true;
            }
            if (!sExcludeTables.isEmpty()) {
                for (Pattern excludeTable : sExcludeTables) {
                    if (excludeTable.matcher(x).matches()) {
                        return false;
                    }
                }
            }
            if (!sIncludeTables.isEmpty()) {
                for (Pattern excludeTable : sIncludeTables) {
                    if (excludeTable.matcher(x).matches()) {
                        return true;
                    }
                }
                return false;
            }
            return true;
        };
    }
}
