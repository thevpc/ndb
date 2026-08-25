package net.thevpc.ndb.servers.sql.sqlbase.cmd;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.io.NExecInput;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.ndb.servers.NdbConfig;
import net.thevpc.ndb.servers.base.CmdRedirect;
import net.thevpc.ndb.servers.base.cmd.RestoreCmd;
import net.thevpc.ndb.servers.sql.nmysql.util.AtName;
import net.thevpc.ndb.servers.sql.sqlbase.SqlSupport;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NRef;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SqlRestoreCmd<C extends NdbConfig> extends RestoreCmd<C> {
    public SqlRestoreCmd(SqlSupport<C> support, String... names) {
        super(support, names);
    }

    @Override
    public SqlSupport<C> getSupport() {
        return (SqlSupport<C>) super.getSupport();
    }

    public void run(NCmdLine cmdLine) {
        NRef<AtName> name = NRef.ofNull(AtName.class);
        NRef<NPath> file = NRef.ofNull(NPath.class);
        C otherOptions = createConfigInstance();
        while (cmdLine.hasNext()) {
            if (cmdLine.isNextOption()) {
                switch (cmdLine.peek().get().key()) {
                    case "--name": {
                        readConfigNameOption(cmdLine, name);
                        break;
                    }
                    case "--file": {
                        cmdLine.matcher().whenAny().asEntry((v) -> {
                            file.set(NPath.of(v.stringValue()));
                        }).anyMatch();
                        break;
                    }
                    default: {
                        fillOptionLast(cmdLine, otherOptions);
                    }
                }
            } else {
                cmdLine.throwUnexpectedArgument();
            }
        }
        String dumpExt = getSupport().getDumpExt(otherOptions);

        C options = loadFromName(name, otherOptions);
        NPath sqlFile;
        revalidateOptions(options);
        getSupport().prepareDump(options);
        if (file.get() == null) {
            throw new NIllegalArgumentException(NMsg.ofP("missing file"));
        } else {
            if (file.get().isDirectory()) {

            }
            if (file.get().name().toLowerCase().endsWith(".sql")) {
                sqlFile = file.get();
                CmdRedirect restoreCommand = getSupport().createRestoreCommand(sqlFile, options);
                NExec nExec = sysCmd().command(restoreCommand.getCmd().toStringArray());
                if (restoreCommand.getPath() != null) {
                    nExec.in(NExecInput.ofPath(restoreCommand.getPath()));
                }
                run(nExec);
            } else if (file.get().name().toLowerCase().endsWith(".zip")) {
                try (ZipInputStream zis = new ZipInputStream(file.get().inputStream())) {
                    //get the zipped file list entry
                    ZipEntry ze = zis.getNextEntry();
                    while (ze != null) {
                        String fileName = ze.getName();
                        if (fileName.endsWith("/")) {
                            file.get().resolveSibling(fileName).mkdirs();
                        } else {
                            if (fileName.endsWith(dumpExt)) {
                                NPath newFile = file.get().resolveSibling(NPath.of(fileName).name());
                                newFile.parent().mkdirs();
                                try (OutputStream fos = newFile.outputStream()) {
                                    byte[] buffer = new byte[2048];
                                    int count;
                                    while ((count = zis.read(buffer)) > 0) {
                                        fos.write(buffer, 0, count);
                                    }
                                    zis.closeEntry();
                                }

                                CmdRedirect restoreCommand = getSupport().createRestoreCommand(newFile, options);
                                NExec nExec = sysCmd().command(restoreCommand.getCmd().toStringArray());
                                if (restoreCommand.getPath() != null) {
                                    nExec.in(NExecInput.ofPath(restoreCommand.getPath()));
                                }
                                run(nExec);
                                newFile.delete();
                            }
                        }
                        ze = zis.getNextEntry();
                    }
                    zis.closeEntry();
                } catch (IOException ex) {
                    throw new NIOException(ex);
                }
            } else {
                throw new NIllegalArgumentException(NMsg.ofP("missing file"));
            }
        }
    }
}
