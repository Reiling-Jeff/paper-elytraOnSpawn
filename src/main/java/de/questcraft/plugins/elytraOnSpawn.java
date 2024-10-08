package de.questcraft.plugins;


import de.questcraft.plugins.commands.elytraOnSpawnCommand;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import de.questcraft.plugins.listener.SpawnBoostListener;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public final class elytraOnSpawn extends JavaPlugin {

    private final Logger log = getLogger();
    ArrayList<String> missingConfigs = new ArrayList<>();
    private boolean verbose;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        log.info("ElytraOnSpawn is starting...");

        saveDefaultConfig();
        loadConfig();

        try {
            verbose = config.getBoolean("verbose", false);
        } catch (Exception e) {
            verbose = true;
        }
        log.info("Verbose mode: " + verbose);

        configCheck();
        getServer().getPluginManager().registerEvents(new SpawnBoostListener(this), this);
        log.info("Loading elytraOnSpawn commands");
        getCommand("elytraOnSpawn").setExecutor(new elytraOnSpawnCommand(this));
        log.info("Commands loaded!");

        log.info("ElytraOnSpawn is ready!");
    }

    public void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void forceSaveDefaultConfig() {
        File configFile = new File(this.getDataFolder(), "config.yml");

        if (configFile.exists()) {
            if (configFile.delete()) {
                this.getLogger().info("Existing config.yml deleted.");
            } else {
                this.getLogger().warning("can't delete config.yml,");
                return;
            }
            if (verbose) configCheck();
        }

        this.saveDefaultConfig();

        this.reloadConfig();
    }

    public void restartPlugin() {
        this.getLogger().info("trying to restart the plugin..");

        getServer().getPluginManager().disablePlugin(this);

        try {
            getServer().getPluginManager().enablePlugin(this);
            this.getLogger().info("Plugin restarted success");
        } catch (Exception e) {
            this.getLogger().severe("Something went wrong: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void configCheck() {
        log.info("Performing config check...");

        List<String> expectedVariables = Arrays.asList(
                "verbose",
                "spawnRadius",
                "flyBoostMultiplier",
                "startBoostMultiplier",
                "world",
                "boostSound",
                "boostSoundVolume",
                "boostSoundPitch",
                "switchGamemodeCancelSound",
                "switchGamemodeCancelSoundVolume",
                "switchGamemodeCancelSoundPitch",
                "particle"
        );

        missingConfigs.clear();
        for (String path : expectedVariables) {
            if (!config.contains(path) && verbose) {
                log.severe("Missing configuration: " + path);
                missingConfigs.add(path);
            } else {
                Object value = config.get(path);
                boolean isValid = true;
                String typeError = switch (path) {
                    case "verbose", "boostSound", "switchGamemodeCancelSound", "particle" -> {
                        isValid = value instanceof Boolean;
                        yield "should be true or false";
                    }
                    case "spawnRadius", "boostSoundVolume", "boostSoundPitch", "switchGamemodeCancelSoundVolume",
                         "switchGamemodeCancelSoundPitch" -> {
                        isValid = value instanceof Number;
                        yield "should be a number";
                    }
                    case "flyBoostMultiplier", "startBoostMultiplier" -> {
                        isValid = value instanceof Number && ((Number) value).doubleValue() > 0;
                        yield "should be a positive number";
                    }
                    case "world" -> {
                        isValid = value instanceof String;
                        yield "should be a string";
                    }
                    default -> "";
                };

                if (!isValid) {
                    log.severe("Invalid configuration for " + path + ": " + typeError);
                    missingConfigs.add(path);
                } else if (verbose) {
                    log.info("Config found and valid: " + path + " = " + value);
                }
            }
        }

        if (!missingConfigs.isEmpty()) {
            log.warning("Invalid or missing configurations: " + String.join(", ", missingConfigs));
            log.warning("Plugin may not function correctly.");
        } else {
            log.info("All configurations are present and valid.");
        }
    }

    @Override
    public @NotNull FileConfiguration getConfig() {
        return config;
    }
}