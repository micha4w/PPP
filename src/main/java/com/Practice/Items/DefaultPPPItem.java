package com.Practice.Items;


import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface DefaultPPPItem {

    boolean add (ItemHandler.NewItem newItem);
    void write (ConfigurationSection section, ItemStack item);
    void delete (ItemStack clickedItem);

    void run (Player player, ItemStack clickedItem);

    ItemStack read (ConfigurationSection data);
}
