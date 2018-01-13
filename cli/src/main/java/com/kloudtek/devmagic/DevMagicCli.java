package com.kloudtek.devmagic;

import com.kloudtek.ktcli.CliCommand;
import com.kloudtek.ktcli.CliHelper;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

@Command(name = "devmagic", showDefaultValues = true)
public class DevMagicCli extends CliCommand<DevMagicCli> {
    private final CliHelper cliHelper = new CliHelper(this);
    private static final ArrayList<Module> modules = new ArrayList<>();
    private static final ArrayList<CliCommand<?>> subCommands = new ArrayList<>();
    @CommandLine.Option(names = {"-o", "--output"}, description = "Output format")
    private Output output = Output.TEXT;

    static {
        for (Module module : ServiceLoader.load(Module.class)) {
            modules.add(module);
            if (module instanceof CliCommand) {
                subCommands.add((CliCommand<?>) module);
            }
        }
    }

    public void run(String... args) {
        cliHelper.initAndRun(args);
    }

    public DevMagicCli() {
    }

    @Override
    public List<CliCommand<?>> getExtraSubCommands() {
        return subCommands;
    }

    public static void main(String[] args) {
        new DevMagicCli().run(args);
    }


    public enum Output {
        TEXT
    }
}
