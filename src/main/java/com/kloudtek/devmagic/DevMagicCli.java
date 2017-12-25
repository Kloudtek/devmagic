package com.kloudtek.devmagic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kloudtek.util.UserDisplayableException;
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
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Command(name = "devmagic", showDefaultValues = true)
public class DevMagicCli extends ComplexCmd {
    public static final String DEFAULT_PROFILE = "defaultProfile";
    public static final String DEFAULT = "default";
    public static final String PROFILES = "profiles";
    @Option(names = {"-q", "--quiet"}, description = "Suppress informative message")
    private boolean quiet;
    @Option(names = {"-qq", "--veryquiet"}, description = "Suppress non-error message")
    private boolean veryQuiet;
    @Option(names = {"-v", "--verbose"}, description = "Verbose logging (overrides -q)")
    private boolean verbose;
    @Option(names = {"--saveconfig"}, description = "Save all configurable parameters (marked with © symbol) into the specified profile")
    private boolean saveConfig;
    @Option(names = {"-p", "--profile"}, description = "Configuration profile")
    private String profile;
    @Option(names = {"-c", "--config"}, description = "Configuration File")
    private File configFile = new File(System.getProperty("user.home") + File.separator + ".devmagic");
    @Option(names = {"-o", "--output"}, description = "Output format (this will set very quiet flag automatically)")
    private Output output = Output.TEXT;
    private static LoggerContext logCtx;
    private final CommandLine cmd;
    private boolean dirtyConfig = false;
    private ObjectNode config;
    private ObjectNode profileConfig;
    private static ObjectMapper objectMapper;
    private static final Console console;
    private static Scanner scanner;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.setDefaultPrettyPrinter(new DefaultPrettyPrinter());
        console = System.console();
        if (console == null) {
            scanner = new Scanner(System.in);
        }
    }

    public DevMagicCli() {
        cmd = new CommandLine(this);
    }

    private void loadConfig() {
        try {
            if (configFile.exists()) {
                JsonNode configNode = objectMapper.readTree(configFile);
                if (configNode.getNodeType() != JsonNodeType.OBJECT) {
                    throw new UserDisplayableException("Invalid configuration file " + configFile.getPath() + " is not a json object");
                }
                this.config = (ObjectNode) configNode;
                profile = getJsonString(config, DEFAULT_PROFILE, DEFAULT);
                ObjectNode profiles = getJsonObject(config, PROFILES);
                profileConfig = getJsonObject(profiles, profile);
            } else {
                config = new ObjectNode(JsonNodeFactory.instance);
                config.put(DEFAULT_PROFILE, DEFAULT);
                profile = DEFAULT;
                profileConfig = config.putObject(PROFILES).putObject(DEFAULT);
                dirtyConfig();
            }
        } catch (IOException e) {
            throw new UserDisplayableException("Unable to read configuration file: " + e.getMessage(), e);
        }
    }

    public String getJsonString(ObjectNode parent, String name, String defaultValue) {
        ObjectNode node;
        if (parent.has(name)) {
            return parent.get(name).textValue();
        } else {
            parent.put(name, defaultValue);
            dirtyConfig();
            return defaultValue;
        }
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static Map<String, Object> readJsonObject(String json) throws IOException {
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
        };
        return objectMapper.readValue(json, typeRef);
    }

    public ObjectNode getJsonObject(ObjectNode parent, String name) {
        ObjectNode node;
        if (parent.has(name)) {
            try {
                node = (ObjectNode) parent.get(name);
            } catch (ClassCastException e) {
                throw new UserDisplayableException("Invalid configuration file " + configFile.getPath() + " '" + name + "' is not a json object");
            }
        } else {
            node = new ObjectNode(JsonNodeFactory.instance);
            dirtyConfig();
        }
        return node;
    }

    public void dirtyConfig() {
        dirtyConfig = true;
    }

    private void writeConfig() {
        if (dirtyConfig) {
            try {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(configFile, config);
            } catch (IOException e) {
                System.out.println("Unable to write config file " + configFile.getPath() + " : " + e.getMessage());
                if (verbose) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ObjectNode getConfig(String module) {
        return getJsonObject(profileConfig, module);
    }

    public String read(String txt, String defVal) {
        return read(txt, defVal, false);
    }

    public String read(String txt, String defVal, boolean password) {
        for (; ; ) {
            System.out.print(txt);
            if (defVal != null) {
                System.out.print(" [" + (password ? "********" : defVal) + "]");
            }
            System.out.print(": ");
            System.out.flush();
            String val = password ? readPassword() : readLine();
            if (val != null) {
                val = val.trim();
                if (!val.isEmpty()) {
                    return val;
                }
                if (defVal != null) {
                    return defVal;
                }
            }
        }
    }

    public String readLine() {
        if (console != null) {
            return console.readLine();
        } else {
            return scanner.nextLine();
        }
    }

    public String readPassword() {
        if (console != null) {
            return new String(console.readPassword());
        } else {
            return scanner.nextLine();
        }
    }

    private boolean confirm(String txt) {
        return confirm(txt, null);
    }

    public boolean confirm(String txt, Boolean defaultValue) {
        for (; ; ) {
            String defValStr = null;
            if (defaultValue != null && defaultValue) {
                defValStr = "yes";
            } else if (defaultValue != null && !defaultValue) {
                defValStr = "no";
            }
            String val = read(txt, defValStr);
            if (val != null) {
                val = val.trim().toLowerCase();
                switch (val) {
                    case "yes":
                    case "y":
                    case "true":
                        return true;
                    case "no":
                    case "n":
                    case "false":
                        return false;
                    default:
                        System.out.println("Response must be either: yes, no, n, y, true, false");
                }
            }
        }
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

    private String[] parseBasicOptions(String[] args) {
        List<String> basicOptions = new ArrayList<>();
        List<String> otherArgs = new ArrayList<>();
        boolean basic = true;
        if (args != null) {
            for (String arg : args) {
                if (basic && !arg.startsWith("-")) {
                    basic = false;
                }
                if (basic) {
                    basicOptions.add(arg);
                } else {
                    otherArgs.add(arg);
                }
            }
            cmd.parse(basicOptions.toArray(new String[basicOptions.size()]));
        }
        return otherArgs.toArray(new String[otherArgs.size()]);
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
        try {
            cli.loadConfig();
            args = cli.parseBasicOptions(args);
            cli.setupLogging();
            cli.init(cli, cli.cmd);
            cli.parse(args);
            cli.writeConfig();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            if (cli.verbose) {
                e.printStackTrace();
            }
            System.exit(-1);
        }
    }

    public enum Output {
        TEXT
    }
}
