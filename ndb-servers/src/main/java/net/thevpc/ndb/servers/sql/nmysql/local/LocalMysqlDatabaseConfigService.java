package net.thevpc.ndb.servers.sql.nmysql.local;

import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.elem.NElementWriter;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.security.NWorkspaceSecurityManager;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.text.NExecWriter;
import net.thevpc.ndb.servers.sql.nmysql.local.config.LocalMysqlDatabaseConfig;
import net.thevpc.nuts.util.NBlankable;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LocalMysqlDatabaseConfigService {
    private String name;
    private LocalMysqlDatabaseConfig config;
    private LocalMysqlConfigService mysql;
    private NSession session;

    public LocalMysqlDatabaseConfigService(String name, LocalMysqlDatabaseConfig config, LocalMysqlConfigService mysql) {
        this.name = name;
        this.config = config;
        this.mysql = mysql;
        this.session = mysql.getSession();
    }

    public LocalMysqlDatabaseConfig getConfig() {
        return config;
    }

    public LocalMysqlConfigService getMysql() {
        return mysql;
    }

    public LocalMysqlDatabaseConfigService remove() {
        mysql.getConfig().getDatabases().remove(name);
        NOut.println(NMsg.ofC("%s app removed.", getBracketsPrefix(getFullName())));
        return this;
    }

    public NText getBracketsPrefix(String str) {
        return NTextBuilder.of()
                .append("[")
                .append(str, NTextStyle.primary5())
                .append("]");
    }

    public String getFullName() {
        return getName() + "@" + mysql.getName();
    }

    public String getName() {
        return name;
    }

    public LocalMysqlDatabaseConfigService write(PrintStream out) {
        NElementWriter.ofJson().write(getConfig(),out);
        return this;
    }

    public ArchiveResult backup(String path) {
        if (NBlankable.isBlank(path)) {
            String databaseName = getConfig().getDatabaseName();
            if (NBlankable.isBlank(databaseName)) {
                databaseName = name;
            }
            path = databaseName + "-" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + ".sql.zip";
        }
        if (!path.endsWith(".sql.zip") && !path.endsWith(".zip") && !path.endsWith(".sql")) {
            path = path + ".sql.zip";
        }
        path= Paths.get(path).toAbsolutePath().normalize().toString();
        String password = getConfig().getPassword();
        char[] credentials = NWorkspaceSecurityManager.of().getCredentials(password.toCharArray());
        password = new String(credentials);
        if (path.endsWith(".sql")) {
            if (session.isPlainTrace()) {
                NOut.println(NMsg.ofC("%s create archive %s", getDatabaseName(), path));
            }

            NExec cmd = NExec.of()
                    .system()
                    .setCommand("sh", "-c",
                            "\"" + mysql.getMysqldumpCommand() + "\" -u \"$CMD_USER\" -p\"$CMD_PWD\" --databases \"$CMD_DB\" > \"$CMD_FILE\""
                    )
                    .setEnv("CMD_FILE", path)
                    .setEnv("CMD_USER", getConfig().getUser())
                    .setEnv("CMD_PWD", password)
                    .setEnv("CMD_DB", getDatabaseName())
                    .grabAll();
            int result = cmd
                    .getResultCode();
            if (result == 0) {
                return new ArchiveResult(path, result, false);
            } else {
                if (new File(path).exists()) {
                    new File(path).delete();
                }
                throw new NExecutionException(NMsg.ofNtf(cmd.getGrabbedOutString()), NExecutionException.ERROR_2);
            }
        } else {
            if (session.isPlainTrace()) {
                NOut.println(NMsg.ofC("%s create archive %s", getBracketsPrefix(getDatabaseName()),
                        NTexts.of()
                        .ofStyled(path, NTextStyle.path())));
            }
            NExec cmd = NExec.of()
                    .system()
                    .setCommand("sh", "-c",
                            "set -o pipefail && \"" + mysql.getMysqldumpCommand() + "\" -u \"$CMD_USER\" -p" + password + " --databases \"$CMD_DB\" | gzip > \"$CMD_FILE\""
                    )
                    .setEnv("CMD_FILE", path)
                    .setEnv("CMD_USER", getConfig().getUser())
                    .setEnv("CMD_PWD", password)
                    .setEnv("CMD_DB", getDatabaseName())
                    //                    .inheritIO()
                    .grabAll();
            if (session.isPlainTrace()) {
                NOut.println(NMsg.ofC("%s    [exec] %s", getBracketsPrefix(getDatabaseName()),
                        NExecWriter.of().setEnvReplacer(envEntry -> {
                            if ("CMD_PWD".equals(envEntry.getName())) {
                                return "****";
                            }
                            return null;
                        }).format(cmd)
                ));
            }
            int result = cmd.getResultCode();
            if (result == 0) {
                return new ArchiveResult(path, result, false);
            } else {
                if (new File(path).exists()) {
                    new File(path).delete();
                }
                throw new NExecutionException(NMsg.ofNtf(cmd.getGrabbedOutString()), NExecutionException.ERROR_2);
            }
        }
    }

    public RestoreResult restore(String path) {
//        if(!path.endsWith(".sql") && !path.endsWith(".sql.zip") && !path.endsWith(".zip")){
//            path=path+
//        }
        char[] password = NWorkspaceSecurityManager.of().getCredentials(getConfig().getPassword().toCharArray());

        if (path.endsWith(".sql")) {
            if (session.isPlainTrace()) {
                NOut.println(NMsg.ofC("%s restore archive %s", getBracketsPrefix(getDatabaseName()), path));
            }
            int result = NExec.of()
                    .system()
                    .setCommand("sh", "-c",
                            "cat \"$CMD_FILE\" | " + "\"" + mysql.getMysqlCommand() + "\" -h \"$CMD_HOST\" -u \"$CMD_USER\" \"-p$CMD_PWD\" \"$CMD_DB\""
                    )
                    .setEnv("CMD_FILE", path)
                    .setEnv("CMD_USER", getConfig().getUser())
                    .setEnv("CMD_PWD", new String(password))
                    .setEnv("CMD_DB", getDatabaseName())
                    .setEnv("CMD_HOST", "localhost")
                    //.inheritIO()
//                        .start().waitFor()
                    .getResultCode();
            return new RestoreResult(path, result, false);
        } else {
            if (session.isPlainTrace()) {
                NOut.println(NMsg.ofC("%s restore archive %s", getBracketsPrefix(getDatabaseName()), path));
            }

            int result = NExec.of()
                    .system().setCommand("sh", "-c",
                            "gunzip -c \"$CMD_FILE\" | \"" + mysql.getMysqlCommand() + "\" -h \"$CMD_HOST\" -u \"$CMD_USER\" \"-p$CMD_PWD\" \"$CMD_DB\""
                    )
                    .setEnv("CMD_FILE", path)
                    .setEnv("CMD_USER", getConfig().getUser())
                    .setEnv("CMD_PWD", new String(password))
                    .setEnv("CMD_DB", getDatabaseName())
                    .setEnv("CMD_HOST", "localhost")
//                        .start()
//                        .inheritIO()
//                        .waitFor()
                    .getResultCode();
            return new RestoreResult(path, result, true);
        }
    }

    public String getDatabaseName() {
        String s = getConfig().getDatabaseName();
        if (NBlankable.isBlank(s)) {
            s = name;
        }
        return s;
    }

    public static class ArchiveResult {

        public String path;
        public int execResult;
        public boolean zip;

        public ArchiveResult(String path, int execResult, boolean zip) {
            this.path = path;
            this.execResult = execResult;
            this.zip = zip;
        }
    }

    public static class RestoreResult {

        public String path;
        public int execResult;
        public boolean zip;

        public RestoreResult(String path, int execResult, boolean zip) {
            this.path = path;
            this.execResult = execResult;
            this.zip = zip;
        }
    }
}
