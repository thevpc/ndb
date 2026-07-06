package net.thevpc.nsql.ddl;

import net.thevpc.nsql.NSqlConnection;
import net.thevpc.nsql.UncheckedSqlException;

import java.io.PrintStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MsSqlServerNDdlScriptGenerator implements NDdlScriptGenerator {
    private Options options = new Options();

    // ------------------------------------------------------------------
    // Options
    // ------------------------------------------------------------------
    public static class Options {
        private boolean useSchema = true;
        private boolean useBrackets = true;
        private boolean lowercaseKeywords = false;
        private boolean addGoSeparators = false;
        private boolean useSemiColon = false;

        public Options() {
        }


        public Options(boolean useBrackets, boolean lowercaseKeywords, boolean addGoSeparators, boolean useSchema,boolean useSemiColon) {
            this.useBrackets = useBrackets;
            this.lowercaseKeywords = lowercaseKeywords;
            this.addGoSeparators = addGoSeparators;
            this.useSchema = useSchema;
            this.useSemiColon = useSemiColon;
        }

        private String kw(String keyword) {
            return lowercaseKeywords ? keyword.toLowerCase(Locale.ROOT) : keyword.toUpperCase(Locale.ROOT);
        }

        private String id(String identifier) {
            return useBrackets ? "[" + identifier + "]" : identifier;
        }

        public Options useBrackets(boolean useBrackets) {
            return new Options(useBrackets, lowercaseKeywords, addGoSeparators, useSchema,useSemiColon);
        }

        public Options lowercaseKeywords(boolean lowercaseKeywords) {
            return new Options(useBrackets, lowercaseKeywords, addGoSeparators, useSchema,useSemiColon);
        }

        public Options addGoSeparators(boolean addGoSeparators) {
            return new Options(useBrackets, lowercaseKeywords, addGoSeparators, useSchema,useSemiColon);
        }

        public Options useSchema(boolean useSchema) {
            return new Options(useBrackets, lowercaseKeywords, addGoSeparators, useSchema,useSemiColon);
        }
        public Options useSemiColon(boolean useSemiColon) {
            return new Options(useBrackets, lowercaseKeywords, addGoSeparators, useSchema,useSemiColon);
        }

        public boolean useSemiColon() {
            return useSemiColon;
        }

        public boolean useSchema() {
            return useSchema;
        }

        public boolean useBrackets() {
            return useBrackets;
        }

        public boolean lowercaseKeywords() {
            return lowercaseKeywords;
        }

        public boolean addGoSeparators() {
            return addGoSeparators;
        }
    }

    // ------------------------------------------------------------------
    // Metadata model
    // ------------------------------------------------------------------
    public static class Column {
        private final String name;
        String typeName;
        int maxLength;
        int precision;
        int scale;
        boolean nullable;
        boolean identity;
        long seed;
        long increment;

        public Column(String name, String typeName, int maxLength, int precision, int scale, boolean nullable, boolean identity, long seed, long increment) {
            this.name = name;
            this.typeName = typeName;
            this.maxLength = maxLength;
            this.precision = precision;
            this.scale = scale;
            this.nullable = nullable;
            this.identity = identity;
            this.seed = seed;
            this.increment = increment;
        }

        public String name() {
            return name;
        }

        public String typeName() {
            return typeName;
        }

        public int maxLength() {
            return maxLength;
        }

        public int precision() {
            return precision;
        }

        public int scale() {
            return scale;
        }

        public boolean nullable() {
            return nullable;
        }

        public boolean identity() {
            return identity;
        }

        public long seed() {
            return seed;
        }

        public long increment() {
            return increment;
        }
    }

    public static class Table {
        private final int objectId;
        String schema;
        String name;
        List<Column> columns;

        public Table(int objectId, String schema, String name, List<Column> columns) {
            this.objectId = objectId;
            this.schema = schema;
            this.name = name;
            this.columns = columns;
        }

        public int objectId() {
            return objectId;
        }

        public String schema() {
            return schema;
        }

        public String name() {
            return name;
        }

        public List<Column> columns() {
            return columns;
        }
    }

    public static class KeyConstraint {
        private final String schema;
        String table;
        String name;
        List<String> columns;
        List<Boolean> descending;

        public KeyConstraint(String schema, String table, String name, List<String> columns, List<Boolean> descending) {
            this.schema = schema;
            this.table = table;
            this.name = name;
            this.columns = columns;
            this.descending = descending;
        }

        public String schema() {
            return schema;
        }

        public String table() {
            return table;
        }

        public String name() {
            return name;
        }

        public List<String> columns() {
            return columns;
        }

        public List<Boolean> descending() {
            return descending;
        }
    }

    public static class ForeignKey {
        String schema;
        String table;
        String name;
        List<String> columns;
        String refSchema;
        String refTable;
        List<String> refColumns;
        int deleteAction;
        int updateAction;

        public ForeignKey(String schema, String table, String name, List<String> columns, String refSchema, String refTable, List<String> refColumns, int deleteAction, int updateAction) {
            this.schema = schema;
            this.table = table;
            this.name = name;
            this.columns = columns;
            this.refSchema = refSchema;
            this.refTable = refTable;
            this.refColumns = refColumns;
            this.deleteAction = deleteAction;
            this.updateAction = updateAction;
        }

        public String schema() {
            return schema;
        }

        public String table() {
            return table;
        }

        public String name() {
            return name;
        }

        public List<String> columns() {
            return columns;
        }

        public String refSchema() {
            return refSchema;
        }

        public String refTable() {
            return refTable;
        }

        public List<String> refColumns() {
            return refColumns;
        }

        public int deleteAction() {
            return deleteAction;
        }

        public int updateAction() {
            return updateAction;
        }
    }

    public static class CheckConstraint {
        String schema;
        String table;
        String name;
        String definition;

        public CheckConstraint(String schema, String table, String name, String definition) {
            this.schema = schema;
            this.table = table;
            this.name = name;
            this.definition = definition;
        }

        public String schema() {
            return schema;
        }

        public String table() {
            return table;
        }

        public String name() {
            return name;
        }

        public String definition() {
            return definition;
        }
    }

    public static class DefaultConstraint {
        String schema;
        String table;
        String name;
        String column;
        String definition;

        public DefaultConstraint(String schema, String table, String name, String column, String definition) {
            this.schema = schema;
            this.table = table;
            this.name = name;
            this.column = column;
            this.definition = definition;
        }

        public String schema() {
            return schema;
        }

        public String table() {
            return table;
        }

        public String name() {
            return name;
        }

        public String column() {
            return column;
        }

        public String definition() {
            return definition;
        }
    }

    public static class Index {
        String schema;
        String table;
        String name;
        boolean unique;
        List<String> columns;
        List<Boolean> descending;

        public Index(String schema, String table, String name, boolean unique, List<String> columns, List<Boolean> descending) {
            this.schema = schema;
            this.table = table;
            this.name = name;
            this.unique = unique;
            this.columns = columns;
            this.descending = descending;
        }

        public String schema() {
            return schema;
        }

        public String table() {
            return table;
        }

        public String name() {
            return name;
        }

        public boolean unique() {
            return unique;
        }

        public List<String> columns() {
            return columns;
        }

        public List<Boolean> descending() {
            return descending;
        }
    }

    // ------------------------------------------------------------------
    // Main
    // ------------------------------------------------------------------
//    public static void main(String[] args) throws Exception {
//        if (args.length < 4) {
//            System.err.println("Usage: DdlScriptGenerator <jdbcUrl> <user> <password> <outputFile> [--lowercase] [--no-brackets] [--go]");
//            System.exit(1);
//        }
//
//        String jdbcUrl = args[0];
//        String user = args[1];
//        String password = args[2];
//        String outputFile = args[3];
//
//        Options opts = new Options();
//        for (int i = 4; i < args.length; i++) {
//            switch (args[i]) {
//                case "--lowercase": {
//                    opts.lowercaseKeywords = true;
//                    break;
//                }
//                case "--no-brackets": {
//                    opts.useBrackets = false;
//                    break;
//                }
//                case "--go": {
//                    opts.addGoSeparators = true;
//                    break;
//                }
//                default: {
//                    System.err.println("Unknown option: " + args[i]);
//                }
//            }
//        }
//
//        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password)) {
//            new MsSqlServerDdlScriptGenerator().generateScript(conn, System.out, opts);
//        }
//    }


    public Options options() {
        return options;
    }

    public MsSqlServerNDdlScriptGenerator setOptions(Options options) {
        this.options = options == null ? new Options() : options;
        return this;
    }

    @Override
    public void generateScript(NSqlConnection conn, PrintStream out) {
        try {
            generateScript(conn.getConnection(), out, options);
        } catch (SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    void generateScript(Connection conn, PrintStream out, Options opts) throws SQLException {
        try (Statement diag = conn.createStatement();
             ResultSet drs = diag.executeQuery("SELECT DB_NAME() AS db, (SELECT COUNT(*) FROM sys.tables WHERE is_ms_shipped = 0) AS tbl_count")) {
            if (drs.next()) {
                System.out.println("Connected to database: " + drs.getString("db")
                        + " | user tables visible: " + drs.getInt("tbl_count"));
            }
        }

        StringBuilder sb = new StringBuilder();

        List<Table> tables = loadTables(conn);
        System.out.println("loadTables() returned " + tables.size() + " table(s)");
        Runnable doGo = () -> {
            if (opts.addGoSeparators) {
                out.println();
                out.println((opts.lowercaseKeywords() ? "go" : "GO"));
                out.println();
            }
        };
        for (Table t : tables) {
            out.println(buildCreateTable(t, opts));
            doGo.run();
        }

        for (KeyConstraint pk : loadKeyConstraints(conn, "PK")) {
            out.println(buildKeyConstraint(pk, "PRIMARY KEY", opts));
            doGo.run();
        }
        for (KeyConstraint uq : loadKeyConstraints(conn, "UQ")) {
            out.println(buildKeyConstraint(uq, "UNIQUE", opts));
            doGo.run();
        }
        for (DefaultConstraint dc : loadDefaultConstraints(conn)) {
            out.println(buildDefaultConstraint(dc, opts));
            doGo.run();
        }
        for (CheckConstraint cc : loadCheckConstraints(conn)) {
            out.println(buildCheckConstraint(cc, opts));
            doGo.run();
        }
        for (ForeignKey fk : loadForeignKeys(conn)) {
            out.println(buildForeignKey(fk, opts));
            doGo.run();
        }
        for (Index ix : loadIndexes(conn)) {
            out.println(buildIndex(ix, opts));
            doGo.run();
        }
    }

    // ------------------------------------------------------------------
    // Loaders
    // ------------------------------------------------------------------
    List<Table> loadTables(Connection conn) throws SQLException {
        List<Table> result = new ArrayList<>();
        String tableSql = "SELECT t.object_id, OBJECT_SCHEMA_NAME(t.object_id) AS schema_name, t.name AS table_name\n" +
                "                FROM sys.tables t\n" +
                "                WHERE t.is_ms_shipped = 0\n" +
                "                ORDER BY OBJECT_SCHEMA_NAME(t.object_id), t.name";
        String colSql = "SELECT c.column_id, c.name, st.name AS type_name, c.max_length, c.precision, c.scale,\n" +
                "                       c.is_nullable, c.is_identity,\n" +
                "                       ISNULL(ic.seed_value, 1) AS seed_value, ISNULL(ic.increment_value, 1) AS increment_value\n" +
                "                FROM sys.columns c\n" +
                "                JOIN sys.types st ON c.user_type_id = st.user_type_id\n" +
                "                LEFT JOIN sys.identity_columns ic ON ic.object_id = c.object_id AND ic.column_id = c.column_id\n" +
                "                WHERE c.object_id = ?\n" +
                "                ORDER BY c.column_id";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(tableSql)) {
            while (rs.next()) {
                int objectId = rs.getInt("object_id");
                String schema = rs.getString("schema_name");
                String name = rs.getString("table_name");

                List<Column> columns = new ArrayList<>();
                try (PreparedStatement ps = conn.prepareStatement(colSql)) {
                    ps.setInt(1, objectId);
                    try (ResultSet crs = ps.executeQuery()) {
                        while (crs.next()) {
                            columns.add(new Column(
                                    crs.getString("name"),
                                    crs.getString("type_name"),
                                    crs.getInt("max_length"),
                                    crs.getInt("precision"),
                                    crs.getInt("scale"),
                                    crs.getBoolean("is_nullable"),
                                    crs.getBoolean("is_identity"),
                                    crs.getLong("seed_value"),
                                    crs.getLong("increment_value")
                            ));
                        }
                    }
                }
                result.add(new Table(objectId, schema, name, columns));
            }
        }
        return result;
    }

    List<KeyConstraint> loadKeyConstraints(Connection conn, String type) throws SQLException {
        String sql = "SELECT kc.name AS constraint_name,\n" +
                "                       OBJECT_SCHEMA_NAME(kc.parent_object_id) AS schema_name,\n" +
                "                       OBJECT_NAME(kc.parent_object_id) AS table_name,\n" +
                "                       c.name AS column_name, ic.is_descending_key, ic.key_ordinal\n" +
                "                FROM sys.key_constraints kc\n" +
                "                JOIN sys.index_columns ic ON ic.object_id = kc.parent_object_id AND ic.index_id = kc.unique_index_id\n" +
                "                JOIN sys.columns c ON c.object_id = ic.object_id AND c.column_id = ic.column_id\n" +
                "                WHERE kc.type = ? AND OBJECTPROPERTY(kc.parent_object_id, 'IsMSShipped') = 0\n" +
                "                ORDER BY kc.name, ic.key_ordinal";
        return groupKeyConstraints(conn, sql, type);
    }

    static List<KeyConstraint> groupKeyConstraints(Connection conn, String sql, String type) throws SQLException {
        List<KeyConstraint> result = new ArrayList<>();
        String curName = null, curSchema = null, curTable = null;
        List<String> cols = new ArrayList<>();
        List<Boolean> desc = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("constraint_name");
                    if (curName != null && !curName.equals(name)) {
                        result.add(new KeyConstraint(curSchema, curTable, curName, new ArrayList<>(cols), new ArrayList<>(desc)));
                        cols.clear();
                        desc.clear();
                    }
                    curName = name;
                    curSchema = rs.getString("schema_name");
                    curTable = rs.getString("table_name");
                    cols.add(rs.getString("column_name"));
                    desc.add(rs.getBoolean("is_descending_key"));
                }
            }
        }
        if (curName != null) {
            result.add(new KeyConstraint(curSchema, curTable, curName, cols, desc));
        }
        return result;
    }

    List<ForeignKey> loadForeignKeys(Connection conn) throws SQLException {
        String sql = "SELECT fk.object_id, fk.name AS fk_name,\n" +
                "                       OBJECT_SCHEMA_NAME(fk.parent_object_id) AS schema_name,\n" +
                "                       OBJECT_NAME(fk.parent_object_id) AS table_name,\n" +
                "                       pc.name AS parent_column,\n" +
                "                       OBJECT_SCHEMA_NAME(fk.referenced_object_id) AS ref_schema,\n" +
                "                       OBJECT_NAME(fk.referenced_object_id) AS ref_table,\n" +
                "                       rc.name AS ref_column,\n" +
                "                       fk.delete_referential_action, fk.update_referential_action,\n" +
                "                       fkc.constraint_column_id\n" +
                "                FROM sys.foreign_keys fk\n" +
                "                JOIN sys.foreign_key_columns fkc ON fkc.constraint_object_id = fk.object_id\n" +
                "                JOIN sys.columns pc ON pc.object_id = fkc.parent_object_id AND pc.column_id = fkc.parent_column_id\n" +
                "                JOIN sys.columns rc ON rc.object_id = fkc.referenced_object_id AND rc.column_id = fkc.referenced_column_id\n" +
                "                WHERE OBJECTPROPERTY(fk.parent_object_id, 'IsMSShipped') = 0\n" +
                "                ORDER BY fk.name, fkc.constraint_column_id";
        List<ForeignKey> result = new ArrayList<>();
        String curName = null, curSchema = null, curTable = null, curRefSchema = null, curRefTable = null;
        int curDelete = 0, curUpdate = 0;
        List<String> cols = new ArrayList<>();
        List<String> refCols = new ArrayList<>();

        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString("fk_name");
                if (curName != null && !curName.equals(name)) {
                    result.add(new ForeignKey(curSchema, curTable, curName, new ArrayList<>(cols),
                            curRefSchema, curRefTable, new ArrayList<>(refCols), curDelete, curUpdate));
                    cols.clear();
                    refCols.clear();
                }
                curName = name;
                curSchema = rs.getString("schema_name");
                curTable = rs.getString("table_name");
                curRefSchema = rs.getString("ref_schema");
                curRefTable = rs.getString("ref_table");
                curDelete = rs.getInt("delete_referential_action");
                curUpdate = rs.getInt("update_referential_action");
                cols.add(rs.getString("parent_column"));
                refCols.add(rs.getString("ref_column"));
            }
        }
        if (curName != null) {
            result.add(new ForeignKey(curSchema, curTable, curName, cols, curRefSchema, curRefTable, refCols, curDelete, curUpdate));
        }
        return result;
    }

    List<CheckConstraint> loadCheckConstraints(Connection conn) throws SQLException {
        String sql = "SELECT OBJECT_SCHEMA_NAME(cc.parent_object_id) AS schema_name,\n" +
                "                       OBJECT_NAME(cc.parent_object_id) AS table_name,\n" +
                "                       cc.name AS constraint_name, cc.definition\n" +
                "                FROM sys.check_constraints cc\n" +
                "                WHERE OBJECTPROPERTY(cc.parent_object_id, 'IsMSShipped') = 0\n" +
                "                ORDER BY table_name, cc.name";
        List<CheckConstraint> result = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new CheckConstraint(
                        rs.getString("schema_name"), rs.getString("table_name"),
                        rs.getString("constraint_name"), rs.getString("definition")));
            }
        }
        return result;
    }

    List<DefaultConstraint> loadDefaultConstraints(Connection conn) throws SQLException {
        String sql = "SELECT OBJECT_SCHEMA_NAME(dc.parent_object_id) AS schema_name,\n" +
                "                       OBJECT_NAME(dc.parent_object_id) AS table_name,\n" +
                "                       dc.name AS constraint_name, c.name AS column_name, dc.definition\n" +
                "                FROM sys.default_constraints dc\n" +
                "                JOIN sys.columns c ON c.object_id = dc.parent_object_id AND c.column_id = dc.parent_column_id\n" +
                "                WHERE OBJECTPROPERTY(dc.parent_object_id, 'IsMSShipped') = 0\n" +
                "                ORDER BY table_name, dc.name";
        List<DefaultConstraint> result = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new DefaultConstraint(
                        rs.getString("schema_name"), rs.getString("table_name"),
                        rs.getString("constraint_name"), rs.getString("column_name"), rs.getString("definition")));
            }
        }
        return result;
    }

    List<Index> loadIndexes(Connection conn) throws SQLException {
        String sql = "SELECT i.object_id, i.name AS index_name,\n"
                + "       OBJECT_SCHEMA_NAME(i.object_id) AS schema_name,\n"
                + "       OBJECT_NAME(i.object_id) AS table_name,\n"
                + "       i.is_unique, c.name AS column_name, ic.is_descending_key, ic.key_ordinal\n"
                + "FROM sys.indexes i\n"
                + "JOIN sys.index_columns ic ON ic.object_id = i.object_id AND ic.index_id = i.index_id AND ic.is_included_column = 0\n"
                + "JOIN sys.columns c ON c.object_id = ic.object_id AND c.column_id = ic.column_id\n"
                + "WHERE i.is_primary_key = 0 AND i.is_unique_constraint = 0 AND i.type > 0\n"
                + "  AND OBJECTPROPERTY(i.object_id, 'IsMSShipped') = 0\n"
                + "ORDER BY i.name, ic.key_ordinal\n";
        List<Index> result = new ArrayList<>();
        String curName = null, curSchema = null, curTable = null;
        boolean curUnique = false;
        List<String> cols = new ArrayList<>();
        List<Boolean> desc = new ArrayList<>();

        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString("index_name");
                if (curName != null && !curName.equals(name)) {
                    result.add(new Index(curSchema, curTable, curName, curUnique, new ArrayList<>(cols), new ArrayList<>(desc)));
                    cols.clear();
                    desc.clear();
                }
                curName = name;
                curSchema = rs.getString("schema_name");
                curTable = rs.getString("table_name");
                curUnique = rs.getBoolean("is_unique");
                cols.add(rs.getString("column_name"));
                desc.add(rs.getBoolean("is_descending_key"));
            }
        }
        if (curName != null) {
            result.add(new Index(curSchema, curTable, curName, curUnique, cols, desc));
        }
        return result;
    }

    private String withSchema(String s, Options o) {
        if (o.useSchema()) {
            return o.id(s) + ".";
        }
        return "";
    }

    // ------------------------------------------------------------------
    // Builders (all formatting decisions live here)
    // ------------------------------------------------------------------
    String buildCreateTable(Table t, Options o) {
        StringBuilder sb = new StringBuilder();
        sb.append(o.kw("CREATE TABLE")).append(" ")
                .append(withSchema(t.schema(), o)).append(o.id(t.name())).append(" (").append(System.lineSeparator());

        for (int i = 0; i < t.columns().size(); i++) {
            Column c = t.columns().get(i);
            sb.append("    ").append(o.id(c.name())).append(" ").append(formatType(c, o));
            if (c.identity()) {
                sb.append(" ").append(o.kw("IDENTITY")).append("(").append(c.seed()).append(",").append(c.increment()).append(")");
            }
            sb.append(c.nullable() ? " " + o.kw("NULL") : " " + o.kw("NOT NULL"));
            if (i < t.columns().size() - 1) sb.append(",");
            sb.append(System.lineSeparator());
        }
        sb.append(")").append(mayBeSemiColon(o));
        return sb.toString();
    }

    String formatType(Column c, Options o) {
        String type = o.lowercaseKeywords ? c.typeName().toLowerCase(Locale.ROOT) : c.typeName().toUpperCase(Locale.ROOT);
        switch (c.typeName().toLowerCase(Locale.ROOT)) {
            case "varchar":
            case "char":
            case "varbinary":
            case "binary":
                return type + "(" + (c.maxLength() == -1 ? "MAX" : c.maxLength()) + ")";
            case "nvarchar":
            case "nchar":
                return type + "(" + (c.maxLength() == -1 ? "MAX" : c.maxLength() / 2) + ")";
            case "decimal":
            case "numeric":
                return type + "(" + c.precision() + "," + c.scale() + ")";
            case "datetime2":
            case "time":
            case "datetimeoffset":
                return type + "(" + c.scale() + ")";
            default:
                return type;
        }
    }

    String buildKeyConstraint(KeyConstraint kc, String kind, Options o) {
        String sb = o.kw("ALTER TABLE") + " " +
                withSchema(kc.schema(), o)
                + o.id(kc.table()) +
                System.lineSeparator() +
                "    " + o.kw("ADD CONSTRAINT") + " " + o.id(kc.name()) + " " + o.kw(kind) + " (" +
                columnList(kc.columns(), kc.descending(), o) + ")"+mayBeSemiColon(o);
        return sb;
    }

    String buildDefaultConstraint(DefaultConstraint dc, Options o) {
        return o.kw("ALTER TABLE") + " "
                + withSchema(dc.schema(), o)
                + o.id(dc.table()) + System.lineSeparator()
                + "    " + o.kw("ADD CONSTRAINT") + " " + o.id(dc.name()) + " " + o.kw("DEFAULT") + " " + dc.definition()
                + " " + o.kw("FOR") + " " + o.id(dc.column()) + mayBeSemiColon(o);
    }

    String buildCheckConstraint(CheckConstraint cc, Options o) {
        return o.kw("ALTER TABLE") + " "
                + withSchema(cc.schema(), o)
                + o.id(cc.table()) + System.lineSeparator()
                + "    " + o.kw("ADD CONSTRAINT") + " " + o.id(cc.name()) + " " + o.kw("CHECK") + " " + cc.definition() + mayBeSemiColon(o)
                ;
    }

    String buildForeignKey(ForeignKey fk, Options o) {
        StringBuilder sb = new StringBuilder();
        sb.append(o.kw("ALTER TABLE")).append(" ")
                .append(withSchema(fk.schema(), o))
                .append(o.id(fk.table()))
                .append(System.lineSeparator())
                .append("    ").append(o.kw("ADD CONSTRAINT")).append(" ").append(o.id(fk.name())).append(" ").append(o.kw("FOREIGN KEY")).append(" (")
                .append(columnList(fk.columns(), null, o)).append(")").append(System.lineSeparator())
                .append("    ").append(o.kw("REFERENCES")).append(" ").append(o.id(fk.refSchema())).append(".").append(o.id(fk.refTable()))
                .append(" (").append(columnList(fk.refColumns(), null, o)).append(")");

        switch (fk.deleteAction()) {
            case 1: {
                sb.append(" " + o.kw("ON DELETE CASCADE"));
                break;
            }
            case 2: {
                sb.append(" " + o.kw("ON DELETE SET NULL"));
                break;
            }
            case 3: {
                sb.append(" " + o.kw("ON DELETE SET DEFAULT"));
                break;
            }
        }
        switch (fk.updateAction()) {
            case 1: {
                sb.append(" " + o.kw("ON UPDATE CASCADE"));
                break;
            }
            case 2: {
                sb.append(" " + o.kw("ON UPDATE SET NULL"));
                break;
            }
            case 3: {
                sb.append(" " + o.kw("ON UPDATE SET DEFAULT"));
                break;
            }
        }
        sb.append(mayBeSemiColon(o));
        return sb.toString();
    }

    String buildIndex(Index ix, Options o) {
        StringBuilder sb = new StringBuilder();
        sb.append(o.kw("CREATE")).append(" ");
        if (ix.unique()) sb.append(o.kw("UNIQUE")).append(" ");
        sb.append(o.kw("INDEX")).append(" ").append(o.id(ix.name())).append(System.lineSeparator())
                .append("    ").append(o.kw("ON")).append(" ").append(o.id(ix.schema())).append(".").append(o.id(ix.table()))
                .append(" (").append(columnList(ix.columns(), ix.descending(), o)).append(")")
                .append(mayBeSemiColon(o))
        ;
        return sb.toString();
    }

    private String mayBeSemiColon(Options o) {
        if(o.useSemiColon()){
            return ";";
        }
        return "";
    }

    private static String columnList(List<String> cols, List<Boolean> descending, Options o) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(o.id(cols.get(i)));
            if (descending != null) {
                sb.append(descending.get(i) ? " " + o.kw("DESC") : " " + o.kw("ASC"));
            }
        }
        return sb.toString();
    }

}
