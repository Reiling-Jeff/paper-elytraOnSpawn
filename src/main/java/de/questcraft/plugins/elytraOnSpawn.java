package de.questcraft.plugins;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import de.questcraft.plugins.listener.SpawnBoostListener;

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

        verbose = config.getBoolean("verbose", false);
        log.info("Verbose mode: " + verbose);

        configCheck();

        getServer().getPluginManager().registerEvents(new SpawnBoostListener(this), this);

        log.info("ElytraOnSpawn is ready!");
    }

    private void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
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
                String typeError = "";

                switch (path) {
                    case "verbose":
                    case "boostSound":
                    case "switchGamemodeCancelSound":
                    case "particle":
                        isValid = value instanceof Boolean;
                        typeError = "should be true or false";
                        break;
                    case "spawnRadius":
                    case "boostSoundVolume":
                    case "boostSoundPitch":
                    case "switchGamemodeCancelSoundVolume":
                    case "switchGamemodeCancelSoundPitch":
                        isValid = value instanceof Number;
                        typeError = "should be a number";
                        break;
                    case "flyBoostMultiplier":
                    case "startBoostMultiplier":
                        isValid = value instanceof Number && ((Number) value).doubleValue() > 0;
                        typeError = "should be a positive number";
                        break;
                    case "world":
                        isValid = value instanceof String;
                        typeError = "should be a string";
                        break;
                }

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
    public FileConfiguration getConfig() {
        return config;
    }
}