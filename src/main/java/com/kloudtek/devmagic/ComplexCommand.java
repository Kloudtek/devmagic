package com.kloudtek.devmagic;

import picocli.CommandLine;

import java.util.List;

public abstract class ComplexCommand implements Runnable {
    protected CommandLine cmd;

    public String getCmdName() {
        return getCmdName(this.getClass());
    }

    public abstract List<Object> getSubcommands();

    public void init(CommandLine cmd) {
        this.cmd = cmd;
        for (Object subCmd : getSubcommands()) {
            if (subCmd instanceof ComplexCommand) {
                CommandLine cmdLine = new CommandLine(subCmd);
                cmd.addSubcommand(((ComplexCommand) subCmd).getCmdName(), cmdLine);
                ((ComplexCommand) subCmd).init(cmdLine);
            } else {
                cmd.addSubcommand(getCmdName(subCmd.getClass()), subCmd);
            }
        }
    }

    @Override
    public void run() {
        cmd.usage(System.out);
    }

    public static String getCmdName(Class<?> clazz) {
        return clazz.getAnnotation(CommandLine.Command.class).name();
    }
}
