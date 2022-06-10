package com.Practice.Useful;

import com.Practice.Items.ItemType;
import com.Practice.PracticePlugin;
import net.minecraft.server.v1_16_R3.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;

public class Configs {


    public static World MLGWorld;
    public static Location firstMLG;
    public static int MapxDistance;
    public static int MapzDistance;

    public static boolean isRandom;

    public static Tuple<Integer, Integer> landingSize;
    public static Tuple<Integer, Integer> mlgHeightRange;
    public static int landingHeight;
    public static int blockClearHeight;
    public static int raceTime;
    public static int respawnDelay;
    public static int ladderRespawnDelay;


    public static void initConfigs () {
        PracticePlugin.configs = PracticePlugin.plugin.getConfig();
        PracticePlugin.configs.addDefault("MLG_World", "MLG_World");
        PracticePlugin.configs.addDefault("MLG_First_X", 0);
        PracticePlugin.configs.addDefault("MLG_First_Z", 0);
        PracticePlugin.configs.addDefault("MLG_Maps_XDistance", 300);
        PracticePlugin.configs.addDefault("MLG_Maps_ZDistance", 300);
        PracticePlugin.configs.addDefault("MLG_Random_Map", true);
        PracticePlugin.configs.addDefault("MLG_Landing_SizeX", 15);
        PracticePlugin.configs.addDefault("MLG_Landing_SizeZ", 15);
        PracticePlugin.configs.addDefault("MLG_Landing_Height", 2);
        PracticePlugin.configs.addDefault("MLG_Minimum_Height", 12);
        PracticePlugin.configs.addDefault("MLG_Maximum_Height", 255);
        PracticePlugin.configs.addDefault("MLG_Race_Height", 100);
        PracticePlugin.configs.addDefault("MLG_Height_To_Clear_Above_Plattform", 9);
        PracticePlugin.configs.addDefault("MLG_Race_Time_seconds", 60);
        PracticePlugin.configs.addDefault("MLG_Respawn_Delay", 5);
        PracticePlugin.configs.addDefault("MLG_Ladder_Respawn_Delay", 20);


        ItemStack compass = Items.createEnchantPPPItem(Material.CLOCK, 1, "Clock", null, ItemType.CLOCKITEM, "unbreaking", 0);
        ItemStack leavestick = Items.createEnchantPPPItem(Material.STICK, 1, "Return to Lobby", null, ItemType.LEAVE, "unbreaking", 0);
        ItemStack startrace = Items.createEnchantPPPItem(Material.DIAMOND, 1, "(Re)start Race", null, ItemType.RACE, "unbreaking", 0);
        PracticePlugin.configs.addDefault("Item_Clock.type", Material.CLOCK.toString());
        PracticePlugin.configs.addDefault("Item_Clock.name", "Clock");
        PracticePlugin.configs.addDefault("Item_Clock.lore", null);

        PracticePlugin.configs.addDefault("Item_LeaveMLG.type", Material.STICK.toString());
        PracticePlugin.configs.addDefault("Item_LeaveMLG.name", "Return to Lobby");
        PracticePlugin.configs.addDefault("Item_LeaveMLG.lore", null);

        PracticePlugin.configs.addDefault("Item_Start_MLGRace.type", Material.DIAMOND.toString());
        PracticePlugin.configs.addDefault("Item_Start_MLGRace.name", "(Re)start Race");
        PracticePlugin.configs.addDefault("Item_Start_MLGRace.lore", null);

        PracticePlugin.configs.addDefault("Item_Change_MLG_Height.type", Material.OBSERVER.toString());
        PracticePlugin.configs.addDefault("Item_Change_MLG_Height.name", "Change Height");
        PracticePlugin.configs.addDefault("Item_Change_MLG_Height.lore", null);

        PracticePlugin.configs.addDefault("Item_Restart.type", Material.FISHING_ROD.toString());
        PracticePlugin.configs.addDefault("Item_Restart.name", "Restart");
        PracticePlugin.configs.addDefault("Item_Restart.lore", null);

        PracticePlugin.configs.options().copyDefaults(true);
        PracticePlugin.plugin.saveConfig();


        MLGWorld = Bukkit.getWorld(PracticePlugin.configs.getString("MLG_World"));
        firstMLG = new Location(MLGWorld, PracticePlugin.configs.getInt("MLG_First_X") + 0.0, 0.0, PracticePlugin.configs.getInt("MLG_First_Z") + 0.0);
        MapxDistance = PracticePlugin.configs.getInt("MLG_Maps_XDistance");
        MapzDistance = PracticePlugin.configs.getInt("MLG_Maps_ZDistance");
        isRandom = PracticePlugin.configs.getBoolean("MLG_Random_Map");
        landingSize = new Tuple<Integer, Integer>(PracticePlugin.configs.getInt("MLG_Landing_SizeX"), PracticePlugin.configs.getInt("MLG_Landing_SizeZ"));
        mlgHeightRange = new Tuple<Integer, Integer>(PracticePlugin.configs.getInt("MLG_Minimum_Height"), PracticePlugin.configs.getInt("MLG_Maximum_Height"));
        blockClearHeight = PracticePlugin.configs.getInt("MLG_Height_To_Clear_Above_Plattform");
        raceTime = PracticePlugin.configs.getInt("MLG_Race_Time_seconds");
        landingHeight = PracticePlugin.configs.getInt("MLG_Landing_Height");
        respawnDelay = PracticePlugin.configs.getInt("MLG_Respawn_Delay");
        ladderRespawnDelay = PracticePlugin.configs.getInt("MLG_Ladder_Respawn_Delay");
    }

    public static Location getMLGCoordinate (int mapType, int mapLevel, int height) {
        return firstMLG.clone().add(new Vector(MapxDistance * mapLevel+0.5, height, MapzDistance * mapType + 0.5));
    }

    public static void saveItems () {
        readItems.writeToSection("Item_Clock",              ItemType.CLOCKITEM);
        readItems.writeToSection("Item_LeaveMLG",           ItemType.LEAVE);
        readItems.writeToSection("Item_Start_MLGRace",      ItemType.RACE);
        readItems.writeToSection("Item_Change_MLG_Height",  ItemType.CHANGEHEIGHT);
        readItems.writeToSection("Item_Restart",            ItemType.RESTART);
        PracticePlugin.plugin.saveConfig();
    }

    public static class readItems {

        public ItemStack compass;
        public ItemStack leaveStick;
        public ItemStack raceStarter;
        public ItemStack changeHeight;
        public ItemStack restart;

        public readItems () {
            compass = readFromSection       ("Item_Clock",              ItemType.CLOCKITEM);
            leaveStick = readFromSection    ("Item_LeaveMLG",           ItemType.LEAVE);
            raceStarter = readFromSection   ("Item_Start_MLGRace",      ItemType.RACE);
            changeHeight = readFromSection  ("Item_Change_MLG_Height",  ItemType.CHANGEHEIGHT);
            restart = readFromSection       ("Item_Restart",            ItemType.RESTART);
        }

        private static ItemStack readFromSection (String section, ItemType itemType) {
            ConfigurationSection sec = PracticePlugin.configs.getConfigurationSection(section);
            String type = sec.getString("type");
            String name = sec.getString("name");
            List<String> lore = sec.getStringList("lore");
            return Items.createEnchantPPPItem(Material.getMaterial(type), 1, name, lore, itemType, "unbreaking", 0);
        }

        public static void writeToSection (String section, ItemType itemType) {
            ConfigurationSection sec = PracticePlugin.configs.getConfigurationSection(section);
            ItemStack item = PracticePlugin.guiItems.get(itemType);

            sec.set("type", item.getType().toString());
            sec.set("name", item.getItemMeta().getDisplayName());
            sec.set("lore", item.getItemMeta().getLore());
        }
    }
}
