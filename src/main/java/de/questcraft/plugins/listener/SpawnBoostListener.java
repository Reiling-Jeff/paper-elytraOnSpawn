package de.questcraft.plugins.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.Plugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getLogger;

public class SpawnBoostListener implements Listener{

    private float flyBoostMultiplier;
    private int spawnRadius;
    private String world;
    private float startBoostMultiplier;
    private boolean boostSound;
    private int boostSoundVolume;
    private int boostSoundPitch;
    private boolean switchGamemodeCancelSound;
    private int switchGamemodeCancelSoundVolume;
    private int switchGamemodeCancelSoundPitch;
    private boolean particle;
    private final List<Entity> flying = new ArrayList<>();
    private final List<Player> boosted = new ArrayList<>();

    public SpawnBoostListener(Plugin plugin) {
        loadInConfig(plugin);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                Objects.requireNonNull(Bukkit.getWorld(world)).getPlayers().forEach(player -> {

                if (player.getGameMode() != GameMode.SURVIVAL) return;

                player.setAllowFlight(isInSpawnRadius(player));

                if ((flying.contains(player) && !player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isAir())
                        || (isInSpawnRadius(Objects.requireNonNull(player.getPlayer())) && boosted.contains(player.getPlayer()) && !player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isAir())) {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    player.setGliding(false);
                    boosted.remove(player);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> flying.remove(player), 5);
                }
                });
        }, 0, 3);
    }

    @EventHandler
    public void onDoubleJump(PlayerToggleFlightEvent event) {
        if(event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
        if(!isInSpawnRadius(event.getPlayer())) return;
        event.setCancelled(true);
        event.getPlayer().setGliding(true);
        flying.add(event.getPlayer());
        boostPlayer(event.getPlayer(), false);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(player.isGliding() && flying.contains(player) && particle) {
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 3, 0, 0, 0, 0.05);
        }
    }

    @EventHandler
    public void OnGamemodeSwitch(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if(event.getNewGameMode() != GameMode.CREATIVE) player.setAllowFlight(false);
        player.setFlying(false);
        player.setGliding(false);
        boosted.remove(player);
        flying.remove(player);
        if(switchGamemodeCancelSound) {
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_DESTROY, switchGamemodeCancelSoundVolume, switchGamemodeCancelSoundPitch);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if ((entity.getType() == EntityType.PLAYER)
        && (event.getCause() == EntityDamageEvent.DamageCause.FALL || event.getCause() == EntityDamageEvent.DamageCause.FLY_INTO_WALL)
        && flying.contains(entity))
            event.setCancelled(true);
    }

    @EventHandler
    public void onSwapItem(PlayerSwapHandItemsEvent event) {
        if(event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
        if(boosted.contains(event.getPlayer()) && !isInSpawnRadius(event.getPlayer())) return;
        if((flying.contains(event.getPlayer()) && event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isAir())
            ||(isInSpawnRadius(event.getPlayer()) && !event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isAir()))
        {
            event.setCancelled(true);
            if(event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isAir() || isInSpawnRadius(event.getPlayer())) boosted.add(event.getPlayer());
            boostPlayer(event.getPlayer(), true);
        }
    }

    @EventHandler
    public void onToggleGlide(EntityToggleGlideEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() == EntityType.PLAYER && flying.contains(entity)) event.setCancelled(true);
    }

    private boolean isInSpawnRadius(Player player) {
        if(!player.getWorld().getName().equals(world)) return false;
        return  player.getWorld().getSpawnLocation().distance(player.getLocation()) < spawnRadius;
    }

    public void boostPlayer(Player player, boolean isBoost) {
        if (isBoost) {
            player.setVelocity(player.getLocation().getDirection().multiply(flyBoostMultiplier));
        }
        else player.setVelocity(player.getLocation().getDirection().multiply(startBoostMultiplier));
        if(boostSound) {
            player.playSound(player.getLocation(), Sound.ENTITY_BREEZE_WIND_BURST, boostSoundVolume, boostSoundPitch);
        }
    }

    public void loadInConfig(Plugin plugin) {
        FileConfiguration config = plugin.getConfig();

        this.flyBoostMultiplier = config.getInt("flyBoostMultiplier");
        this.spawnRadius = config.getInt("spawnRadius");
        this.startBoostMultiplier = config.getInt("startBoostMultiplier");
        this.world = config.getString("world");
        this.boostSound = config.getBoolean("boostSound");
        this.boostSoundVolume = config.getInt("boostSoundVolume");
        this.boostSoundPitch = config.getInt("boostSoundPitch");
        this.switchGamemodeCancelSound = config.getBoolean("switchGamemodeCancelSound");
        this.switchGamemodeCancelSoundVolume = config.getInt("switchGamemodeCancelSoundVolume");
        this.switchGamemodeCancelSoundPitch = config.getInt("switchGamemodeCancelSoundPitch");
        this.particle = config.getBoolean("particle");
    }
}