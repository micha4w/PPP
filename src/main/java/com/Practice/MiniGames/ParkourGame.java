package com.Practice.MiniGames;

import com.Practice.Items.ItemType;
import com.Practice.PracticePlugin;
import com.Practice.Useful.Items;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;

public class ParkourGame implements Listener {

    public static Map<Player, ParkourPlaying> playingPlayers = new HashMap<>();

    public static void addPlayer (Player player, Location loc) {
        ParkourPlaying playing = new ParkourPlaying(loc);

        player.getInventory().clear();
        player.getInventory().setItem(8, PracticePlugin.guiItems.get(ItemType.CLOCKITEM));
        player.getInventory().setItem(7, PracticePlugin.guiItems.get(ItemType.LEAVE));
        player.getInventory().setItem(0, PracticePlugin.guiItems.get(ItemType.RESTART));

        playingPlayers.put(player, playing);

        resetPlayer(player);
    }

    public static void resetPlayer (Player player) {
        ParkourPlaying playing = playingPlayers.get(player);


        player.teleport(playing.restart.clone().add(0.5, 0, 0.5));
//        player.spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 1);
    }

    public static void removePlayer (Player player) {
        playingPlayers.remove(player);

        player.teleport(Bukkit.getWorld("world").getSpawnLocation());
        player.getInventory().clear();
        player.getInventory().setItem(4, PracticePlugin.guiItems.get(ItemType.CLOCKITEM));
    }

    @EventHandler
    public void onDamage (EntityDamageEvent event) {
        if ( !(event.getEntity() instanceof Player) ) return;

        Player player = (Player) event.getEntity();
        ParkourPlaying playing = playingPlayers.get(player);

        if ( playing != null ) {
            if ( event.getCause() == EntityDamageEvent.DamageCause.VOID ) {
                resetPlayer(player);
            }
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onClick (PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ParkourPlaying playing = playingPlayers.get(player);
        if ( playing != null ) {
            ItemType type = Items.getTypeNBT(event.getItem());
            if ( type != null ) {
                switch (type) {
                    case RESTART:
                        resetPlayer(player);
                        break;
                    case LEAVE:
                        removePlayer(player);
                        break;
                }
            }
        }
    }

    @EventHandler
    public void onMove (PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ParkourPlaying playing = playingPlayers.get(player);

        if ( playing != null ) {

            if ( event.getTo() == null ) return;

            Block block = event.getTo().getBlock();
            Location loc = block.getLocation();
            loc.setYaw(event.getTo().getYaw());

            block = block.getRelative(0, -1, 0);

            if (block.getType() == Material.DIAMOND_BLOCK) {
                if ( playing.restart.getBlockX() != loc.getBlockX() || playing.restart.getBlockY() != loc.getBlockY() || playing.restart.getBlockZ() != loc.getBlockZ() ) {
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "You beat the parkour!");
                    playing.addCheckpoint(loc);
                }
            } else if (block.getType() == Material.EMERALD_BLOCK) {
                if ( playing.restart.getBlockX() != loc.getBlockX() || playing.restart.getBlockY() != loc.getBlockY() || playing.restart.getBlockZ() != loc.getBlockZ() ) {
                    playing.addCheckpoint(loc);
                    player.sendMessage(ChatColor.GREEN + "You got a checkpoint! " + ChatColor.GOLD + "(You can do /undocheckpoint)");
                }
            }
        }
    }

    public static class ParkourPlaying {
        Location start;
        Location restart;
        Location lastRestart;

        public ParkourPlaying (Location start) {
            this.start = this.restart = start;
            this.lastRestart = null;
        }

        public void addCheckpoint (Location newLoc) {
            lastRestart = restart;
            restart = newLoc;
        }

        public boolean removeCheckpoint () {
            if ( lastRestart !=  null ) {
                restart = lastRestart;
                return true;
            } else {
                return false;
            }
        }
    }

}
