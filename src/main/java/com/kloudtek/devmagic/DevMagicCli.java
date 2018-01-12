package com.kloudtek.devmagic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kloudtek.devmagic.util.VerySimpleLogger;
import com.kloudtek.util.UserDisplayableException;
import org.slf4j.spi.LocationAwareLogger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import static picocli.CommandLine.printHelpIfRequested;

@Command(name = "devmagic", showDefaultValues = true)
public class DevMagicCli extends AbstractCmd<DevMagicCli> {
    public static final String DEFAULT_PROFILE = "defaultProfile";
    public static final String DEFAULT = "default";
    public static final String PROFILES = "profiles";
    public static final String SUBCOMMANDS = "subcommands";
    @Option(names = {"-q", "--quiet"}, description = "Suppress informative message")
    private boolean quiet;
    @Option(names = {"-v", "--verbose"}, description = "Verbose logging (overrides -q)")
    private boolean verbose;
    @Option(names = {"-sc", "--save-config"}, description = "Save all configurable parameters (marked with © symbol) into the specified profile")
    private boolean saveConfig;
    @Option(names = {"-p", "--profile"}, description = "Configuration profile")
    private String profile;
    @Option(names = {"-c", "--config"}, description = "Configuration File")
    private File configFile = new File(System.getProperty("user.home") + File.separator + ".devmagic");
    @Option(names = {"-o", "--output"}, description = "Output format")
    private Output output = Output.TEXT;
    private final CommandLine cmd;
    private ObjectNode profileConfig;
    private static ObjectMapper objectMapper;
    private static final Console console;
    private static Scanner scanner;
    private static final ArrayList<Module> modules = new ArrayList<>();
    private static final ArrayList<AbstractCmd<?>> subCommands = new ArrayList<>();

    static {
        objectMapper = new ObjectMapper();
        objectMapper.setDefaultPrettyPrinter(new DefaultPrettyPrinter());
        objectMapper.disable(MapperFeature.AUTO_DETECT_CREATORS,
                MapperFeature.AUTO_DETECT_FIELDS,
                MapperFeature.AUTO_DETECT_GETTERS,
                MapperFeature.AUTO_DETECT_IS_GETTERS);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        console = System.console();
        if (console == null) {
            scanner = new Scanner(System.in);
        }
        for (Module module : ServiceLoader.load(Module.class)) {
            modules.add(module);
            if (module instanceof AbstractCmd) {
                subCommands.add((AbstractCmd<?>) module);
            }
        }
    }

    public DevMagicCli() {
        cmd = new CommandLine(this);
    }

    protected void loadConfigFile() {
        try {
            if (configFile.exists()) {
                JsonNode configNode = objectMapper.readTree(configFile);
                if (configNode.getNodeType() != JsonNodeType.OBJECT) {
                    throw new UserDisplayableException("Invalid configuration file " + configFile.getPath() + " is not a json object");
                }
                config = (ObjectNode) configNode;
                profile = getJsonString(config, DEFAULT_PROFILE, DEFAULT);
                ObjectNode profiles = getJsonObject(config, PROFILES);
                profileConfig = getJsonObject(profiles, profile);
            } else {
                config = new ObjectNode(JsonNodeFactory.instance);
                config.put(DEFAULT_PROFILE, DEFAULT);
                profile = DEFAULT;
                profileConfig = config.putObject(PROFILES).putObject(DEFAULT);
            }
        } catch (IOException e) {
            throw new UserDisplayableException("Unable to read configuration file: " + e.getMessage(), e);
        }
    }

    public String getJsonString(ObjectNode parent, String name, String defaultValue) {
        if (parent.has(name)) {
            return parent.get(name).textValue();
        } else {
            parent.put(name, defaultValue);
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
        }
        return node;
    }

    private void writeConfig() {
        if (saveConfig) {
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

    @Override
    public List<AbstractCmd<?>> getSubCommands() {
        return subCommands;
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
        if (verbose) {
            VerySimpleLogger.LOGLEVEL = LocationAwareLogger.DEBUG_INT;
        } else if (quiet) {
            VerySimpleLogger.LOGLEVEL = LocationAwareLogger.ERROR_INT;
        } else {
            VerySimpleLogger.LOGLEVEL = LocationAwareLogger.INFO_INT;
        }
    }

    private void execute(String[] args) throws Exception {
        doInit(cmd, null, profileConfig);
        List<Object> objects = cmd.parseWithHandler(new CommandLine.IParseResultHandler() {
            @Override
            public List<Object> handleParseResult(List<CommandLine> parsedCommands, PrintStream out, CommandLine.Help.Ansi ansi) throws CommandLine.ExecutionException {
                if (printHelpIfRequested(parsedCommands, out, ansi)) {
                    return Collections.emptyList();
                }
                CommandLine last = parsedCommands.get(parsedCommands.size() - 1);
                try {
                    ((AbstractCmd) last.getCommand()).execute();
                } catch (Exception e) {
                    throw new CommandLine.ExecutionException(last, "Error executing " + last.getCommandName() + ": " + e.getMessage(), e);
                }
                if (saveConfig) {
                    for (CommandLine parsedCommand : parsedCommands) {
                        AbstractCmd command = parsedCommand.getCommand();
                        command.saveConfig();
                    }
                }
                return Collections.emptyList();
            }
        }, System.out, args);
    }

    @SuppressWarnings("unchecked")
    private void doInit(@Nonnull CommandLine commandLine, @Nullable AbstractCmd parent, @Nonnull ObjectNode cfg) {
        try {
            AbstractCmd cmd = commandLine.getCommand();
            cmd.init(this, commandLine, parent, cfg);
            cmd.loadConfig();
            List<AbstractCmd> subModules = cmd.getSubCommands();
            if (!subModules.isEmpty()) {
                for (AbstractCmd subCommand : subModules) {
                    Command annotation = subCommand.getClass().getAnnotation(Command.class);
                    if (annotation == null) {
                        throw new UserDisplayableException("Command is missing annotation: " + subCommand.getClass().getName());
                    }
                    CommandLine subCmdLine = new CommandLine(subCommand);
                    String subCmdName = annotation.name();
                    commandLine.addSubcommand(subCmdName, subCmdLine);
                }
            }
            for (Map.Entry<String, CommandLine> subCmdEntry : commandLine.getSubcommands().entrySet()) {
                ObjectNode subCommandsConfigNode = (ObjectNode) cfg.get(SUBCOMMANDS);
                if (subCommandsConfigNode == null) {
                    subCommandsConfigNode = cfg.putObject(SUBCOMMANDS);
                }
                CommandLine subCmdLine = subCmdEntry.getValue();
                ObjectNode subCmdConfigNode = (ObjectNode) subCommandsConfigNode.get(subCmdLine.getCommandName());
                if (subCmdConfigNode == null) {
                    subCmdConfigNode = subCommandsConfigNode.putObject(subCmdLine.getCommandName());
                }
                doInit(subCmdLine, cmd, subCmdConfigNode);
            }
        } catch (Exception e) {
            throw new UserDisplayableException("Error loading config: " + e.getMessage(), e);
        }
    }

    @Override
    protected void init(DevMagicCli cli, CommandLine commandLine, DevMagicCli parent, ObjectNode cfg) {
    }

    @Override
    protected void loadConfig() {
    }

    @Override
    protected void saveConfig() {
    }

    public static void main(String[] args) {
        DevMagicCli cli = new DevMagicCli();
        try {
            cli.loadConfigFile();
            args = cli.parseBasicOptions(args);
            cli.setupLogging();
            cli.execute(args);
            cli.writeConfig();
        } catch (Exception e) {
            if (e instanceof CommandLine.ExecutionException) {
                System.out.println(((CommandLine.ExecutionException) e).getCommandLine().getCommandName() + " : " + e.getCause().getMessage());
            } else {
                System.out.println(e.getMessage());
            }
            if (cli.verbose) {
                e.printStackTrace();
            }
            System.exit(-1);
        }
    }

    public enum Output {
        TEXT
    }

    public static String readLine() {
        if (console != null) {
            return console.readLine();
        } else {
            return scanner.nextLine();
        }
    }

    public static String readPassword() {
        if (console != null) {
            return new String(console.readPassword());
        } else {
            return scanner.nextLine();
        }
    }

    public static boolean confirm(String txt) {
        return confirm(txt, null);
    }

    public static boolean confirm(String txt, Boolean defaultValue) {
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

    public static String read(String txt, String defVal) {
        return read(txt, defVal, false);
    }

    public static String read(String txt, String defVal, boolean password) {
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
}
