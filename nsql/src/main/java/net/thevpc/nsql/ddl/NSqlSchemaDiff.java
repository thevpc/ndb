package net.thevpc.nsql.ddl;

import net.thevpc.nsql.NQueryResult;
import net.thevpc.nsql.NSqlConnection;
import net.thevpc.nsql.NSqlRow;
import net.thevpc.nsql.UncheckedSqlException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NStringBuilder;
import net.thevpc.nuts.util.NStringUtils;

import java.sql.SQLException;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

public class NSqlSchemaDiff {

    // ---------- Data model ----------

    public interface ActionApplierContext {
        NSqlConnection connection();

        DiffAction action();
    }

    public interface ActionApplier {
        void apply(ActionApplierContext connection);
    }

    static class Column {
        String name;
        String type;
        boolean nullable;
        String newName;

        Column(String name, String type, boolean nullable) {
            this.name = name;
            this.type = type;
            this.nullable = nullable;
        }
    }

    static class ForeignKey {
        String constraintName;
        String column;
        String targetTable;

        ForeignKey(String constraintName, String column, String targetTable) {
            this.constraintName = constraintName;
            this.column = column;
            this.targetTable = targetTable;
        }
    }

    static class UniqueConstraint {
        String constraintName;
        String column;
        String newName;

        UniqueConstraint(String constraintName, String column) {
            this.constraintName = constraintName;
            this.column = column;
        }
    }

    public static class Table {
        String name;
        String newName;
        LinkedHashMap<String, Column> columns = new LinkedHashMap<>(); // key = lowercase name
        List<ForeignKey> foreignKeys = new ArrayList<>();
        List<UniqueConstraint> uniqueConstraints = new ArrayList<>();
        List<String> primaryKeyColumns = new ArrayList<>(); // lowercase names, in declared order
    }

    // ---------- Explicit rename knowledge (verified against this schema) ----------
    // These are 100% consistent across the whole schema (checked: every old name appears
    // in exactly 110 tables, every new name appears in exactly 110 tables).
    final Map<String, String> EXPLICIT_TABLE_RENAMES = new LinkedHashMap<>();
    final Map<String, String> EXPLICIT_COLUMN_RENAMES = new LinkedHashMap<>();
    final Map<String, Map<String, String>> EXPLICIT_COLUMN_RENAMES_PER_TABLE = new LinkedHashMap<>();

    public void addExplicitRenameTable(String oldName, String newName) {
        EXPLICIT_TABLE_RENAMES.put(oldName, newName);
    }

    public void addExplicitRenameColumns(String oldName, String newName) {
        EXPLICIT_COLUMN_RENAMES.put(oldName, newName);
    }

    public void addExplicitRenameColumn(String table, String oldName, String newName) {
        EXPLICIT_COLUMN_RENAMES_PER_TABLE.computeIfAbsent(table, s -> new LinkedHashMap<>()).put(oldName, newName);
    }

    // suffix -> replacement, applied per-table when a dropped column's transformed
    // name matches a genuinely added column in the same table (same as before, but now
    // generalized rather than hand listed one by one).
    final String[][] SUFFIX_RULES = {
            {"reftype", "ref_type"},
            {"shortname", "short_name"},
            {"longname", "long_name"},
    };

    // ---------- Parsing ----------

    public static LinkedHashMap<String, Table> parseTables(NPath path) {
        String content = path.readString();
        return parseTablesFromContentScript(content);
    }

    public static LinkedHashMap<String, Table> parseTablesFromContentScript(String content) {
        LinkedHashMap<String, Table> tables = new LinkedHashMap<>();

        // T-SQL batch separator: a line containing only "go"
        String[] blocks = Pattern.compile("(?i)\\n\\s*go\\s*\\n").split(content);

        Pattern createTablePattern = Pattern.compile(
                "create\\s+table\\s+([A-Za-z0-9_.\\[\\]]+)\\s*\\((.*)\\)\\s*$",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

        for (String rawBlock : blocks) {
            String block = NStringUtils.strip(rawBlock);
            if (block.isEmpty()) continue;
            Matcher m = createTablePattern.matcher(block);
            if (!m.find()) continue;

            String tableName = stripBrackets(NStringUtils.strip(m.group(1)));
            String body = m.group(2);

            List<String> parts = splitTopLevel(body);

            Table table = new Table();
            table.name = tableName;

            for (String rawPart : parts) {
                String part = NStringUtils.strip(rawPart);
                if (part.isEmpty()) continue;
                String lower = part.toLowerCase(Locale.ROOT);

                if (lower.startsWith("primary key")) {
                    // table-level composite primary key: primary key (col1, col2)
                    Matcher pkm = Pattern.compile("primary\\s+key\\s*\\(([^)]*)\\)", Pattern.CASE_INSENSITIVE).matcher(part);
                    if (pkm.find()) {
                        for (String c : pkm.group(1).split(",")) {
                            table.primaryKeyColumns.add(stripBrackets(NStringUtils.strip(c)).toLowerCase(Locale.ROOT));
                        }
                    }
                    continue;
                }
                if (lower.startsWith("constraint") || lower.startsWith("foreign key") || lower.startsWith("unique") || lower.startsWith("check(")) {
                    // rare/unexpected table-level constraint not attached to a column - skip
                    continue;
                }

                // column definition: <name> <rest...>
                Matcher cm = Pattern.compile("([A-Za-z0-9_\\[\\]]+)\\s+(.*)", Pattern.DOTALL).matcher(part);
                if (!cm.find()) continue;
                String colName = stripBrackets(NStringUtils.strip(cm.group(1)));
                String rest = NStringUtils.strip(cm.group(2));
                String restNorm = rest.replaceAll("\\s+", " ");

                Matcher typeM = Pattern.compile("^([A-Za-z0-9_]+(?:\\s*\\(\\s*[^)]*\\))?)").matcher(restNorm);
                String colType = typeM.find() ? NStringUtils.strip(typeM.group(1)) : restNorm;

                boolean notNull = Pattern.compile("(?i)\\bnot\\s+null\\b").matcher(restNorm).find();
                boolean nullable = !notNull;

                String colKey = colName.toLowerCase(Locale.ROOT);
                table.columns.put(colKey, new Column(colName, colType, nullable));

                // single-column primary key (e.g. "id varchar(255) not null primary key")
                if (Pattern.compile("(?i)\\bprimary\\s+key\\b(?!\\s*\\()").matcher(restNorm).find()) {
                    table.primaryKeyColumns.add(colKey);
                }

                // one or more "constraint NAME references TABLE" / "constraint NAME unique"
                Matcher constraintM = Pattern.compile(
                        "(?i)constraint\\s+(\\S+)\\s+(references\\s+([A-Za-z0-9_.\\[\\]]+)|unique)"
                ).matcher(restNorm);
                while (constraintM.find()) {
                    String constraintName = constraintM.group(1);
                    String kind = constraintM.group(2);
                    if (kind.toLowerCase(Locale.ROOT).startsWith("references")) {
                        String targetTable = stripBrackets(constraintM.group(3));
                        table.foreignKeys.add(new ForeignKey(constraintName, colKey, targetTable));
                    } else {
                        table.uniqueConstraints.add(new UniqueConstraint(constraintName, colKey));
                    }
                }
            }

            tables.put(tableName.toLowerCase(Locale.ROOT), table);
        }
        return tables;
    }

    static String stripBrackets(String s) {
        return s.replace("[", "").replace("]", "");
    }

    /**
     * Split on top-level commas only, respecting parenthesis nesting.
     */
    static List<String> splitTopLevel(String body) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < body.length(); i++) {
            char ch = body.charAt(i);
            if (ch == '(') {
                depth++;
                cur.append(ch);
            } else if (ch == ')') {
                depth--;
                cur.append(ch);
            } else if (ch == ',' && depth == 0) {
                parts.add(cur.toString());
                cur.setLength(0);
            } else cur.append(ch);
        }
        if (cur.length() > 0) parts.add(cur.toString());
        return parts;
    }

    // ---------- DDL rendering for whole new/dropped tables ----------

    static String columnDisplayName(Table t, String key) {
        Column c = t.columns.get(key);
        return c != null ? c.name : key;
    }

    /**
     * Reconstruct a full CREATE TABLE statement from a parsed Table (used for tables only present in `new`).
     */
    static String renderCreateTable(Table t) {
        List<String> lines = new ArrayList<>();

        for (Column c : t.columns.values()) {
            String line = "    " + c.name + " " + c.type;
            if (!c.nullable) line += " not null";
            lines.add(line);
        }
        for (ForeignKey fk : t.foreignKeys) {
            lines.add(String.format("    constraint %s foreign key (%s) references %s",
                    fk.constraintName, columnDisplayName(t, fk.column), fk.targetTable));
        }
        for (UniqueConstraint uq : t.uniqueConstraints) {
            lines.add(String.format("    constraint %s unique (%s)",
                    uq.constraintName, columnDisplayName(t, uq.column)));
        }
        if (!t.primaryKeyColumns.isEmpty()) {
            List<String> pkNames = new ArrayList<>();
            for (String c : t.primaryKeyColumns) pkNames.add(columnDisplayName(t, c));
            lines.add("    primary key (" + String.join(", ", pkNames) + ")");
        }

        return "CREATE TABLE " + t.name + " (\n" + String.join(",\n", lines) + "\n);";
    }

    // ---------- Diff ----------

    public enum DiffActionType {
        TABLE_ADD,
        TABLE_RENAME,
        TABLE_DROP,
        TABLE_ALTER,
        COLUMN_RENAME,
        COLUMN_ALTER,
        COLUMN_DROP,
        COLUMN_ADD,
        UNIQUE_ADD,
        UNIQUE_DROP,
        FK_ADD,
        FK_DROP,
        COMMENT,
        WARNING,
    }

    public static class DiffAction {
        private final DiffActionType type;
        private final String ddl;
        private String oldTableName;
        private String oldColumnName;
        private String from;
        private String to;
        private ActionApplier applier;

        public DiffAction(DiffActionType type, String ddl) {
            this.type = type;
            this.ddl = ddl;
        }

        public DiffAction(DiffActionType type, String ddl, String from, String to) {
            this.type = type;
            this.ddl = ddl;
            this.from = from;
            this.to = to;
        }

        public DiffAction(DiffActionType type, String ddl, String from, String to, String oldTableName, String oldColumnName, ActionApplier applier) {
            this.type = type;
            this.ddl = ddl;
            this.from = from;
            this.to = to;
            this.oldTableName = oldTableName;
            this.oldColumnName = oldColumnName;
            this.applier = applier;
        }

        public void apply(NSqlConnection connection) {
            applier.apply(new ActionApplierContext() {
                @Override
                public NSqlConnection connection() {
                    return connection;
                }

                @Override
                public DiffAction action() {
                    return DiffAction.this;
                }
            });
        }

        public String oldTableName() {
            return oldTableName;
        }

        public String oldColumnName() {
            return oldColumnName;
        }

        public DiffActionType type() {
            return type;
        }

        public String ddl() {
            return ddl;
        }

        public String from() {
            return from;
        }

        public String to() {
            return to;
        }

        @Override
        public String toString() {
            return "DiffAction{" +
                    "type=" + type +
                    ", ddl='" + ddl + '\'' +
                    '}';
        }
    }

    public static class DiffResult {
        LinkedHashMap<String, Table> oldTables;
        LinkedHashMap<String, Table> newTables;
        List<DiffAction> actions = new ArrayList<>();

        List<String> droppedTables = new ArrayList<>();
        List<String> addedTables = new ArrayList<>();
        Map<String, String> globalRenamesUsed = new TreeMap<>();
        int addedCols = 0, droppedCols = 0, modifiedCols = 0, renamedCols = 0;
        int addedFks = 0, droppedFks = 0, addedUniques = 0, droppedUniques = 0;
        int renamedTables = 0;

        public List<DiffAction> actions() {
            return actions;
        }

        public List<String> droppedTables() {
            return droppedTables;
        }

        public List<String> addedTables() {
            return addedTables;
        }

        public Map<String, String> globalRenamesUsed() {
            return globalRenamesUsed;
        }

        public int addedCols() {
            return addedCols;
        }

        public int droppedCols() {
            return droppedCols;
        }

        public int modifiedCols() {
            return modifiedCols;
        }

        public int renamedCols() {
            return renamedCols;
        }

        public int addedFks() {
            return addedFks;
        }

        public int droppedFks() {
            return droppedFks;
        }

        public int addedUniques() {
            return addedUniques;
        }

        public int droppedUniques() {
            return droppedUniques;
        }

        public int renamedTables() {
            return renamedTables;
        }

        public String diffScript() {
            return String.join("\n", diffScriptList());
        }

        public List<DiffAction> diffActions() {
            List<DiffAction> actions = new ArrayList<>();
            actions.add(new DiffAction(DiffActionType.COMMENT, "-- Schema diff: ALTER statements (old -> new)"));
            actions.add(new DiffAction(DiffActionType.COMMENT, "-- Renamed tables (explicit): " + this.renamedTables()));
            actions.add(new DiffAction(DiffActionType.COMMENT, "-- Renamed columns (detected): " + this.renamedCols()));
            actions.add(new DiffAction(DiffActionType.COMMENT, "-- Added columns: " + this.addedCols()));
            actions.add(new DiffAction(DiffActionType.COMMENT, "-- Dropped columns: " + this.droppedCols()));
            actions.add(new DiffAction(DiffActionType.COMMENT, "-- Modified columns (type/nullability): " + this.modifiedCols()));
            actions.add(new DiffAction(DiffActionType.COMMENT, "-- Added FKs: " + this.addedFks() + " | Dropped FKs: " + this.droppedFks()));
            actions.add(new DiffAction(DiffActionType.COMMENT, "-- Added UNIQUE constraints: " + this.addedUniques() + " | Dropped UNIQUE constraints: " + this.droppedUniques()));

            actions.add(new DiffAction(DiffActionType.COMMENT, "\n-- ==== TABLE changes (drop / rename / add) ===="));
            this.actions().stream().filter(x -> x.type() == DiffActionType.FK_DROP).forEach(actions::add);
            this.actions().stream().filter(x -> x.type() == DiffActionType.UNIQUE_DROP).forEach(actions::add);
            this.actions().stream().filter(x -> x.type() == DiffActionType.COLUMN_DROP).forEach(actions::add);
            this.actions().stream().filter(x -> x.type() == DiffActionType.TABLE_DROP).forEach(actions::add);

            this.actions().stream().filter(x -> x.type() == DiffActionType.TABLE_RENAME).forEach(actions::add);
            this.actions().stream().filter(x -> x.type() == DiffActionType.TABLE_ADD).forEach(actions::add);
            actions.add(new DiffAction(DiffActionType.COMMENT, "\n-- ==== ADD / DROP / ALTER COLUMN ===="));
            this.actions().stream().filter(x -> x.type() == DiffActionType.COLUMN_ALTER).forEach(actions::add);
            this.actions().stream().filter(x -> x.type() == DiffActionType.COLUMN_RENAME).forEach(actions::add);
            this.actions().stream().filter(x -> x.type() == DiffActionType.COLUMN_ADD).forEach(actions::add);
            actions.add(new DiffAction(DiffActionType.COMMENT, "\n-- ==== FOREIGN KEY changes ===="));
            this.actions().stream().filter(x -> x.type() == DiffActionType.FK_ADD).forEach(actions::add);
            actions.add(new DiffAction(DiffActionType.COMMENT, "\n-- ==== UNIQUE constraint changes ===="));
            this.actions().stream().filter(x -> x.type() == DiffActionType.UNIQUE_ADD).forEach(actions::add);
            actions.add(new DiffAction(DiffActionType.COMMENT, "\n-- ==== PRIMARY KEY warnings (manual review needed - names not in DDL) ===="));
            this.actions().stream().filter(x -> x.type() == DiffActionType.WARNING).forEach(s -> actions.add(s));

            actions.add(new DiffAction(DiffActionType.COMMENT, "\n-- ==== Rename map applied (old_column -> new_column) ===="));
            for (Map.Entry<String, String> e : this.globalRenamesUsed().entrySet()) {
                actions.add(new DiffAction(DiffActionType.COMMENT, "-- " + e.getKey() + " -> " + e.getValue()));
            }
            return actions;
        }

        public List<String> diffScriptList() {
            return diffActions().stream().map(x -> {
                return x.ddl;
            }).collect(Collectors.toList());
        }

        public String diffStats() {
            NStringBuilder sb = new NStringBuilder();
            sb.println("Parsed old tables: " + oldTables.size());
            sb.println("Parsed new tables: " + newTables.size());
            sb.println("Renamed tables (explicit): " + this.renamedTables());
            sb.println("Renamed columns: " + this.renamedCols());
            sb.println("Added columns: " + this.addedCols() + " | Dropped columns: " + this.droppedCols());
            sb.println("Modified columns: " + this.modifiedCols());
            sb.println("Added FKs: " + this.addedFks() + " | Dropped FKs: " + this.droppedFks());
            sb.println("Added UNIQUE: " + this.addedUniques() + " | Dropped UNIQUE: " + this.droppedUniques());
            sb.println("PK warnings: " + this.actions().stream().filter(x -> x.type() == DiffActionType.WARNING).count());
            sb.println("Dropped tables: " + this.droppedTables().size());
            sb.println("Added tables: " + this.addedTables().size());
            return sb.toString();
        }
    }

    public DiffResult diff(LinkedHashMap<String, Table> oldTables, LinkedHashMap<String, Table> newTables) {
        DiffResult result = new DiffResult();
        result.oldTables = new LinkedHashMap<>(oldTables);
        result.newTables = new LinkedHashMap<>(newTables);

        Set<String> oldNames = oldTables.keySet();
        Set<String> newNames = newTables.keySet();

        // pairs of (oldKey, newKey) to diff as "the same table"
        List<String[]> tablePairs = new ArrayList<>();
        Set<String> matchedNewKeys = new HashSet<>();

        for (String t : oldNames) {
            if (newNames.contains(t)) {
                tablePairs.add(new String[]{t, t});
                matchedNewKeys.add(t);
            } else {
                String renamedTo = EXPLICIT_TABLE_RENAMES.get(t);
                if (renamedTo != null && newNames.contains(renamedTo)) {
                    tablePairs.add(new String[]{t, renamedTo});
                    matchedNewKeys.add(renamedTo);
                    oldTables.get(t).newName = newTables.get(renamedTo).name;
                    result.actions.add(new DiffAction(
                            DiffActionType.TABLE_RENAME,
                            String.format("EXEC sp_rename '%s', '%s';", oldTables.get(t).name, newTables.get(renamedTo).name),
                            oldTables.get(t).name,
                            newTables.get(renamedTo).name,
                            oldTables.get(t).name,
                            null,
                            cc -> cc.connection().executeUpdate(cc.action().ddl())
                    ));
                    result.renamedTables++;
                }
            }
        }
        tablePairs.sort((a, b) -> a[0].compareTo(b[0]));

        for (String t : oldNames) {
            boolean matched = newNames.contains(t) || EXPLICIT_TABLE_RENAMES.containsKey(t) && matchedNewKeys.contains(EXPLICIT_TABLE_RENAMES.get(t));
            if (!matched) {
                Table ot = oldTables.get(t);
                result.droppedTables.add(ot.name);
                result.actions.add(new DiffAction(
                        DiffActionType.TABLE_DROP,
                        "DROP TABLE " + ot.name + ";",
                        ot.name,
                        null,
                        oldTables.get(t).name,
                        null,
                        cc -> {
                            for (String dep : findDependentDropStatements(cc.connection(), cc.action().oldTableName(), null)) {
//                                System.out.println("Executing (dependency): " + dep);
                                cc.connection().executeUpdate(dep);
                            }
                            cc.connection().executeUpdate(cc.action().ddl());
                        }
                ));
            }
        }
        for (String t : newNames) {
            if (!matchedNewKeys.contains(t)) {
                Table nt = newTables.get(t);
                result.addedTables.add(nt.name);
                result.actions.add(new DiffAction(
                        DiffActionType.TABLE_ADD,
                        renderCreateTable(nt),
                        null,
                        nt.name,
                        null,
                        null,
                        cc -> cc.connection().executeUpdate(cc.action().ddl())
                ));
            }
        }
        Collections.sort(result.droppedTables);
        Collections.sort(result.addedTables);

        for (String[] pair : tablePairs) {
            String oldKey = pair[0], newKey = pair[1];
            Table oldT = oldTables.get(oldKey);
            Table newT = newTables.get(newKey);
            String realName = newT.name;

            Set<String> oldColNames = new LinkedHashSet<>(oldT.columns.keySet());
            Set<String> newColNames = new LinkedHashSet<>(newT.columns.keySet());

            List<String> added = new ArrayList<>(newColNames);
            added.removeAll(oldColNames);
            List<String> dropped = new ArrayList<>(oldColNames);
            dropped.removeAll(newColNames);
            List<String> common = new ArrayList<>(oldColNames);
            common.retainAll(newColNames);
            Collections.sort(added);
            Collections.sort(dropped);
            Collections.sort(common);

            // build per-table rename map: explicit per-table map first, then explicit
            // global map, then suffix-normalization auto-detection
            Map<String, String> explicitForTable = EXPLICIT_COLUMN_RENAMES_PER_TABLE.getOrDefault(oldKey, Collections.emptyMap());
            Map<String, String> renameMap = new LinkedHashMap<>();

            for (String dcol : dropped) {
                String candidate = explicitForTable.get(dcol);
                if (candidate != null) {
                    if(added.contains(candidate)) {
                        renameMap.put(dcol, candidate);
                    }else{
                        System.err.println("invalid match "+dcol+"->"+candidate+" for table "+oldKey);
                    }
                }
            }
            for (String dcol : dropped) {
                String candidate = EXPLICIT_COLUMN_RENAMES.get(dcol);
                if (candidate != null && added.contains(candidate)) {
                    renameMap.put(dcol, candidate);
                }
            }
            for (String dcol : dropped) {
                if (renameMap.containsKey(dcol)) continue;
                for (String[] rule : SUFFIX_RULES) {
                    if (dcol.endsWith(rule[0])) {
                        String candidate = dcol.substring(0, dcol.length() - rule[0].length()) + rule[1];
                        if (added.contains(candidate) && !renameMap.containsValue(candidate)) {
                            renameMap.put(dcol, candidate);
                            break;
                        }
                    }
                }
            }
            // after the three renameMap-populating loops, before the full-replace check:
            for (Map.Entry<String, String> e : explicitForTable.entrySet()) {
                String oldCol = e.getKey();
                String newCol = e.getValue();
                if (renameMap.containsKey(oldCol)) continue; // applied fine
                String reason;
                if (!dropped.contains(oldCol) && !oldColNames.contains(oldCol)) {
                    reason = "old column '" + oldCol + "' not found on table '" + oldKey + "' at all - check table key/column name";
                } else if (!dropped.contains(oldCol)) {
                    reason = "old column '" + oldCol + "' still exists unchanged in new schema (not in dropped set) - nothing to rename";
                } else if (!added.contains(newCol)) {
                    reason = "target column '" + newCol + "' not found in new-only columns (check spelling, or it may already exist as a common column)";
                } else {
                    reason = "unknown - candidate was already claimed by another rename pair";
                }
                result.actions.add(new DiffAction(DiffActionType.COMMENT,
                        "-- WARNING: explicit rename " + oldKey + "." + oldCol + " -> " + newCol
                                + " did NOT apply: " + reason));
            }


            // Full table replace: if literally nothing carries over (no common columns, no renames),
            // column-by-column ALTER is pointless and risky - it can transiently drop the table to
            // zero columns, which SQL Server refuses outright. Drop and recreate the whole table
            // instead. Both actions share oldTableName so a runtime gate (e.g. "only run on empty
            // tables") applies to them as a single atomic unit.
            if (common.isEmpty() && renameMap.isEmpty() && !oldT.columns.isEmpty()) {
                result.actions.add(new DiffAction(DiffActionType.COMMENT,
                        "-- " + realName + ": fully restructured (no surviving columns) - dropped and recreated instead of altered"));
                result.actions.add(new DiffAction(
                        DiffActionType.TABLE_DROP,
                        "DROP TABLE " + oldT.name + ";",
                        oldT.name,
                        null,
                        oldT.name,
                        null,
                        cc -> {
                            for (String dep : findDependentDropStatements(cc.connection(), cc.action().oldTableName(), null)) {
                                cc.connection().executeUpdate(dep);
                            }
                            cc.connection().executeUpdate(cc.action().ddl());
                        }
                ));
                result.actions.add(new DiffAction(
                        DiffActionType.TABLE_ADD,
                        renderCreateTable(newT),
                        null,
                        realName,
                        oldT.name, // same key as the DROP above, so both gate together at the call site
                        null,
                        cc -> cc.connection().executeUpdate(cc.action().ddl())
                ));
                continue;
            }



            List<String> effectiveAdded = new ArrayList<>(added);
            List<String> effectiveDropped = new ArrayList<>(dropped);
            for (Map.Entry<String, String> e : renameMap.entrySet()) {
                String oldColName = oldT.columns.get(e.getKey()).name;
                String newColName = newT.columns.get(e.getValue()).name;
                oldT.columns.get(e.getKey()).newName = newColName;
                result.actions.add(
                        new DiffAction(
                                DiffActionType.COLUMN_RENAME,
                                String.format("EXEC sp_rename '%s.%s', '%s', 'COLUMN';", realName, oldColName, newColName),
                                realName + "." + oldColName,
                                realName + "." + newColName,
                                realName,
                                oldColName,
                                cc -> {
                                    safeRenameColumn(cc.connection(),realName,oldColName,newColName);
                                }
                        )
                );
                effectiveDropped.remove(e.getKey());
                effectiveAdded.remove(e.getValue());
                result.renamedCols++;
                result.globalRenamesUsed.put(e.getKey(), e.getValue());
            }

            for (String c : effectiveAdded) {
                Column col = newT.columns.get(c);
                String nullClause = col.nullable ? "" : " NOT NULL";
                result.actions.add(
                        new DiffAction(
                                DiffActionType.COLUMN_ADD,
                                String.format(
                                        "ALTER TABLE %s ADD %s %s%s;", realName, col.name, col.type, nullClause),
                                null,
                                realName + "." + col.name,
                                realName,
                                null,
                                cc -> cc.connection().executeUpdate(cc.action().ddl())
                        )
                );
                result.addedCols++;
            }
            for (String c : effectiveDropped) {
                Column col = oldT.columns.get(c);
                result.actions.add(
                        new DiffAction(
                                DiffActionType.COLUMN_DROP,
                                String.format("ALTER TABLE %s DROP COLUMN %s;", realName, col.name),
                                realName + "." + col.name,
                                null,
                                realName,
                                col.name,
                                cc -> {
                                    for (String dep : findDependentDropStatements(cc.connection(), cc.action().oldTableName(), cc.action().oldColumnName())) {
//                                        System.out.println("Executing (dependency): " + dep);
                                        cc.connection().executeUpdate(dep);
                                    }
                                    cc.connection().executeUpdate(cc.action().ddl());
                                }
                        )
                );
                result.droppedCols++;
            }
            for (String c : common) {
                Column oc = oldT.columns.get(c);
                Column nc = newT.columns.get(c);
                if (!oc.type.equalsIgnoreCase(nc.type) || oc.nullable != nc.nullable) {
                    String nullClause = nc.nullable ? "" : " NOT NULL";
                    String was = oc.type + (oc.nullable ? "" : " NOT NULL");
                    result.actions.add(
                            new DiffAction(
                                    DiffActionType.COLUMN_ALTER,
                                    String.format(
                                            "ALTER TABLE %s ALTER COLUMN %s %s%s; -- was %s",
                                            realName, nc.name, nc.type, nullClause, was),
                                    realName + "." + nc.name,
                                    nullClause,
                                    realName,
                                    oc.name,
                                    cc -> cc.connection().executeUpdate(cc.action().ddl())
                            )

                    );
                    result.modifiedCols++;
                }
            }

            // ---- Foreign keys ----
            // map old FK column names through the rename map so a renamed column's FK
            // is compared against its new-name counterpart, not flagged as drop+add.
            Map<String, ForeignKey> oldFkByCol = new HashMap<>();
            for (ForeignKey fk : oldT.foreignKeys) {
                String mappedCol = renameMap.getOrDefault(fk.column, fk.column);
                oldFkByCol.put(mappedCol, fk);
            }
            Map<String, ForeignKey> newFkByCol = new HashMap<>();
            for (ForeignKey fk : newT.foreignKeys) newFkByCol.put(fk.column, fk);

            for (String col : newFkByCol.keySet()) {
                ForeignKey nfk = newFkByCol.get(col);
                ForeignKey ofk = oldFkByCol.get(col);
                if (ofk == null) {
                    Column c = newT.columns.get(col);
                    String colName = c != null ? c.name : col;
                    result.actions.add(
                            new DiffAction(
                                    DiffActionType.FK_ADD,
                                    String.format("ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s;", realName, nfk.constraintName, colName, nfk.targetTable)
                                    , null,
                                    String.format("%s.%s FOREIGN KEY (%s) REFERENCES %s;", realName, nfk.constraintName, colName, nfk.targetTable),
                                    realName,
                                    null,
                                    cc -> cc.connection().executeUpdate(cc.action().ddl())
                            )

                    );
                    result.addedFks++;
                } else if (!ofk.targetTable.equalsIgnoreCase(nfk.targetTable)) {
                    Column c = newT.columns.get(col);
                    String colName = c != null ? c.name : col;
                    result.actions.add(
                            new DiffAction(
                                    DiffActionType.FK_DROP,
                                    String.format("ALTER TABLE %s DROP CONSTRAINT %s; -- was FK on %s -> %s", realName, ofk.constraintName, colName, ofk.targetTable),
                                    realName + "." + ofk.constraintName,
                                    null,
                                    realName,
                                    null,
                                    cc -> cc.connection().executeUpdate(cc.action().ddl())
                            )
                    );
                    result.actions.add(
                            new DiffAction(DiffActionType.FK_ADD,
                                    String.format(
                                            "ALTER TABLE %s ADD CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s;",
                                            realName, nfk.constraintName, colName, nfk.targetTable),
                                    null,
                                    realName + "." + nfk.constraintName,
                                    realName,
                                    null,
                                    cc -> cc.connection().executeUpdate(cc.action().ddl())
                            )
                    );
                    result.addedFks++;
                    result.droppedFks++;
                }
            }
            for (String col : oldFkByCol.keySet()) {
                if (!newFkByCol.containsKey(col)) {
                    ForeignKey ofk = oldFkByCol.get(col);
                    Column c = oldT.columns.get(ofk.column);
                    String colName = c != null ? c.name : ofk.column;
                    result.actions.add(
                            new DiffAction(
                                    DiffActionType.FK_DROP,
                                    String.format(
                                            "ALTER TABLE %s DROP CONSTRAINT %s; -- was FK on %s -> %s",
                                            realName, ofk.constraintName, colName, ofk.targetTable),
                                    realName + "." + ofk.constraintName,
                                    null,
                                    realName,
                                    null,
                                    cc -> cc.connection().executeUpdate(cc.action().ddl())
                            )

                    );
                    result.droppedFks++;
                }
            }

            // ---- Unique constraints ----
            Map<String, UniqueConstraint> oldUqByCol = new HashMap<>();
            for (UniqueConstraint uq : oldT.uniqueConstraints) {
                String mappedCol = renameMap.getOrDefault(uq.column, uq.column);
                oldUqByCol.put(mappedCol, uq);
            }
            Map<String, UniqueConstraint> newUqByCol = new HashMap<>();
            for (UniqueConstraint uq : newT.uniqueConstraints) newUqByCol.put(uq.column, uq);

            for (String col : newUqByCol.keySet()) {
                if (!oldUqByCol.containsKey(col)) {
                    UniqueConstraint nuq = newUqByCol.get(col);
                    Column c = newT.columns.get(col);
                    String colName = c != null ? c.name : col;
                    result.actions.add(
                            new DiffAction(
                                    DiffActionType.UNIQUE_ADD,
                                    String.format(
                                            "ALTER TABLE %s ADD CONSTRAINT %s UNIQUE (%s);", realName, nuq.constraintName, colName),
                                    null,
                                    realName + "." + nuq.constraintName,
                                    realName,
                                    null,
                                    cc -> cc.connection().executeUpdate(cc.action().ddl())
                            )

                    );
                    result.addedUniques++;
                }
            }
            for (String col : oldUqByCol.keySet()) {
                if (!newUqByCol.containsKey(col)) {
                    UniqueConstraint ouq = oldUqByCol.get(col);
                    Column c = oldT.columns.get(ouq.column);
                    String colName = c != null ? c.name : ouq.column;
                    result.actions.add(
                            new DiffAction(
                                    DiffActionType.UNIQUE_DROP,
                                    String.format(
                                            "ALTER TABLE %s DROP CONSTRAINT %s; -- was UNIQUE (%s)", realName, ouq.constraintName, colName),
                                    realName + "." + ouq.constraintName,
                                    null,
                                    realName,
                                    null,
                                    cc -> cc.connection().executeUpdate(cc.action().ddl())
                            )

                    );
                    result.droppedUniques++;
                }
            }

            // ---- Primary key ----
            List<String> oldPk = new ArrayList<>();
            for (String c : oldT.primaryKeyColumns) oldPk.add(renameMap.getOrDefault(c, c));
            List<String> newPk = new ArrayList<>(newT.primaryKeyColumns);
            Collections.sort(oldPk);
            Collections.sort(newPk);
            if (!oldPk.equals(newPk)) {
                result.actions.add(new DiffAction(
                        DiffActionType.WARNING,
                        String.format(
                                "-- %s: primary key columns changed old=%s new=%s " +
                                        "(default-named PK constraint - verify actual constraint name in target DB before altering)",
                                realName, oldPk, newPk)
                ));
            }
        }

        return result;
    }

    // ---------- Main / output ----------


    /**
     * Finds every constraint that currently depends on a table (and optionally one column),
     * queried live from SQL Server's catalog views - not from the static DDL diff.
     * Covers the four things a diff-only approach can miss:
     * - FKs defined on THIS table referencing that column (outbound)
     * - FKs defined on OTHER tables referencing THIS table (inbound - blocks DROP TABLE)
     * - UNIQUE / PRIMARY KEY constraints (backed by indexes) involving that column
     * - DEFAULT constraints on that column
     * Returns ready-to-run "ALTER TABLE ... DROP CONSTRAINT ..." statements, in an order
     * that is always safe to execute before the column/table drop itself.
     */
    public List<String> findDependentDropStatements(NSqlConnection conn, String table, String column /* null = whole table */) {
        List<String> drops = new ArrayList<>();

        // 1. Inbound FKs from OTHER tables (only matters when dropping the whole table,
        //    but harmless / a no-op to check even for a single-column drop)
        String inboundFkSql =
                "SELECT fk.name, OBJECT_NAME(fk.parent_object_id) AS referencing_table " +
                        "FROM sys.foreign_keys fk " +
                        "WHERE fk.referenced_object_id = OBJECT_ID('" + table + "')";
        try (NQueryResult rr = conn.executeQuery(inboundFkSql)) {
            for (NSqlRow row : rr.iterable()) {
                String fkName = row.getString(1);
                String referencingTable = row.getString(2);
                drops.add("ALTER TABLE " + referencingTable + " DROP CONSTRAINT " + fkName + ";");
            }
        }

        // 2. FKs defined on THIS table (outbound). If column == null, all of them;
        //    otherwise only the ones touching that specific column.
        String outboundFkSql =
                "SELECT DISTINCT fk.name " +
                        "FROM sys.foreign_key_columns fkc " +
                        "JOIN sys.foreign_keys fk ON fkc.constraint_object_id = fk.object_id " +
                        "JOIN sys.columns c ON fkc.parent_object_id = c.object_id AND fkc.parent_column_id = c.column_id " +
                        "WHERE fkc.parent_object_id = OBJECT_ID('" + table + "')" +
                        (column != null ? " AND c.name = '" + column + "'" : "");
        try (NQueryResult rr = conn.executeQuery(outboundFkSql)) {
            for (NSqlRow row : rr.iterable()) {
                drops.add("ALTER TABLE " + table + " DROP CONSTRAINT " + row.getString(1) + ";");
            }
        }

        // 3. UNIQUE / PRIMARY KEY constraints (index-backed) involving the column (or all, if whole table)
        String uniqueSql =
                "SELECT DISTINCT i.name " +
                        "FROM sys.index_columns ic " +
                        "JOIN sys.indexes i ON ic.object_id = i.object_id AND ic.index_id = i.index_id " +
                        "JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id " +
                        "WHERE ic.object_id = OBJECT_ID('" + table + "') " +
                        "AND (i.is_unique = 1 OR i.is_primary_key = 1)" +
                        (column != null ? " AND c.name = '" + column + "'" : "");
        try (NQueryResult rr = conn.executeQuery(uniqueSql)) {
            for (NSqlRow row : rr.iterable()) {
                drops.add("ALTER TABLE " + table + " DROP CONSTRAINT " + row.getString(1) + ";");
            }
        }

        // 4. DEFAULT constraints on the column (SQL Server names these even if you never named them)
        if (column != null) {
            String defaultSql =
                    "SELECT dc.name " +
                            "FROM sys.default_constraints dc " +
                            "JOIN sys.columns c ON dc.parent_object_id = c.object_id AND dc.parent_column_id = c.column_id " +
                            "WHERE dc.parent_object_id = OBJECT_ID('" + table + "') AND c.name = '" + column + "'";
            try (NQueryResult rr = conn.executeQuery(defaultSql)) {
                for (NSqlRow row : rr.iterable()) {
                    drops.add("ALTER TABLE " + table + " DROP CONSTRAINT " + row.getString(1) + ";");
                }
            }
        }

        // 5. CHECK constraints referencing the column. SQL Server doesn't reliably expose which
        // columns a CHECK expression touches via a clean catalog join, so we pull the constraint
        // definition text and match the column name as a whole identifier (word boundary, not a
        // bare substring - avoids matching e.g. "gender" inside "gender_code"). Still a heuristic:
        // every match is logged with a greppable tag + the actual definition so it can be reviewed
        // by hand later, even though we go ahead and drop it now to unblock the migration.
        String checkSql =
                "SELECT cc.name, cc.definition " +
                        "FROM sys.check_constraints cc " +
                        "WHERE cc.parent_object_id = OBJECT_ID('" + table + "')";
        try (NQueryResult rr = conn.executeQuery(checkSql)) {
            for (NSqlRow row : rr.iterable()) {
                String ckName = row.getString(1);
                String definition = row.getString(2);
                boolean matches;
                if (column == null) {
                    matches = true;
                } else if (definition == null) {
                    matches = false;
                } else {
                    Pattern colPattern = Pattern.compile("(?i)\\b" + Pattern.quote(column) + "\\b");
                    matches = colPattern.matcher(definition).find();
                }
                if (matches) {
                    if (column != null) {
                        System.out.println("REVIEW-HEURISTIC-CHECK: dropping " + ckName + " on " + table
                                + " because its definition matched column '" + column + "': " + definition);
                    }
                    drops.add("ALTER TABLE " + table + " DROP CONSTRAINT " + ckName + ";");
                }
            }
        }

        return drops;
    }

    /**
     * Renames a column while preserving every constraint that depends on it. Unlike
     * findDependentDropStatements() (used for real DROP COLUMN, where dropping constraints
     * permanently is correct because the column is gone), a rename must recreate each
     * dependent constraint against the new column name afterward - otherwise UNIQUE/FK/CHECK
     * guarantees silently vanish from the schema.
     */
    public void safeRenameColumn(NSqlConnection conn, String table, String oldColumn, String newColumn) {
        List<String> recreateStatements = new ArrayList<>();

        // CHECK constraints referencing the column (same word-boundary heuristic as
        // findDependentDropStatements - see REVIEW-HEURISTIC-CHECK logging there).
        String checkSql =
                "SELECT cc.name, cc.definition " +
                        "FROM sys.check_constraints cc " +
                        "WHERE cc.parent_object_id = OBJECT_ID('" + table + "')";
        try (NQueryResult rr = conn.executeQuery(checkSql)) {
            for (NSqlRow row : rr.iterable()) {
                String ckName = row.getString(1);
                String definition = row.getString(2);
                if (definition != null && Pattern.compile("(?i)\\b" + Pattern.quote(oldColumn) + "\\b")
                        .matcher(definition).find()) {
                    System.out.println("REVIEW-HEURISTIC-CHECK (rename): dropping+recreating " + ckName
                            + " on " + table + " for rename " + oldColumn + " -> " + newColumn + ": " + definition);
                    conn.executeUpdate("ALTER TABLE " + table + " DROP CONSTRAINT " + ckName + ";");
                    String newDefinition = definition.replaceAll(
                            "(?i)\\b" + Pattern.quote(oldColumn) + "\\b", newColumn);
                    recreateStatements.add("ALTER TABLE " + table + " ADD CONSTRAINT " + ckName
                            + " CHECK " + newDefinition + ";");
                }
            }
        }

        // UNIQUE / PRIMARY KEY constraints (index-backed) involving the column.
        // Handles composite indexes too - rebuilds the full column list with the rename applied.
        String uniqueSql =
                "SELECT i.name, i.is_primary_key, " +
                        "STRING_AGG(c.name, ',') WITHIN GROUP (ORDER BY ic.key_ordinal) AS cols " +
                        "FROM sys.index_columns ic " +
                        "JOIN sys.indexes i ON ic.object_id = i.object_id AND ic.index_id = i.index_id " +
                        "JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id " +
                        "WHERE ic.object_id = OBJECT_ID('" + table + "') " +
                        "AND (i.is_unique = 1 OR i.is_primary_key = 1) " +
                        "AND i.object_id IN (" +
                        "    SELECT ic2.object_id FROM sys.index_columns ic2 " +
                        "    JOIN sys.columns c2 ON ic2.object_id = c2.object_id AND ic2.column_id = c2.column_id " +
                        "    WHERE ic2.index_id = i.index_id AND c2.name = '" + oldColumn + "'" +
                        ") " +
                        "GROUP BY i.name, i.is_primary_key, i.index_id";
        try (NQueryResult rr = conn.executeQuery(uniqueSql)) {
            for (NSqlRow row : rr.iterable()) {
                String idxName = row.getString(1);
                boolean isPk = row.getLong(2) != 0;
                String cols = row.getString(3);
                System.out.println("REVIEW-RENAME-CONSTRAINT: dropping+recreating " + idxName
                        + " on " + table + " for rename " + oldColumn + " -> " + newColumn);
                conn.executeUpdate("ALTER TABLE " + table + " DROP CONSTRAINT " + idxName + ";");
                String newCols = Arrays.stream(cols.split(","))
                        .map(c -> c.equalsIgnoreCase(oldColumn) ? newColumn : c)
                        .collect(Collectors.joining(", "));
                recreateStatements.add("ALTER TABLE " + table + " ADD CONSTRAINT " + idxName + " "
                        + (isPk ? "PRIMARY KEY" : "UNIQUE") + " (" + newCols + ");");
            }
        }

        // FKs defined on THIS table (outbound) touching the column.
        String fkSql =
                "SELECT fk.name, OBJECT_NAME(fk.referenced_object_id) AS ref_table, rc.name AS ref_col " +
                        "FROM sys.foreign_key_columns fkc " +
                        "JOIN sys.foreign_keys fk ON fkc.constraint_object_id = fk.object_id " +
                        "JOIN sys.columns c ON fkc.parent_object_id = c.object_id AND fkc.parent_column_id = c.column_id " +
                        "JOIN sys.columns rc ON fkc.referenced_object_id = rc.object_id AND fkc.referenced_column_id = rc.column_id " +
                        "WHERE fkc.parent_object_id = OBJECT_ID('" + table + "') AND c.name = '" + oldColumn + "'";
        try (NQueryResult rr = conn.executeQuery(fkSql)) {
            for (NSqlRow row : rr.iterable()) {
                String fkName = row.getString(1);
                String refTable = row.getString(2);
                String refCol = row.getString(3);
                System.out.println("REVIEW-RENAME-CONSTRAINT: dropping+recreating " + fkName
                        + " on " + table + " for rename " + oldColumn + " -> " + newColumn);
                conn.executeUpdate("ALTER TABLE " + table + " DROP CONSTRAINT " + fkName + ";");
                recreateStatements.add("ALTER TABLE " + table + " ADD CONSTRAINT " + fkName
                        + " FOREIGN KEY (" + newColumn + ") REFERENCES " + refTable + "(" + refCol + ");");
            }
        }

        // DEFAULT constraint on the column.
        String defaultSql =
                "SELECT dc.name, dc.definition " +
                        "FROM sys.default_constraints dc " +
                        "JOIN sys.columns c ON dc.parent_object_id = c.object_id AND dc.parent_column_id = c.column_id " +
                        "WHERE dc.parent_object_id = OBJECT_ID('" + table + "') AND c.name = '" + oldColumn + "'";
        try (NQueryResult rr = conn.executeQuery(defaultSql)) {
            for (NSqlRow row : rr.iterable()) {
                String dcName = row.getString(1);
                String definition = row.getString(2);
                System.out.println("REVIEW-RENAME-CONSTRAINT: dropping+recreating " + dcName
                        + " on " + table + " for rename " + oldColumn + " -> " + newColumn);
                conn.executeUpdate("ALTER TABLE " + table + " DROP CONSTRAINT " + dcName + ";");
                recreateStatements.add("ALTER TABLE " + table + " ADD CONSTRAINT " + dcName
                        + " DEFAULT " + definition + " FOR " + newColumn + ";");
            }
        }

        // Now the rename can proceed without hitting "enforced dependencies".
        conn.executeUpdate(String.format("EXEC sp_rename '%s.%s', '%s', 'COLUMN';", table, oldColumn, newColumn));

        // Recreate everything against the new column name.
        for (String stmt : recreateStatements) {
            System.out.println("Executing (recreate after rename): " + stmt);
            conn.executeUpdate(stmt);
        }
    }


}