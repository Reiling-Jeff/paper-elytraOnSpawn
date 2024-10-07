package de.questcraft.plugins.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import de.questcraft.plugins.elytraOnSpawn;
import org.bukkit.configuration.file.FileConfiguration;

import javax.management.StringValueExp;
import java.io.File;
import java.io.IOException;

public class elytraOnSpawnCommand implements CommandExecutor {
    private final elytraOnSpawn plugin;
    private final FileConfiguration config;
    private final File configFile;

    public elytraOnSpawnCommand(elytraOnSpawn plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /elytraOnSpawn config <key> [value]");
            return false;
        }

        if (args[0].equalsIgnoreCase("config") || args[0].equalsIgnoreCase("conf")) {
            String key = args[1].toLowerCase();

            if (args.length == 2) {
                Object value = config.get(key);
                if (value != null) {
                    sender.sendMessage("Current value of '" + key + "': " + value);
                } else {
                    sender.sendMessage("Configuration key '" + key + "' not found.");
                }
            }else if (args.length == 3) {
                String value = args[2];

                switch (key) {
                    case "verbose", "boostsound", "switchgamemodecancelsound", "particle":
                        boolean boolValue = Boolean.parseBoolean(value);
                        config.set(key, boolValue);
                        break;
                    case "spawnradius", "boostsoundvolume", "boostsoundpitch", "switchgamemodecancelsoundvolume", "switchgamemodecancelsoundpitch":
                        int intValue = Integer.parseInt(value);
                        config.set(key, intValue);
                        break;
                    case "flyboostmultiplier", "startsoundboost":
                        float floatValue = Float.parseFloat(value);
                        if(floatValue < 1) {
                            sender.sendMessage("Value must be 1 or higher.");
                            break;
                        }
                        config.set(key, floatValue);
                        break;
                    case "world":
                        config.set(key, value);
                    default:
                        sender.sendMessage("Unknown configuration key: " + key);
                        return false;
                }

                try {
                    config.save(configFile);
                    sender.sendMessage("Configuration updated: " + key + " = " + value);
                    plugin.reloadConfig();
                } catch (IOException e) {
                    sender.sendMessage("Error saving configuration: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                sender.sendMessage("Too many arguments. Usage: /elytraOnSpawn config <key> [value]");
                return false;
            }
        } else {
            sender.sendMessage("Invalid argument: " + args[0]);
            return false;
        }

        return true;
    }
}