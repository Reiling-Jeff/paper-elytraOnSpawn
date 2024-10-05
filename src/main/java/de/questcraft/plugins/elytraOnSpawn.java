package de.questcraft.plugins;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import de.questcraft.plugins.listener.SpawnBoostListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public final class elytraOnSpawn extends JavaPlugin {

    private final Logger log = getLogger();
    ArrayList<String> missingConfigs = new ArrayList<String>();
    private String MSG;
    private boolean verbose;


    @Override
    public void onEnable() {
        verbose = getConfig().getBoolean("verbose");
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new SpawnBoostListener(this), this);
        if(verbose)configCheck();
        log.info("ElytraOnSpawn is ready!");
    }

    public void configCheck() {
        FileConfiguration config = this.getConfig();

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

        for (String path : expectedVariables) {
            if (!config.contains(path)) {
                log.severe("Missing configuration: " + path);
            }
        }
    }
}
