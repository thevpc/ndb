import net.thevpc.nsql.dump.api.NSqlDump;

public class TestDump {
    public static void main(String[] args) {
        NSqlDump nSqlDump = NSqlDump.of("sqlserver://sa:Rombatakaya@192.168.1.148/ESA4");
        System.out.println(nSqlDump.getConnection().getDatabaseName());
    }
}
