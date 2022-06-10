package com.Practice;

import com.Practice.Items.ItemHandler;
import com.Practice.Items.ItemType;
import com.Practice.Useful.Items;
import com.Practice.Useful.PPPInventory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;

public class Savior {

    public static void write () {
//        File customYml = new File(PracticePlugin.plugin.getDataFolder()+"/items.yml");
//        FileConfiguration config = YamlConfiguration.loadConfiguration(customYml);
//
//        for(String key : config.getKeys(false)) config.set(key, null);
        FileConfiguration config  = new YamlConfiguration();

        for ( Integer key : PracticePlugin.inventories.keySet() ) {
            PPPInventory inv = PracticePlugin.inventories.get(key);
            String invID = "" + key + ".";

            if ( inv.name != null ) {
                config.set(invID + "name", inv.name);
                config.set(invID + "size", inv.i.getSize());
            }

            for ( int slot = 0; slot < inv.i.getSize(); slot++) {
                ItemStack item = inv.i.getItem(slot);
                if ( item == null ) continue;

                config.createSection(invID + "." + slot);
                ConfigurationSection section = config.getConfigurationSection(invID + "." + slot);

                section.set("name", item.getItemMeta().getDisplayName());
                section.set("item", item.getType().toString());
                section.set("count", item.getAmount());

                ItemHandler.write(section, item);
            }
        }

        try {
            config.save(PracticePlugin.plugin.getDataFolder()+"/items.yml");
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void read () {
        File customYml = new File(PracticePlugin.plugin.getDataFolder()+"/items.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(customYml);

        if ( config.get("0") == null ) {
            PracticePlugin.plugin.getLogger().info("Created new Config File!");
            config.set("0.name", "WELCOME");
            config.set("0.size", 27);
            try {
                config.save(PracticePlugin.plugin.getDataFolder()+"/items.yml");
            } catch (IOException e) { e.printStackTrace(); }
        }

        PracticePlugin.inventories.clear();
        PracticePlugin.mlgID.clear();

        for ( String key : config.getKeys(false) ) {
            ConfigurationSection section = config.getConfigurationSection(key);
            String invName = section.getString("name");

            PPPInventory newInv;
            if ( invName != null ) {
                newInv = new PPPInventory(section.getInt("size"), invName);
            } else {
                newInv = new PPPInventory(45, null);
            }

            for ( String slot : section.getKeys(false) ) {
                if ( slot.equals("size") || slot.equals("name") ) continue;

                ConfigurationSection data = section.getConfigurationSection(slot);

                ItemStack newItem = ItemHandler.read(data);
                newInv.i.setItem(Integer.parseInt(slot), newItem);
            }
            PracticePlugin.inventories.put(Integer.parseInt(key), newInv);
        }
    }


    public static void deleteInventory(int invID) {
        Inventory inv = PracticePlugin.inventories.get(invID).i;

        for ( ItemStack item : inv.getContents() ) {
            if ( item == null ) continue;

            ItemType type = ItemType.fromID(Items.getIntNBT(item, "PPPType"));
            if ( type == ItemType.INVENTORY || type == ItemType.TELEPORTMLG || type == ItemType.TELEPORTINV ) {
                Savior.deleteInventory(Items.getIntNBT(item, "PPPInvID"));
            }
        }

        PracticePlugin.inventories.remove(invID);
    }
}
