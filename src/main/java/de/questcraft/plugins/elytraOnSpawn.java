package de.questcraft.plugins;

import org.bukkit.plugin.java.JavaPlugin;
import de.questcraft.plugins.listener.SpawnBoostListener;

public final class elytraOnSpawn extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new SpawnBoostListener(this), this);
    }
}
