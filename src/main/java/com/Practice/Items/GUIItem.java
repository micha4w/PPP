package com.Practice.Items;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GUIItem implements DefaultPPPItem {

    public boolean add(ItemHandler.NewItem newItem) {
        return false;
    }

    public void write(ConfigurationSection section, ItemStack item) {

    }

    public void delete(ItemStack clickedItem) {

    }

    public void run(Player player, ItemStack clickedItem) {

    }

    public ItemStack read(ConfigurationSection data) {
        return null;
    }
}
