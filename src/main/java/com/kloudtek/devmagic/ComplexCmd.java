package com.kloudtek.devmagic;

import picocli.CommandLine;

import java.util.List;

public abstract class ComplexCmd implements Runnable {
    protected CommandLine cmd;

    public String getCmdName() {
        return getCmdName(this.getClass());
    }

    public abstract List<Object> getSubcommands();

    public void init(DevMagicCli cli, CommandLine cmd) {
        this.cmd = cmd;
        for (Object subCmd : getSubcommands()) {
            if (subCmd instanceof ComplexCmd) {
                CommandLine cmdLine = new CommandLine(subCmd);
                cmd.addSubcommand(((ComplexCmd) subCmd).getCmdName(), cmdLine);
                ((ComplexCmd) subCmd).init(cli, cmdLine);
            } else {
                cmd.addSubcommand(getCmdName(subCmd.getClass()), subCmd);
                if (subCmd instanceof Cmd) {
                    ((Cmd) subCmd).init(cli);
                }
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
