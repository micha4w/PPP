package com.Practice.MiniGames;

import com.Practice.Items.ItemType;
import com.Practice.PracticePlugin;
import com.Practice.Useful.Configs;
import com.Practice.Useful.Inventories;
import com.Practice.Useful.Items;
import com.Practice.Useful.PPPScoreboard;
import net.minecraft.server.v1_16_R3.Tuple;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_16_R3.block.impl.CraftTrapdoor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public class MLGGame implements Listener {

    private static final int mapAmount = 3;
    private static final int ofEachMap = 15;

    public static boolean[][] mapsInUse = new boolean[mapAmount][ofEachMap];
    public static int taskID;

    public static Map<Player, MLGPlaying> playingPlayers = new HashMap<>();
    public static Map<Player, Float> playerFallDistances = new HashMap<>();
    public static List<Player> freezedPlayers = new ArrayList<>();

    public static final String failTag = "PPPFailedMLG";
    public static final String noFallTag = "PPPNoFall";

    public MLGGame() {
        Bukkit.getServer().getPluginManager().registerEvents(this, PracticePlugin.plugin);
    }

    public static void addPlayer(Player player, ItemStack[] items, double height, int mlgID, int ladderDifficulty, boolean hasFloor) {
        MLGPlaying playing;

        if (playingPlayers.containsKey(player)) {
            semiRemovePlayer(player);
            MLGPlaying oldPlaying = playingPlayers.get(player);
            playing = new MLGPlaying(player, items, oldPlaying.mapType, oldPlaying.mapLevel, (int) height, mlgID, ladderDifficulty, hasFloor);
            playingPlayers.put(player, playing);
            playerFallDistances.put(player, 0.f);

            if ( playing.ladderDifficulty > 0 ) playing.addPillars();
            if ( !playing.hasFloor ) playing.removeFloor(true);
        } else {
            int mapType, mapLevel;
            if (Configs.isRandom) {
                mapType = new Random().nextInt(mapAmount);
                mapLevel = nextFree(mapsInUse[mapType]);
            } else {
                mapType = 0;
                mapLevel = 0;
            }

            mapsInUse[mapType][mapLevel] = true;

            player.setExp(0.0f);
            player.setLevel(0);

            if (playingPlayers.size() == 0)
                taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(PracticePlugin.plugin, () -> {
//                    System.out.println("Going");
                    for ( Player player1 : playingPlayers.keySet() ) {
                        MLGPlaying playing1 = playingPlayers.get(player1);
//                        System.out.println(player1.getLocation().getY() +" "+ (Configs.landingHeight - 1));
                        if ( player1.getLocation().getY() < Configs.landingHeight ) {

                            if ( playing1.playerTaskID > 0 ) {
                                Bukkit.getScheduler().cancelTask(playing1.playerTaskID);
                                playing1.playerTaskID = -1;
                            }
                            if ( playing1.playerTaskID != -2 ) {
                                if ( player1.getHealth() > 1.0 ) {
                                    player1.damage(19.0);
                                }

                                playerFallDistances.remove(player1);
                                playing1.playerTaskID = -2;
                                Bukkit.getScheduler().scheduleSyncDelayedTask(PracticePlugin.plugin, () -> {
                                    resetPlayer(player1, false);
                                    player1.removeScoreboardTag(failTag);
                                    playerFallDistances.put(player1, 0.f);
                                }, Configs.respawnDelay);
                            }
                        }
                    }

                    for (Player player1 : playerFallDistances.keySet()) {
                        float oldD = playerFallDistances.get(player1);
                        float newD = player1.getFallDistance();

//                        System.out.println(player1 + " " + oldD);

                        if (newD < oldD) {
                            MLGPlaying playing1 = playingPlayers.get(player1);
                            if ( player1.getLocation().getY() < Configs.landingHeight + Configs.blockClearHeight ) {
                                if ( player1.getLocation().getY() < Configs.landingHeight + Configs.blockClearHeight ) {

                                }

//                                System.out.println(player1);
                                playerFallDistances.remove(player1);

                                playing1.playerTaskID = Bukkit.getScheduler().scheduleSyncDelayedTask(PracticePlugin.plugin, () -> {
                                    resetPlayer(player1, !player1.getScoreboardTags().contains(failTag));
                                    player1.removeScoreboardTag(failTag);
                                    playerFallDistances.put(player1, 0.f);
                                    playing1.playerTaskID = -1;
                                }, playing1.hasFloor || player1.getScoreboardTags().contains(failTag) ? Configs.respawnDelay : Configs.ladderRespawnDelay);

                            } else if (player1.getLocation().getY() < playing1.height) {
                                resetPlayer(player1, false);
                            }
                        } else {
                            playerFallDistances.put(player1, newD);
                        }
                    }
                }, 1L, 1L);

            playing = new MLGPlaying(player, items, mapType, mapLevel, (int) height, mlgID, ladderDifficulty, hasFloor);
            if (!playing.hasFloor) {
                Location loc = playing.block.getLocation();
                move(loc.getWorld(), true, loc.getBlockX(), Configs.landingHeight - 1, loc.getBlockZ(), loc.getBlockX() - Configs.landingSize.a() - 1, Configs.landingHeight - 2, loc.getBlockZ(), Configs.landingSize.a(), 1, Configs.landingSize.b());
            }
        }

        playingPlayers.put(player, playing);
        playerFallDistances.put(player, 0.f);

        for (int y = Configs.landingHeight; y < 256; y++) {
            Configs.MLGWorld.getBlockAt((int) playing.x, y, (int) playing.z).setType(Material.AIR);
        }

        resetPlayer(player);
    }

    public static void startRace(Player player) {
        if (freezedPlayers.contains(player)) return;

        MLGPlaying playing = playingPlayers.get(player);
        playing.changeHeight(player, 255);

        playing.score = 0;
        player.setExp(0.0f);
        player.setLevel(0);

        freezedPlayers.add(player);

        screenCountDown(5, player);
    }

    public static void leaveRace(Player player) {
        freezedPlayers.remove(player);
        MLGPlaying playing = playingPlayers.get(player);
        if (playing.countDownID != -1) Bukkit.getScheduler().cancelTask(playing.countDownID);
    }

    private static void screenCountDown(int n, Player player) {
//        if ( !playingPlayers.containsKey(player) ) return;
        if (n > 0) {
            playingPlayers.get(player).countDownID = Bukkit.getScheduler().scheduleSyncDelayedTask(PracticePlugin.plugin, () -> screenCountDown(n - 1, player), 20L);
            player.sendTitle(ChatColor.RED + "" + n, "", 0, 20, 0);
        } else {
            freezedPlayers.remove(player);
            player.sendTitle(ChatColor.RED + "" + n, "", 0, 10, 15);
            xpCountDown(Configs.raceTime, player);
        }
    }

    private static void xpCountDown(float seconds, Player player) {
        xpCountDown(seconds, seconds, player);
    }

    private static void xpCountDown(float current, float max, Player player) {
//        if ( !playingPlayers.containsKey(player) || freezedPlayers.contains(player) ) return;

        MLGPlaying playing = playingPlayers.get(player);

        if (current > 0) {
            playing.countDownID = Bukkit.getScheduler().scheduleSyncDelayedTask(PracticePlugin.plugin, () -> xpCountDown(current - 0.1f, max, player), 2L);
            player.setExp((float) (0.99 * current / max));
        } else {
            player.sendMessage(ChatColor.AQUA + "You managed to get " + ChatColor.GOLD + playing.score + ChatColor.AQUA + " points, GJ!");
            resetPlayer(player);

            if (playing.score > playing.raceHighScore) {
                playing.raceHighScore = playing.score;
                player.sendMessage(ChatColor.AQUA + "You got a new Highscore, congrats!");

                playing.scoreboard.setLine(2, ChatColor.BLUE + " " + playing.raceHighScore);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (freezedPlayers.contains(event.getPlayer())) {
            event.getTo().setX(event.getFrom().getX());
            event.getTo().setY(event.getFrom().getY());
            event.getTo().setZ(event.getFrom().getZ());
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (playingPlayers.containsKey(player)) {
                MLGPlaying playing = playingPlayers.get(player);
                if ( player.getLocation().getY() >= playing.block.getLocation().getBlockY() ) {
                    event.setCancelled(true);
                    return;
                }

                Location loc = player.getLocation();

                if ( loc.getY() == Configs.landingHeight || playing.ladderDifficulty > 0 ) {
                    player.addScoreboardTag(failTag);
                    ((Player) event.getEntity()).setHealth(.5f);
                    event.setDamage(0.0);
                    return;
                }

                Block block = blockBelow(loc);
                if ( block == null ) {
                    player.addScoreboardTag(failTag);
                    ((Player) event.getEntity()).setHealth(.5f);
                    event.setDamage(0.0);
                    return;
                }

                event.setCancelled(true);
            }
        }
    }

    public static Block blockBelow(Location loc) {
        Location newLoc = loc.clone();
        newLoc.setY(Math.ceil(loc.getY()) - 1);
        Block center = newLoc.getBlock();
        if (center.getType() != Material.AIR) return center;

        double dx = loc.getX() - loc.getBlockX();
        double dz = loc.getZ() - loc.getBlockZ();

        Block block;
        if (dx > 0.7) {
//            System.out.println("X Positive");
            block = center.getRelative(1, 0, 0);
            if (block.getType() != Material.AIR) return block;

        } else if (dx < 0.3) {
//            System.out.println("X Negative");
            block = center.getRelative(-1, 0, 0);
            if (block.getType() != Material.AIR) return block;
        }

        if (dz > 0.7) {
//            System.out.println("Z Positive");
            block = center.getRelative(0, 0, 1);
            if (block.getType() != Material.AIR) return block;
            if (dx > 0.7) {
                block = center.getRelative(1, 0, 1);
                if (block.getType() != Material.AIR) return block;
            } else if (dx < 0.3) {
                block = center.getRelative(-1, 0, 1);
                if (block.getType() != Material.AIR) return block;
            }
        } else if (dz < 0.3) {
//            System.out.println("Z Negative");
            block = center.getRelative(0, 0, -1);
            if (block.getType() != Material.AIR) return block;
            if (dx > 0.7) {
                block = center.getRelative(1, 0, -1);
                if (block.getType() != Material.AIR) return block;
            } else if (dx < 0.3) {
                block = center.getRelative(-1, 0, -1);
                if (block.getType() != Material.AIR) return block;
            }
        }

        return null;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (playingPlayers.containsKey(event.getPlayer())) {
            if (freezedPlayers.contains(event.getPlayer())) {
                event.setCancelled(true);
                return;
            }

            ItemType type = Items.getTypeNBT(event.getItem());
            Player player = event.getPlayer();

            if ( type != null ) {
                switch (type) {
                    case LEAVE:
                        leaveRace(player);
                        removePlayer(player);
                        event.setCancelled(true);
                        break;
                    case RACE:
                        leaveRace(player);
                        startRace(player);
                        event.setCancelled(true);
                        break;
                    case CHANGEHEIGHT:
                        MLGPlaying playing = playingPlayers.get(player);
                        Inventories.heightInput(player, height -> playing.changeHeight(player, height));
                        break;
                }
                event.setUseItemInHand(Event.Result.DENY);

            } else { // Dont use if clicking on Trapdoor
                Block clickedBlock = event.getClickedBlock();
                if (clickedBlock == null) {
                    event.setUseItemInHand(Event.Result.DENY);
                    return;
                }
                Block startBlock = playingPlayers.get(player).block;

                if (clickedBlock.getLocation().equals(startBlock.getLocation())) {
                    event.setUseItemInHand(Event.Result.DENY);
                    return;
                }
                if (clickedBlock.getX() > startBlock.getX() + Configs.landingSize.a() / 2 || clickedBlock.getX() < startBlock.getX() - Configs.landingSize.a() / 2
                 || clickedBlock.getZ() > startBlock.getZ() + Configs.landingSize.b() / 2 || clickedBlock.getZ() < startBlock.getZ() - Configs.landingSize.b() / 2) {
                    event.setCancelled(true);
                }
            }
        }
    }

    public static void resetPlayer(Player player) {
        resetPlayer(player, true, false);
    }

    public static void resetPlayer(Player player, boolean success) {
        resetPlayer(player, success, true);
    }

    public static void resetPlayer(Player player, boolean success, boolean count) {
        MLGPlaying playing = playingPlayers.get(player);

        player.getInventory().setContents(playing.items);

        playing.block.setType(Material.SPRUCE_TRAPDOOR);

        CraftTrapdoor data = (CraftTrapdoor) playing.block.getBlockData();
        data.setHalf(Bisected.Half.TOP);

        playing.block.setBlockData(data);

        player.setVelocity(new Vector());
        player.teleport(new Location(Configs.MLGWorld, playing.x, playing.height, playing.z, player.getLocation().getYaw(), player.getLocation().getPitch()));
        player.setFallDistance(0.0f);

        player.setHealth(20.0);

        if (count) {
            if (success) {
                playing.score += 1;

                playing.succededMLGS += 1;

                playing.scoreboard.setLine(8, ChatColor.BLUE + " " + playing.succededMLGS);

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                player.setLevel(player.getLevel() + 1);
            } else {
                playing.failedMLGS += 1;

                playing.scoreboard.setLine(5, ChatColor.BLUE + " " + playing.failedMLGS);
            }
        }

        Tuple<Integer, Integer> landingSizeXZ = Configs.landingSize;

        Set<Chunk> chunks = new HashSet<>();
        for (int x = (int) playing.x - landingSizeXZ.a() / 2; x < playing.x + landingSizeXZ.a() / 2; x++) {
            for (int z = (int) playing.z - landingSizeXZ.b() / 2; z < playing.z + landingSizeXZ.b() / 2; z++) {
                for (int y = Configs.landingHeight; y < Configs.landingHeight + Configs.blockClearHeight; y++) {
                    Block block = Configs.MLGWorld.getBlockAt(x, y, z);
                    block.setType(Material.AIR);
                    player.sendBlockChange(block.getLocation(), block.getBlockData());
                }
                Chunk chunk = new Location(Configs.MLGWorld, x, 0, z).getChunk();
                chunks.add(chunk);
            }
        }

        for (Chunk chunk : chunks) {
            for (Entity entity : chunk.getEntities()) {
                if (!(entity instanceof Player)) {
                    entity.remove();
                }
            }
        }

        if ( playing.ladderDifficulty > 0 ) playing.addPillars();
        if ( !playing.hasFloor ) playing.removeFloor(false);
    }

    public static void cut (World world, boolean center, int srcX, int srcY, int srcZ, int sizeX, int sizeY, int sizeZ) {
        int startX = center ? -sizeX / 2 : 0;
        int endX = sizeX + startX;

        int startY = center ? -sizeY / 2 : 0;
        int endY = sizeY + startY;

        int startZ = center ? -sizeZ / 2 : 0;
        int endZ = sizeZ + startZ;

        for (int x = startX; x < endX; x++) {
            for (int z = startZ; z < endZ; z++) {
                for (int y = startY; y < endY; y++) {
                    Block block = world.getBlockAt(srcX + x, srcY + y, srcZ + z);
                    block.setType(Material.AIR);
                }
            }
        }
    }

    public static void move (World world, boolean center, int srcX, int srcY, int srcZ, int destX, int destY, int destZ, int sizeX, int sizeY, int sizeZ) {
        int startX = center ? -sizeX / 2 : 0;
        int endX = sizeX + startX;

        int startY = center ? -sizeY / 2 : 0;
        int endY = sizeY + startY;

        int startZ = center ? -sizeZ / 2 : 0;
        int endZ = sizeZ + startZ;

//        System.out.println(srcX + " " + srcY + " " + srcZ + " " + destX + " " + destY + " " + destZ + " " + sizeX + " " + sizeY + " " + sizeZ);

        for (int x = startX; x < endX; x++) {
            for (int z = startZ; z < endZ; z++) {
                for (int y = startY; y < endY; y++) {
                    Block src = world.getBlockAt(srcX + x, srcY + y, srcZ + z);
                    Block dest = world.getBlockAt(destX + x, destY + y, destZ + z);

                    dest.setBlockData(src.getBlockData());
                    src.setType(Material.AIR);
                }
            }
        }
    }

    public static void copy (World world, boolean center, int srcX, int srcY, int srcZ, int destX, int destY, int destZ, int sizeX, int sizeY, int sizeZ) {
        int startX = center ? -sizeX / 2 : 0;
        int endX = sizeX + startX;

        int startY = center ? -sizeY / 2 : 0;
        int endY = sizeY + startY;

        int startZ = center ? -sizeZ / 2 : 0;
        int endZ = sizeZ + startZ;

//        System.out.println(srcX + " " + srcY + " " + srcZ + " " + destX + " " + destY + " " + destZ + " " + sizeX + " " + sizeY + " " + sizeZ);

        for (int x = startX; x < endX; x++) {
            for (int z = startZ; z < endZ; z++) {
                for (int y = startY; y < endY; y++) {
                    Block src = world.getBlockAt(srcX + x, srcY + y, srcZ + z);
                    Block dest = world.getBlockAt(destX + x, destY + y, destZ + z);

                    dest.setBlockData(src.getBlockData());
                }
            }
        }
    }

    public static void semiRemovePlayer (Player player) {
        player.removeScoreboardTag(failTag);
        player.removeScoreboardTag(noFallTag);

        MLGPlaying playing = playingPlayers.get(player);

        playing.block.setType(Material.AIR);
        player.sendBlockChange(playing.block.getLocation(), playing.block.getBlockData());

        freezedPlayers.remove(player);

        player.setHealth(20.0);
        player.setExp(0.0f);
        player.setLevel(0);

        if ( playing.ladderDifficulty > 0 ) playing.removePillars();
        if ( !playing.hasFloor ) playing.addFloor();

        String uuid = player.getUniqueId().toString();

        PracticePlugin.playerData.set(uuid + "." + playing.mlgID  + ".highscores", playing.raceHighScore);
        PracticePlugin.playerData.set(uuid + "." + playing.mlgID + ".succeded", playing.succededMLGS);
        PracticePlugin.playerData.set(uuid + "." + playing.mlgID + ".failed", playing.failedMLGS);
        PracticePlugin.saveScores();

        playing.scoreboard.delete(player);
    }

    public static void removePlayer (Player player) {

        player.removeScoreboardTag(failTag);
        player.removeScoreboardTag(noFallTag);

        MLGPlaying playing = playingPlayers.get(player);
        mapsInUse[playing.mapType][playing.mapLevel] = false;

        playing.block.setType(Material.AIR);
        player.sendBlockChange(playing.block.getLocation(), playing.block.getBlockData());

        playingPlayers.remove(player);
        playerFallDistances.remove(player);
        freezedPlayers.remove(player);

        player.setHealth(20.0);
        player.setExp(0.0f);
        player.setLevel(0);

        if ( playing.ladderDifficulty > 0 ) playing.removePillars();
        if ( !playing.hasFloor ) playing.addFloor();

        player.teleport(Bukkit.getWorld("world").getSpawnLocation());
        player.getInventory().clear();
        player.getInventory().setItem(4, PracticePlugin.guiItems.get(ItemType.CLOCKITEM));

        if ( playingPlayers.size() == 0 ) {
            Bukkit.getScheduler().cancelTask(taskID);
        }

        String uuid = player.getUniqueId().toString();

        PracticePlugin.playerData.set(uuid + "." + playing.mlgID  + ".highscores", playing.raceHighScore);
        PracticePlugin.playerData.set(uuid + "." + playing.mlgID + ".succeded", playing.succededMLGS);
        PracticePlugin.playerData.set(uuid + "." + playing.mlgID + ".failed", playing.failedMLGS);
        PracticePlugin.saveScores();

        playing.scoreboard.delete(player);
    }

    public static void removePlayerIfNeed (Player player) {
        if ( playerFallDistances.containsKey(player) ) removePlayer(player);
    }

    private static int nextFree (boolean[] maps) {
        for (int i = 0; i < maps.length; i++) {
            if (!maps[i]) {
                return i;
            }
        }
        return -1;
    }

    public enum PlayType {
        Practice,
        Timed,
        Race
    }

    public static class MLGPlaying {
        public ItemStack[] items;
        public int mapType;
        public int mapLevel;
        public double height;
        public double x;
        public double z;
        public int score = 0;
        public Block block;
        public int raceHighScore;
        public int succededMLGS;
        public int failedMLGS;
        public int mlgID;
        public PPPScoreboard scoreboard;
        public int countDownID = -1;
        public int ladderDifficulty;
        public final boolean hasFloor;
        public int playerTaskID = -1;
//        public PlayType playType = PlayType.Practice;
//        public Player raceOpponent;

        public MLGPlaying(Player player, ItemStack[] items, int mapType, int mapLevel, int height, int mlgID, int ladderDifficulty, boolean hasFloor) {
//            System.out.println(ladderDifficulty + " " + hasFloor);

            this.items = items;
            this.mapType = mapType;
            this.mapLevel = mapLevel;

            Location playingLocation = Configs.getMLGCoordinate(mapType, mapLevel, height-1);

            this.x = playingLocation.getX();
            this.z = playingLocation.getZ();
            this.height = height;
            this.block = playingLocation.getBlock();

            String uuid = player.getUniqueId().toString();

            this.mlgID = mlgID;
            ConfigurationSection data = PracticePlugin.playerData.getConfigurationSection(uuid + "." + mlgID);

            this.ladderDifficulty = ladderDifficulty;
            this.hasFloor = hasFloor;

            if ( data == null ) {
                PracticePlugin.playerData.set(uuid + "." + mlgID + ".highscores", 0);
                PracticePlugin.playerData.set(uuid + "." + mlgID + ".succeded", 0);
                PracticePlugin.playerData.set(uuid + "." + mlgID + ".failed", 0);

                this.raceHighScore = 0;
                this.succededMLGS = 0;
                this.failedMLGS = 0;
            } else {
                this.raceHighScore = data.getInt("highscores");
                this.succededMLGS = data.getInt("succeded");
                this.failedMLGS = data.getInt("failed");
            }

            this.scoreboard = new PPPScoreboard(player, 10, ChatColor.AQUA +""+ ChatColor.BOLD + "Your Statistics");
            this.scoreboard.setLine(1, ChatColor.YELLOW + " Your current Highscore:");
            this.scoreboard.setLine(2, ( this.raceHighScore == 0 ? ChatColor.DARK_RED + " You haven't raced yet" : ChatColor.BLUE + " " + this.raceHighScore ));

            this.scoreboard.setLine(4, ChatColor.YELLOW + " Your Fails:");
            this.scoreboard.setLine(5, ChatColor.BLUE + " " + this.failedMLGS);

            this.scoreboard.setLine(7, ChatColor.YELLOW + " Your Successes:");
            this.scoreboard.setLine(8, ChatColor.BLUE + " " + this.succededMLGS);
        }

        public void changeHeight (Player player, double height) {
            this.block.setType(Material.AIR);

            this.height = (int) height;
            Location playingLocation = this.block.getLocation();
            playingLocation.setY(this.height-1);
            this.block = playingLocation.getBlock();

            resetPlayer(player);
        }

        public void removeFloor (boolean move) {
            if ( move ) {
                Location loc = this.block.getLocation();
                move(loc.getWorld(), true, loc.getBlockX(), Configs.landingHeight - 1, loc.getBlockZ(), loc.getBlockX() - Configs.landingSize.a() - 1, Configs.landingHeight - 2, loc.getBlockZ(), Configs.landingSize.a(), 1, Configs.landingSize.b());
            } else {
                Location loc = this.block.getLocation();
                cut(loc.getWorld(), true, loc.getBlockX(), Configs.landingHeight - 1, loc.getBlockZ(), Configs.landingSize.a(), 1, Configs.landingSize.b());
            }

        }

        public void addFloor () {
            Location loc = this.block.getLocation();
            copy(loc.getWorld(), true, loc.getBlockX()-Configs.landingSize.a()-1, Configs.landingHeight-2, loc.getBlockZ(), loc.getBlockX(), Configs.landingHeight-1, loc.getBlockZ(), Configs.landingSize.a(),1, Configs.landingSize.b());
        }

        public void removePillars () {
            Location loc = this.block.getLocation();
            for ( int y = 0; y < ladderDifficulty; y++ ) {
                cut(loc.getWorld(), true, loc.getBlockX(), Configs.landingHeight + y, loc.getBlockZ(), Configs.landingSize.a(),1, Configs.landingSize.b());
            }
        }

        public void addPillars () {
            Location loc = this.block.getLocation();
            for ( int y = 0; y < ladderDifficulty; y++ ) {
                copy(loc.getWorld(), true, loc.getBlockX()+Configs.landingSize.a()+1, Configs.landingHeight-2, loc.getBlockZ(), loc.getBlockX(), Configs.landingHeight + y, loc.getBlockZ(), Configs.landingSize.a(),1, Configs.landingSize.b());
            }
        }
    }
}
