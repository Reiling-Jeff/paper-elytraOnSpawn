package de.questcraft.plugins.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.Plugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.bukkit.Bukkit.getLogger;

public class SpawnBoostListener implements Listener{

    private final float flyBoostMultiplier;
    private final int spawnRadius;
    private final String world;
    private final float startBoostMultiplier;
    private final List<Player> flying = new ArrayList<>();
    private final List<Player> boosted = new ArrayList<>();

    public SpawnBoostListener(Plugin plugin) {
        this.flyBoostMultiplier = plugin.getConfig().getInt("flyBoostMultiplier");
        this.spawnRadius = plugin.getConfig().getInt("spawnRadius");
        this.startBoostMultiplier = plugin.getConfig().getInt("startBoostMultiplier");
        this.world = plugin.getConfig().getString("world");

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if(world != null) {
                Objects.requireNonNull(Bukkit.getWorld(world)).getPlayers().forEach(player -> {
                    if (player.getGameMode() != GameMode.SURVIVAL) return;
                    player.setAllowFlight(isInSpawnRadius(player));
                    if ((flying.contains(player) && !player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isAir())
                            || (isInSpawnRadius(player.getPlayer()) && boosted.contains(player.getPlayer()) && !player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isAir())) {
                        player.setAllowFlight(false);
                        player.setFlying(false);
                        player.setGliding(false);
                        boosted.remove(player);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            flying.remove(player);
                        }, 5);
                    }
                });
            } else getLogger().severe("Please make sure you added the right world name in config.yml. Default: \"world: world\"(you cant add multiple worlds yet)");
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
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.PLAYER
        && (event.getCause() == EntityDamageEvent.DamageCause.FALL || event.getCause() == EntityDamageEvent.DamageCause.FLY_INTO_WALL)
        && flying.contains(event.getEntity())) event.setCancelled(true);
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
        if(event.getEntityType() == EntityType.PLAYER && flying.contains(event.getEntity())) event.setCancelled(true);
    }


    private boolean isInSpawnRadius(Player player) {
        if(!player.getWorld().getName().equals(world)) return false;
        return  player.getWorld().getSpawnLocation().distance(player.getLocation()) < spawnRadius;
    }

    public void boostPlayer(Player player, boolean isBoost) {
        if (isBoost) player.setVelocity(player.getLocation().getDirection().multiply(flyBoostMultiplier));
        else player.setVelocity(player.getLocation().getDirection().multiply(startBoostMultiplier));
    }
}
