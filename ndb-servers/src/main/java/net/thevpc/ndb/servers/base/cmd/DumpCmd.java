package net.thevpc.ndb.servers.base.cmd;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.text.NObjectFormat;
import net.thevpc.nuts.io.NExecInput;
import net.thevpc.nuts.io.NPath;
import net.thevpc.ndb.servers.NdbConfig;
import net.thevpc.ndb.servers.base.CmdRedirect;
import net.thevpc.ndb.servers.base.NdbCmd;
import net.thevpc.ndb.servers.base.NdbSupportBase;
import net.thevpc.ndb.servers.sql.nmysql.util.AtName;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.util.NStringUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class DumpCmd<C extends NdbConfig> extends NdbCmd<C> {
    public DumpCmd(NdbSupportBase<C> support, String... names) {
        super(support, "dump");
        this.names.addAll(Arrays.asList(names));
    }


//    @Override
//    public void run(NSession session, NCmdLine cmdLine) {
//        NRef<AtName> name = NRef.ofNull(AtName.class);
//        ExtendedQuery eq = new ExtendedQuery(getName());
//        C otherOptions = createConfigInstance();
//
//        String status = "";
//        while (cmdLine.hasNext()) {
//            switch (status) {
//                case "": {
//                    switch (cmdLine.peek().get(session).key()) {
//                        case "--config": {
//                            readConfigNameOption(cmdLine, session, name);
//                            break;
//                        }
//                        case "--entity":
//                        case "--table":
//                        case "--collection": {
//                            cmdLine.withNextString((v, a, s) -> eq.setTable(v));
//                            break;
//                        }
//                        case "--where": {
//                            status = "--where";
//                            cmdLine.withNextBoolean((v, a, s) -> {
//                            });
//                            break;
//                        }
//                        case "--set": {
//                            status = "--set";
//                            cmdLine.withNextBoolean((v, a, s) -> {
//                            });
//                            break;
//                        }
//                        default: {
//                            fillOptionLast(cmdLine, otherOptions);
//                        }
//                    }
//                    break;
//                }
//                case "--where": {
//                    switch (cmdLine.peek().get(session).key()) {
//                        case "--set": {
//                            status = "--set";
//                            cmdLine.withNextBoolean((v, a, s) -> {
//                            });
//                            break;
//                        }
//                        default: {
//                            eq.getWhere().add(cmdLine.next().get().toString());
//                        }
//                    }
//                    break;
//                }
//                case "--set": {
//                    switch (cmdLine.peek().get(session).key()) {
//                        case "--where": {
//                            status = "--where";
//                            cmdLine.withNextBoolean((v, a, s) -> {
//                            });
//                            break;
//                        }
//                        default: {
//                            eq.getSet().add(cmdLine.next().get().toString());
//                        }
//                    }
//                    break;
//                }
//            }
//        }
//        if (NBlankable.isBlank(eq.getTable())) {
//            cmdLine.throwMissingArgumentByName("--table");
//        }
//
//        C options = loadFromName(name, otherOptions);
//        support.revalidateOptions(options);
//        if (NBlankable.isBlank(otherOptions.getDatabaseName())) {
//            cmdLine.throwMissingArgumentByName("--dbname");
//        }
//        runDump(eq, options, session);
//    }
//
//    protected void runDump(ExtendedQuery eq, C options, NSession session) {
//        throw new NIllegalArgumentException(session, NMsg.ofPlain("invalid"));
//    }


    public void run(NCmdLine cmdLine) {
        NRef<AtName> name = NRef.ofNull(AtName.class);
        NRef<NPath> file = NRef.ofNull(NPath.class);
        C otherOptions = createConfigInstance();
        NRef<Integer> roll = NRef.of(-1);
        while (cmdLine.hasNext()) {
            if (cmdLine.isNextOption()) {
                switch (cmdLine.peek().get().key()) {
                    case "--name": {
                        readConfigNameOption(cmdLine, name);
                        break;
                    }
                    case "--file": {
                        cmdLine.matcher().matchEntry((v) -> {
                            file.set(NPath.of(v.stringValue()));
                        }).anyMatch();
                        break;
                    }
                    case "--roll": {
                        cmdLine.matcher().matchEntry((v) -> {
                            roll.set(v.intValue());
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

        C options = loadFromName(name, otherOptions);
        revalidateOptions(options);
        getSupport().prepareDump(options);
        String simpleName = null;
        NPath plainFolderPath;
        NPath zipPath;
        boolean plainFolder = false;
        boolean zip = false;
        String dumpExt = NStringUtils.trim(getSupport().getDumpExt(options));
        if (file.get() == null) {
            if (roll.get() > 0) {
                zipPath=NPath.of(NObjectFormat.of()
                        .setFormatParam("count",roll.get())
                        .setNtf(false)
                        .format(NPath.ofUserDirectory().resolve(options.getDatabaseName() + "#.zip")).filteredText());
                simpleName = zipPath.nameParts().getBaseName();
                plainFolderPath = zipPath.resolve(simpleName + dumpExt);
            } else {
                simpleName = options.getDatabaseName() + "-" + new SimpleDateFormat("yyyyMMddHHmmssSSSSSS").format(new Date());
                plainFolderPath = NPath.of(simpleName + dumpExt);
                zipPath = NPath.of(simpleName + ".zip");
            }
            plainFolder = false;
            zip = true;

        } else if (file.get().isDirectory()) {
            if (roll.get() > 0) {
                zipPath=NPath.of(NObjectFormat.of()
                        .setFormatParam("count",roll.get())
                        .setNtf(false)
                        .format(file.get().resolve(options.getDatabaseName() + "#.zip")).filteredText());

                simpleName = zipPath.nameParts().getBaseName();
                plainFolderPath = zipPath.resolve(simpleName + dumpExt);

            } else {
                simpleName = options.getDatabaseName() + "-" + new SimpleDateFormat("yyyyMMddHHmmssSSSSSS").format(new Date());
                plainFolderPath = file.get().resolve(simpleName + dumpExt);
                zipPath = file.get().resolve(simpleName + ".zip");
            }
            plainFolder = false;
            zip = true;
        } else {
            NPath nFile = file.get();
            simpleName = nFile.nameParts().getBaseName();
            if (nFile.getName().toLowerCase().endsWith(".zip")) {
                if (roll.get() > 0) {
                    zipPath=NPath.of(NObjectFormat.of()
                            .setFormatParam("count",roll.get())
                            .setNtf(false)
                            .format(nFile).filteredText());
                    plainFolderPath = zipPath.resolveSibling(zipPath.getName() + dumpExt);
                } else {
                    zipPath = nFile;
                    plainFolderPath = zipPath.resolveSibling(simpleName + dumpExt);
                }
                plainFolder = false;
                zip = true;
            } else if (dumpExt.length() > 0 && nFile.getName().toLowerCase().endsWith(dumpExt)) {
                if (roll.get() > 0) {
                    plainFolderPath=NPath.of(NObjectFormat.of()
                            .setFormatParam("count",roll.get())
                            .setNtf(false)
                            .format(nFile).filteredText());
                    zipPath = plainFolderPath.resolveSibling(plainFolderPath.nameParts().getBaseName() + ".zip");
                } else {
                    plainFolderPath = nFile;
                    zipPath = plainFolderPath.resolveSibling(simpleName + ".zip");
                }
                plainFolder = true;
                zip = false;
            } else {
                if (roll.get() > 0) {
                    NPath roll1=NPath.of(NObjectFormat.of()
                            .setFormatParam("count",roll.get())
                            .setNtf(false)
                            .format(nFile).filteredText());
                    plainFolderPath = roll1.resolveSibling(roll1.getName() + dumpExt);
                    zipPath = nFile.resolveSibling(roll1.getName() + ".zip");
                } else {
                    plainFolderPath = nFile.resolveSibling(nFile.getName() + dumpExt);
                    zipPath = nFile.resolveSibling(nFile.getName() + ".zip");
                }
                plainFolder = false;
                zip = true;
            }
        }
        if (isRemoteCommand(options)) {
            String simpleName0 = zipPath.nameParts().getBaseName();
            NPath remoteTempFolder = getSupport().getRemoteTempFolder(options);
            NPath remotePlainFolder = remoteTempFolder.resolve(simpleName0 + dumpExt);
            NPath remoteZip = remoteTempFolder.resolve(simpleName0 + ".zip");
            CmdRedirect dumpCommand = getSupport().createDumpCommand(remotePlainFolder, options);
            run(getSupport().sysSsh(options)
                    .addCommand(dumpCommand.toString())
            );
            if (zip) {
                if (getSupport().isFolderArchive(options)) {
                    String sf = getSupport().getZipSubFolder(options);
                    if (NBlankable.isBlank(sf)) {
                        run(sysSsh(options)
                                .addCommand("cd " + remotePlainFolder.toString() + " ; zip -q -r "
                                        + remoteZip.toString()
                                        + " ."
                                )
                        );
                    } else {
                        run(sysSsh(options)
                                .addCommand("cd " + remotePlainFolder.resolve(sf).toString() + " ; zip -q -r "
                                        + remoteZip.toString()
                                        + " ."
                                )
                        );
                    }
                } else {
                    run(sysSsh(options)
                            .addCommand("zip -q -r "
                                    + remoteZip.toString()
                                    + " "
                                    + remotePlainFolder.toString()
                            )
                    );
                }
            }
            if (!plainFolder) {
                sshRm(remotePlainFolder, options);
            } else {
                sshPull(remotePlainFolder, plainFolderPath, options);
                sshRm(remotePlainFolder, options);
            }
            if (zip) {
                sshPull(remoteZip, zipPath, options);
                sshRm(remotePlainFolder, options);
            }
        } else {
            CmdRedirect dumpCommand = getSupport().createDumpCommand(plainFolderPath, options);
            NExec nExec = sysCmd().addCommand(dumpCommand.getCmd().toStringArray());
            if (dumpCommand.getPath() != null) {
                nExec.setIn(NExecInput.ofPath(dumpCommand.getPath()));
            }
            run(nExec);
            if (zip) {
                if (getSupport().isFolderArchive(options)) {
                    String sf = getSupport().getZipSubFolder(options);
                    if (NBlankable.isBlank(sf)) {
                        NExec zipExec = sysCmd()
                                .addCommand("zip")
                                .addCommand("-q");
                        if (plainFolderPath.isDirectory()) {
                            zipExec.addCommand("-r");
                            if (true) {
                                zipExec.addCommand("-j");
                            }
                        }
                        zipExec.addCommand(zipPath.toString());
                        zipExec.addCommand(".");
                        zipExec.setDirectory(plainFolderPath);
                        run(zipExec);
                    } else {

                        NExec zipExec = sysCmd()
                                .addCommand("zip")
                                .addCommand("-q");
                        if (plainFolderPath.isDirectory()) {
                            zipExec.addCommand("-r");
                            if (true) {
                                zipExec.addCommand("-j");
                            }
                        }
                        zipExec.addCommand(zipPath.toString());
                        zipExec.addCommand(".");
                        zipExec.setDirectory(plainFolderPath.resolve(sf));
                        run(zipExec);
                    }
                } else {
                    NExec zipExec = sysCmd()
                            .addCommand("zip")
                            .addCommand("-q");
                    if (plainFolderPath.isDirectory()) {
                        zipExec.addCommand("-r");
                        if (true) {
                            zipExec.addCommand("-j");
                        }
                    }
                    zipExec.addCommand(zipPath.toString());
                    zipExec.addCommand(plainFolderPath.toString());
                    zipExec.setDirectory(plainFolderPath.getParent());
                    run(zipExec);
                }


            }
            if (!plainFolder) {
                plainFolderPath.deleteTree();
            }
        }
    }

}
