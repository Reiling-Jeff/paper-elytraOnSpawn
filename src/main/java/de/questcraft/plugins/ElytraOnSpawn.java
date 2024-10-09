package de.questcraft.plugins;


import de.questcraft.plugins.commands.ElytraOnSpawnCommand;
import de.questcraft.plugins.listener.SpawnBoostListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Logger;

public final class ElytraOnSpawn extends JavaPlugin {

    private final Logger log = getLogger();
    private boolean verbose = true;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        log.info("ElytraOnSpawn is starting...");

        saveDefaultConfig();
        loadConfig();
        configCheck();

        log.info("Loading elytraOnSpawn events...");
        getServer().getPluginManager().registerEvents(new SpawnBoostListener(this), this);

        log.info("Loading elytraOnSpawn commands...");
        getCommand("elytraOnSpawn").setExecutor(new ElytraOnSpawnCommand(this));

        log.info("ElytraOnSpawn is ready!");
    }

    public void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
    }


    private static @NotNull Map<String, ConfigType> getConfigTypeMap() {
        Map<String, ConfigType> expectedVariableTypes = new HashMap<>();
        expectedVariableTypes.put("spawnRadius", ConfigType.NUMBER);
        expectedVariableTypes.put("flyBoostMultiplier", ConfigType.POSITIVE_NUMBER);
        expectedVariableTypes.put("startBoostMultiplier", ConfigType.POSITIVE_NUMBER);
        expectedVariableTypes.put("world", ConfigType.STRING);
        expectedVariableTypes.put("boostSoundSetter", ConfigType.BOOLEAN);
        expectedVariableTypes.put("boostSound", ConfigType.STRING);
        expectedVariableTypes.put("boostSoundVolume", ConfigType.NUMBER);
        expectedVariableTypes.put("boostSoundPitch", ConfigType.NUMBER);
        expectedVariableTypes.put("switchGamemodeCancelSoundSetter", ConfigType.BOOLEAN);
        expectedVariableTypes.put("switchGamemodeCancelSound", ConfigType.STRING);
        expectedVariableTypes.put("switchGamemodeCancelSoundVolume", ConfigType.NUMBER);
        expectedVariableTypes.put("switchGamemodeCancelSoundPitch", ConfigType.NUMBER);
        expectedVariableTypes.put("particle", ConfigType.BOOLEAN);
        return expectedVariableTypes;
    }

    public void configCheck() {
        log.info("Performing config check...");

        verbose = config.getBoolean("verbose", true);
        log.info("Verbose mode: " + verbose);

        Map<String, ConfigType> expectedVariableTypes = getConfigTypeMap();

        ArrayList<String> missingConfigs = new ArrayList<>();
        expectedVariableTypes.forEach((variableName, variableType) -> {
            if (verbose && !config.contains(variableName)) {
                log.severe("Missing configuration: " + variableName);
                missingConfigs.add(variableName);
                return;
            }

            boolean isValid = variableType.isValid.test(config.get(variableName));
            if (verbose && isValid) log.info("Config found and valid: " + variableName + " = " + config.get(variableName));
            if (isValid) return;

            String typeError = variableType.message;
            log.severe("Invalid configuration for " + variableName + ": " + typeError);
            missingConfigs.add(variableName);
        });

        if (!missingConfigs.isEmpty()) {
            log.warning("Invalid or missing configurations: " + String.join(", ", missingConfigs));
            log.warning("Plugin may not function correctly.");
        } else {
            log.info("All configurations are present and valid.");
        }
    }

    public void deleteConfig() {
        File configFile = new File(this.getDataFolder(), "config.yml");
        if (configFile.exists()) {
            if (configFile.delete()) {
                this.getLogger().info("Existing config.yml deleted.");
            } else {
                this.getLogger().warning("can't delete config.yml,");
            }
        }
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

    @Override
    @NotNull
    public FileConfiguration getConfig() {
        return config;
    }

    private record ConfigType(String message, Predicate<Object> isValid) {
        static final ConfigType BOOLEAN = new ConfigType("should be true or false", o -> o instanceof Boolean);
        static final ConfigType NUMBER = new ConfigType("should be a number", o -> o instanceof Number);
        static final ConfigType POSITIVE_NUMBER = new ConfigType("should be a positive number", o -> o instanceof Number n && n.doubleValue() > 0);
        static final ConfigType STRING = new ConfigType("should be a string", o -> o instanceof String);
    }
}