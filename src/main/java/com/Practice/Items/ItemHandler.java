package com.Practice.Items;

import com.Practice.PracticePlugin;
import com.Practice.Useful.Items;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemHandler {

    public static ItemStack read (ConfigurationSection data) {
        ItemType type = ItemType.fromID(data.getInt("type"));
        ItemStack newItem;

        if ( type != null ) {
            switch (type) {
                case CLOCKITEM:
                case LEAVE:
                case RACE:
                case CHANGEHEIGHT:
                    newItem = PracticePlugin.guiItems.get(type);
                    break;
                default:
                    newItem = type.item.read(data);
            }
        } else {
            String itemName = data.getString("name");
            Material material = Material.getMaterial(data.getString("item"));
            int count = data.getInt("count");
            newItem = Items.createNamedItem(material, count, itemName, null);
        }

        return newItem;
    }

    public static void write (ConfigurationSection section, ItemStack item) {
        ItemType type = ItemType.fromID(Items.getIntNBT(item, "PPPType"));

        if ( type != null) {
            section.set("type", type.id);
            DefaultPPPItem ppp = type.item;

            ppp.write(section, item);
        }
    }

    public static void run (ItemType type, Player player, ItemStack clickedItem) {
        type.item.run(player, clickedItem);
    }

    public static void delete (ItemStack clickedItem) {
        ItemType type = ItemType.fromID(Items.getIntNBT(clickedItem, "PPPType"));

        if ( type != null) {
            type.item.delete(clickedItem);
        }
    }

    public boolean add (NewItem newItem, ItemType type) {
        return type.item.add(newItem);
    }

    public static class NewItem {
        public Player player;
        public int newSlot;
        public ItemStack newItem;
        public Inventory addedToInventory;

        public NewItem(Player player, int newSlot, ItemStack newItem, Inventory addedToInventory) {
            this.player = player;
            this.newSlot = newSlot;
            this.newItem = newItem;
            this.addedToInventory = addedToInventory;
        }

    }
}
