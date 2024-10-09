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

// TODO TabCompleter for ElytraOnSpawnCommand
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
            "boostsoundvolume",
            "boostsoundpitch",
            "switchgamemodecancelsoundsetter",
            "switchgamemodecancelsound",
            "switchgamemodecancelsoundvolume",
            "switchgamemodecancelsoundpitch",
            "particle"
    );

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

        if (args.length < 2) {
            // TODO Create a "/elytraOnSpawn Help" command
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

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            return mainCommands.stream()
                    .filter(c -> c.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("config") || args[0].equalsIgnoreCase("conf"))) {
            List<String> options = new ArrayList<>(configCommands);
            options.addAll(configKeys);
            return options.stream()
                    .filter(c -> c.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("config") || args[0].equalsIgnoreCase("conf"))) {
            switch (args[1].toLowerCase()) {
                case "verbose", "boostsoundsetter", "switchgamemodecancelsoundsetter", "particle":
                    return Arrays.asList("true", "false");
                case "world", "boostsound", "switchgamemodecancelsound":
                    return null;
                default:
                    return Arrays.asList("<value>");
            }
        }

        return completions;
    }

    private boolean parseFirstArgument(CommandSender sender, String[] args) {
        String firstArgument = args[1];
        switch (firstArgument.toLowerCase()) {
            case ("reset"):
                plugin.deleteConfig();
                plugin.saveDefaultConfig();
                plugin.restartPlugin();
                return true;

            case ("check"):
                plugin.configCheck();
                sender.sendMessage("Please check your server console");
                return true;

            default:
                if (args.length == 2) {
                    Object value = config.get(firstArgument);
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
                config.set(firstArgument, boolValue);
                break;
            case "spawnradius", "boostsoundvolume", "boostsoundpitch",
                 "switchgamemodecancelsoundvolume", "switchgamemodecancelsoundpitch":
                int intValue = Integer.parseInt(secondArgument);
                config.set(firstArgument, intValue);
                break;
            case "flyboostmultiplier", "startsoundboost":
                float floatValue = Float.parseFloat(secondArgument);
                if (floatValue < 1) {
                    sender.sendMessage("Value must be 1 or higher.");
                    break;
                }
                config.set(firstArgument, floatValue);
                break;
            case "world", "boostsound", "switchgamemodecancelsound":
                config.set(firstArgument, secondArgument);
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
