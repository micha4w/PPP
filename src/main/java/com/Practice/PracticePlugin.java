package com.Practice;

import com.Practice.Items.ItemType;
import com.Practice.MiniGames.MLGGame;
import com.Practice.MiniGames.ParkourGame;
import com.Practice.Useful.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class PracticePlugin extends JavaPlugin {

    public static final String NMS_VERSION = Bukkit.getServer().getClass().getPackage().getName().substring(23);
    public static PracticePlugin plugin;


    public static Map<ItemType, ItemStack> guiItems = new HashMap<>();

    public static UUIDMap<PPPInventory> inventories = new UUIDMap<>();
    public static UUIDList mlgID = new UUIDList();

    public static List<Player> editingPlayers = new ArrayList<>();

    public static FileConfiguration playerData;
    public static FileConfiguration configs;


    @Override
    public void onEnable() {
        plugin = this;

        Configs.initConfigs();

        File file = new File(getDataFolder()+"/players.yml");

        if ( !file.exists() ) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        playerData = YamlConfiguration.loadConfiguration(file);
        saveScores();

        registerItems();

        Savior.read();
        Inventories.init();

        CommandListener com = new CommandListener();
        this.getCommand("editinv").setExecutor(com);
        this.getCommand("savegui").setExecutor(com);
        this.getCommand("loadgui").setExecutor(com);
        this.getCommand("getitem").setExecutor(com);
        this.getCommand("setitem").setExecutor(com);
        this.getCommand("undocheckpoint").setExecutor(com);
        this.getCommand("updateppp").setExecutor(com);
        this.getCommand("test").setExecutor(com);
        this.getCommand("ppp").setExecutor(com);

        this.getCommand("getitem").setTabCompleter(com);
        this.getCommand("setitem").setTabCompleter(com);
        this.getCommand("ppp").setTabCompleter(com);

        Bukkit.getServer().getPluginManager().registerEvents(new MLGGame(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new ParkourGame(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new Listeners(), this);


//        Handler handler = new Handler() {
//            @Override
//            public void publish(LogRecord record) {
//                Player micha = Bukkit.getPlayer("micha4w");
//                if ( micha != null /*&& micha.getScoreboardTags().contains("PPPconsole")*/ ) {
//                    micha.sendMessage(record.getMessage());
//                }
//            }
//
//            @Override
//            public void flush() {
//
//            }
//
//            @Override
//            public void close() throws SecurityException {
//
//            }
//        };
//
//        getLogger().addHandler(handler);
        getLogger().info("Succefuly enabled PPP!");
    }

    private void registerItems () {
        Configs.readItems items = new Configs.readItems();
        guiItems.put(ItemType.CLOCKITEM, items.compass);
        guiItems.put(ItemType.LEAVE, items.leaveStick);
        guiItems.put(ItemType.RACE, items.raceStarter);
        guiItems.put(ItemType.CHANGEHEIGHT, items.changeHeight);
        guiItems.put(ItemType.RESTART, items.restart);
    }

    public static void changeItem (ItemType type, ItemStack newItem) {
        ItemStack copy = Items.createGUIItem(newItem, type);
        guiItems.put(type, copy);
    }

    @Override
    public void onDisable() {
        Savior.write();

        for ( Player player : MLGGame.playingPlayers.keySet() ) {
            MLGGame.removePlayer(player);
        }

        saveScores();

        File file = new File("world/playerdata/");
        try {
            FileUtils.cleanDirectory(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        getLogger().info("Disabled PPP! D:");
    }

    public static void saveScores () {
        try {
            playerData.save(PracticePlugin.plugin.getDataFolder()+"/players.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
