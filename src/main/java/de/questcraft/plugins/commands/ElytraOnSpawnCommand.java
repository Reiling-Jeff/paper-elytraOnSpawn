package de.questcraft.plugins.commands;

import de.questcraft.plugins.ElytraOnSpawn;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ElytraOnSpawnCommand implements CommandExecutor, TabCompleter {
    private final ElytraOnSpawn plugin;
    private final FileConfiguration config;
    private final File configFile;

    private final List<String> mainCommands = Arrays.asList("config", "conf", "reload");
    private final List<String> configCommands = Arrays.asList("reset", "check");
    private final List<String> configKeys = Arrays.asList(
            "verbose",
            "spawnradius",
            "flyboostmultiplier",
            "startboostmultiplier",
            "world",
            "boostsoundsetter",
            "boostsound",
            "switchgamemodecancelsoundsetter",
            "switchgamemodecancelsound",
            "particle"
    );
    private final List<String> allCommands = Arrays.asList("help", "reload", "config");

    public ElytraOnSpawnCommand(ElytraOnSpawn plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("You don't have the permission to do that.");
            return false;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(sender, args);
            return true;
        }

        if (args.length < 2) {
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig();
                plugin.restartPlugin();
                return true;
            } else {
                sender.sendMessage("Usage: /elytraOnSpawn config <key> [value]");
            }
            return false;
        }

        if (args[0].equalsIgnoreCase("config") || args[0].equalsIgnoreCase("conf")) {
            return !parseFirstArgument(sender, args);
        } else {
            sender.sendMessage("Invalid argument: " + args[0]);
            return false;
        }
    }

    private void sendHelpMessage(CommandSender sender, String[] args) {
        if (args.length == 1) {
            sendGeneralHelp(sender);
        } else {
            String subCommand = args[1].toLowerCase();
            switch (subCommand) {
                case "reload":
                    sendReloadHelp(sender);
                    break;
                case "config":
                    sendConfigHelp(sender);
                    break;
                default:
                    sender.sendMessage("§cUnknown command. Type '/elytraonspawn help' for general help.");
            }
        }
    }

    private void sendGeneralHelp(CommandSender sender) {
        sender.sendMessage("§6=== ElytraOnSpawn Help ===");
        sender.sendMessage("§fAvailable commands:");
        sender.sendMessage("§f/elytraonspawn help [command] §7- Display help for a specific command");
        sender.sendMessage("§f/elytraonspawn reload §7- Reload the plugin");
        sender.sendMessage("§f/elytraonspawn config §7- Manage plugin configuration");
        sender.sendMessage("§7Type '/elytraonspawn help <command>' for more details on each command.");
    }

    private void sendReloadHelp(CommandSender sender) {
        sender.sendMessage("§6=== ElytraOnSpawn Reload Help ===");
        sender.sendMessage("§fUsage: /elytraonspawn reload");
        sender.sendMessage("§7This command reloads the plugin configuration and restarts the plugin.");
        sender.sendMessage("§7Use this after making changes to the config file or when you need to reset the plugin state.");
    }

    private void sendConfigHelp(CommandSender sender) {
        sender.sendMessage("§6=== ElytraOnSpawn Config Help ===");
        sender.sendMessage("§fUsage:");
        sender.sendMessage("§f/elytraonspawn config <key> §7- View a config value");
        sender.sendMessage("§f/elytraonspawn config <key> <value> §7- Set a config value");
        sender.sendMessage("§f/elytraonspawn config reset §7- Reset the config to default");
        sender.sendMessage("§f/elytraonspawn config check §7- Check the config for errors");
        sender.sendMessage("§fAvailable config keys:");
        for (String key : configKeys) {
            String description = getConfigKeyDescription(key);
            sender.sendMessage("§f" + key + " §7- " + description);
        }
    }

    private String getConfigKeyDescription(String key) {
        switch (key.toLowerCase()) {
            case "verbose":
                return "Enable/disable verbose logging. When enabled, provides more detailed plugin operation logs. (Boolean: true/false)";
            case "spawnradius":
                return "Set the spawn radius (in blocks) within which the elytra boost is unlimited. Players within this radius from spawn will be able to start to fly with their elytra. (Integer: positive number)";
            case "flyboostmultiplier":
                return "Set the fly boost multiplier. This affects the strength of the boost while flying. Higher values result in stronger boosts. (Float: 1.0 or higher)";
            case "startboostmultiplier":
                return "Set the start boost multiplier. This affects the initial boost strength when taking off. Higher values result in stronger initial boosts. (Float: 1.0 or higher)";
            case "world":
                return "Set the world name where the plugin feature is active. The plugin will only work in the specified world. (String: world name)";
            case "boostsoundsetter":
                return "Enable/disable the sound played when a boost is applied. (Boolean: true/false)";
            case "boostsound":
                return "Set the sound effect played when a fly is applied. (String: sound name)";
            case "switchgamemodecancelsoundsetter":
                return "Enable/disable the sound played when a boost is cancelled due to gamemode switch. (Boolean: true/false)";
            case "switchgamemodecancelsound":
                return "Set the sound effect played when a boost is cancelled due to gamemode switch. (String: sound name)";
            case "particle":
                return "Enable/disable particle effects displayed during elytra flight. (Boolean: true/false)";
            default:
                return "No description available for this configuration key.";
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            return allCommands.stream()
                    .filter(c -> c.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("help")) {
                return allCommands.stream()
                        .filter(c -> c.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("config") || args[0].equalsIgnoreCase("conf")) {
                List<String> options = new ArrayList<>(configCommands);
                options.addAll(configKeys);
                return options.stream()
                        .filter(c -> c.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("config") || args[0].equalsIgnoreCase("conf"))) {
            switch (args[1].toLowerCase()) {
                case "verbose":
                case "boostsoundsetter":
                case "switchgamemodecancelsoundsetter":
                case "particle":
                    return Arrays.asList("true", "false").stream()
                            .filter(c -> c.startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
                case "world":
                case "boostsound":
                case "switchgamemodecancelsound":
                    return null;
                case "spawnradius":
                case "flyboostmultiplier":
                case "startboostmultiplier":
                    return Arrays.asList("<value>");
                default:
                    return completions;
            }
        }

        return completions;
    }

    private boolean parseFirstArgument(CommandSender sender, String[] args) {
        String firstArgument = args[1];
        switch (firstArgument.toLowerCase()) {
            case ("reset"):
                plugin.deleteConfig();
                plugin.restartPlugin();
                return true;

            case ("check"):
                plugin.configCheck();
                sender.sendMessage("Please check your server console");
                return true;

            default:
                if (args.length == 2) {
                    Object value = plugin.getConfigValue(config, firstArgument);
                    if (value != null) {
                        sender.sendMessage("Current value of '" + firstArgument + "': " + value);
                    } else {
                        sender.sendMessage("Configuration key '" + firstArgument + "' not found.");
                    }
                } else if (args.length == 3) {
                    if (parseSecondArgument(sender, firstArgument, args[2])) return true;
                } else {
                    sender.sendMessage("Too many arguments. Usage: /elytraOnSpawn config <key> [value]");
                    return true;
                }
        }
        return false;
    }

    private boolean parseSecondArgument(CommandSender sender, String firstArgument, String secondArgument) {
        switch (firstArgument.toLowerCase()) {
            case "verbose", "boostsoundsetter", "switchgamemodecancelsoundsetter", "particle":
                boolean boolValue = Boolean.parseBoolean(secondArgument);
                plugin.setConfigValue(config, firstArgument, boolValue);
                break;
            case "spawnradius":
                int intValue = Integer.parseInt(secondArgument);
                plugin.setConfigValue(config, firstArgument, intValue);
                break;
            case "flyboostmultiplier", "startboostmultiplier", "startsoundboost":
                float floatValue = Float.parseFloat(secondArgument);
                if (floatValue < 1) {
                    sender.sendMessage("Value must be 1 or higher.");
                    break;
                }
                plugin.setConfigValue(config, firstArgument, floatValue);
                break;
            case "world", "boostsound", "switchgamemodecancelsound":
                plugin.setConfigValue(config, firstArgument, secondArgument);
                break;
            default:
                sender.sendMessage("Unknown configuration key: " + firstArgument);
                return true;
        }

        try {
            config.save(configFile);
            sender.sendMessage("Configuration updated: " + firstArgument + " = " + secondArgument);
            plugin.reloadConfig();
        } catch (IOException e) {
            sender.sendMessage("Error saving configuration: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
