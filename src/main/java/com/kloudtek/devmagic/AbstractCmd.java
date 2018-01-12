package com.kloudtek.devmagic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import picocli.CommandLine;

import java.util.Collections;
import java.util.List;

public abstract class AbstractCmd<T extends AbstractCmd> {
    protected DevMagicCli cli;
    protected CommandLine commandLine;
    protected ObjectNode config;
    protected T parent;

    public AbstractCmd() {
    }

    public AbstractCmd(DevMagicCli cli, CommandLine commandLine, T parent, ObjectNode cfg) {
        init(cli, commandLine, parent, cfg);
    }

    protected void init(DevMagicCli cli, CommandLine commandLine, T parent, ObjectNode cfg) {
        this.cli = cli;
        this.commandLine = commandLine;
        this.parent = parent;
        config = cfg;
    }

    protected void loadConfig() throws Exception {
        if (config != null) {
            DevMagicCli.getObjectMapper().readerForUpdating(this).readValue(config);
        }
    }

    protected void saveConfig() {
//        DevMagicCli.getObjectMapper().writeValue(this,config);
        ObjectNode jsonNode = DevMagicCli.getObjectMapper().valueToTree(this);
        config.setAll(jsonNode);
    }

    public List<AbstractCmd<?>> getSubCommands() {
        return Collections.emptyList();
    }

    protected void execute() throws Exception {
        commandLine.usage(System.out);
    }
}
