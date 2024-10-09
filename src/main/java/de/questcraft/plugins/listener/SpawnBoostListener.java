package de.questcraft.plugins.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
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

public class SpawnBoostListener implements Listener {

    private float flyBoostMultiplier;
    private int spawnRadius;
    private String world;
    private float startBoostMultiplier;
    private boolean boostSoundSetter;
    private String boostSound;
    private int boostSoundVolume;
    private int boostSoundPitch;
    private boolean switchGamemodeCancelSoundSetter;
    private String switchGamemodeCancelSound;
    private int switchGamemodeCancelSoundVolume;
    private int switchGamemodeCancelSoundPitch;
    private boolean particle;
    private final List<Entity> flying = new ArrayList<>();
    private final List<Player> boosted = new ArrayList<>();

    public SpawnBoostListener(final Plugin plugin) {
        loadInConfig(plugin);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Bukkit.getWorld(world).getPlayers().forEach(player -> {

                if (player.getGameMode() != GameMode.SURVIVAL) return;

                player.setAllowFlight(isInSpawnRadius(player));

                final boolean isPlayerInAir = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isAir();

                final boolean isFirstContact = flying.contains(player) && !isPlayerInAir;
                final boolean hasSpawnPrivileges = isInSpawnRadius(player) && boosted.contains(player) && !isPlayerInAir;

                if (!isFirstContact && !hasSpawnPrivileges) return;

                // set anticheat permissions / animation
                player.setAllowFlight(false);
                player.setFlying(false);
                player.setGliding(false);

                boosted.remove(player);
                Bukkit.getScheduler().runTaskLater(plugin, () -> flying.remove(player), 5);
            });
        }, 0, 3);
    }


    @EventHandler
    public void onDoubleJump(final PlayerToggleFlightEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
        if (!isInSpawnRadius(event.getPlayer())) return;
        event.setCancelled(true);

        event.getPlayer().setGliding(true);
        boostPlayer(event.getPlayer(), false);
        flying.add(event.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        if (player.isGliding() && flying.contains(player) && particle)
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 3, 0, 0, 0, 0.05);
    }

    @EventHandler
    public void onGamemodeSwitch(final PlayerGameModeChangeEvent event) {
        final Player player = event.getPlayer();
        if (event.getNewGameMode() != GameMode.CREATIVE) player.setAllowFlight(false);
        player.setFlying(false);
        player.setGliding(false);
        boosted.remove(player);
        flying.remove(player);
        if (switchGamemodeCancelSoundSetter)
            player.playSound(player.getLocation(), SoundMapper.getSound(switchGamemodeCancelSound), switchGamemodeCancelSoundVolume, switchGamemodeCancelSoundPitch);
    }

    @EventHandler
    public void onDamage(final EntityDamageEvent event) {
        final Entity entity = event.getEntity();
        final boolean isPlayer = entity.getType() == EntityType.PLAYER;
        final boolean isFlyingDamage = event.getCause() == EntityDamageEvent.DamageCause.FALL || event.getCause() == EntityDamageEvent.DamageCause.FLY_INTO_WALL;

        if (isPlayer && isFlyingDamage && flying.contains(entity)) event.setCancelled(true);
    }

    @EventHandler
    public void onSwapItem(final PlayerSwapHandItemsEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
        if (boosted.contains(event.getPlayer()) && !isInSpawnRadius(event.getPlayer())) return;

        final boolean isPlayerOnGround = !event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isAir();
        final boolean infiniteBoost = isInSpawnRadius(event.getPlayer()) && isPlayerOnGround;
        final boolean normalBoost = flying.contains(event.getPlayer()) && !isPlayerOnGround;

        if (!infiniteBoost && !normalBoost) return;
        event.setCancelled(true);
        boostPlayer(event.getPlayer(), true);
        if (!infiniteBoost) boosted.add(event.getPlayer());
    }

    @EventHandler
    public void onToggleGlide(final EntityToggleGlideEvent event) {
        if (event.getEntity().getType() == EntityType.PLAYER && flying.contains(event.getEntity()))
            event.setCancelled(true);
    }

    private boolean isInSpawnRadius(final Player player) {
        if (!player.getWorld().getName().equals(world)) return false;

        final double distance = player.getWorld().getSpawnLocation().distance(player.getLocation());
        return distance < spawnRadius;
    }

    public void boostPlayer(final Player player, final boolean isBoost) {
        final float boosMultiplier = isBoost ? flyBoostMultiplier : startBoostMultiplier;
        player.setVelocity(player.getLocation().getDirection().multiply(boosMultiplier));

        if (boostSoundSetter)
            player.playSound(player.getLocation(), SoundMapper.getSound(boostSound), boostSoundVolume, boostSoundPitch);
    }

    public void loadInConfig(Plugin plugin) {
        FileConfiguration config = plugin.getConfig();

        this.flyBoostMultiplier = config.getInt("flyBoostMultiplier");
        this.spawnRadius = config.getInt("spawnRadius");
        this.startBoostMultiplier = config.getInt("startBoostMultiplier");
        this.world = config.getString("world");
        this.boostSoundSetter = config.getBoolean("boostSoundSetter");
        this.boostSound = config.getString("boostSound");
        this.boostSoundVolume = config.getInt("boostSoundVolume");
        this.boostSoundPitch = config.getInt("boostSoundPitch");
        this.switchGamemodeCancelSoundSetter = config.getBoolean("switchGamemodeCancelSoundSetter");
        this.switchGamemodeCancelSound = config.getString("switchGamemodeCancelSound");
        this.switchGamemodeCancelSoundVolume = config.getInt("switchGamemodeCancelSoundVolume");
        this.switchGamemodeCancelSoundPitch = config.getInt("switchGamemodeCancelSoundPitch");
        this.particle = config.getBoolean("particle");
    }
}