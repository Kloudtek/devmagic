package com.kloudtek.devmagic;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class DevMagicCli extends ComplexCommand {
    @Option(names = {"-q", "--quiet"}, description = "Suppress informative message")
    private boolean quiet;
    @Option(names = {"-v", "--verbose"}, description = "Verbose logging (overrides -q)")
    private boolean verbose;
    private static LoggerContext logCtx;
    private final CommandLine cmd;

    public DevMagicCli() {
        cmd = new CommandLine(this);
        cmd.setCommandName("devmagic");
    }

    @Override
    public String getCmdName() {
        return "devmagic";
    }

    @Override
    public List<Object> getSubcommands() {
        ArrayList<Object> list = new ArrayList<>();
        for (Module module : ServiceLoader.load(Module.class)) {
            list.add(module);
        }
        return list;
    }

    private void parseBasicOptions(String[] args) {
        if( args != null ) {
            ArrayList<String> basicOptions = new ArrayList<>();
            for (String arg : args) {
                if( arg.startsWith("-") ) {
                    basicOptions.add(arg);
                } else {
                    break;
                }
            }
            cmd.parse(basicOptions.toArray(new String[basicOptions.size()]));
        }
    }

    private void setupLogging() {
        Level lvl;
        if (verbose) {
            lvl = Level.DEBUG;
        } else if (quiet) {
            lvl = Level.WARN;
        } else {
            lvl = Level.INFO;
        }
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setStatusLevel(Level.ERROR);
        builder.setConfigurationName("BuilderTest");
        builder.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.NEUTRAL)
                .addAttribute("level", lvl));
        AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE").addAttribute("target",
                ConsoleAppender.Target.SYSTEM_OUT);
        appenderBuilder.add(builder.newLayout("PatternLayout").addAttribute("pattern", "%msg%n"));
        builder.add(appenderBuilder);
        builder.add(builder.newLogger("org.apache.logging.log4j", Level.INFO).add(builder.newAppenderRef("Stdout")).addAttribute("additivity", false));
        builder.add(builder.newRootLogger(lvl).add(builder.newAppenderRef("Stdout")));
        logCtx = Configurator.initialize(builder.build());
    }

    private void parse(String[] args) {
        cmd.parseWithHandler(new CommandLine.RunLast(), System.out, args);
    }

    public static void main(String[] args) throws Exception {
        DevMagicCli cli = new DevMagicCli();
        cli.parseBasicOptions(args);
        cli.setupLogging();
        cli.init(cli.cmd);
        cli.parse(args);
    }
}
