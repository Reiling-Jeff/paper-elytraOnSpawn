package de.questcraft.plugins.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
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

    private final float flyBoostMultiplier;
    private final int spawnRadius;
    private final String world;
    private final float startBoostMultiplier;
    private final boolean boostSound;
    private final int boostSoundVolume;
    private final int boostSoundPitch;
    private final boolean switchGamemodeCancelSound;
    private final int switchGamemodeCancelSoundVolume;
    private final int switchGamemodeCancelSoundPitch;
    private final boolean particle;
    private final List<Entity> flying = new ArrayList<>();
    private final List<Player> boosted = new ArrayList<>();

    private final Logger logger;

    public SpawnBoostListener(Plugin plugin) {

        this.flyBoostMultiplier = plugin.getConfig().getInt("flyBoostMultiplier");
        this.spawnRadius = plugin.getConfig().getInt("spawnRadius");
        this.startBoostMultiplier = plugin.getConfig().getInt("startBoostMultiplier");
        this.world = plugin.getConfig().getString("world");
        this.boostSound = plugin.getConfig().getBoolean("boostSound");
        this.boostSoundVolume = plugin.getConfig().getInt("boostSoundVolume");
        this.boostSoundPitch = plugin.getConfig().getInt("boostSoundPitch");
        this.switchGamemodeCancelSound = plugin.getConfig().getBoolean("switchGamemodeCancelSound");
        this.switchGamemodeCancelSoundVolume = plugin.getConfig().getInt("switchGamemodeCancelSoundVolume");
        this.switchGamemodeCancelSoundPitch = plugin.getConfig().getInt("switchGamemodeCancelSoundPitch");
        this.particle = plugin.getConfig().getBoolean("particle");

        this.logger = getLogger();

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if(world != null) {
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
            } else logger.severe("Please make sure you added the right world name in config.yml. Default: \"world: world\"(you cant add multiple worlds yet)");
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
}