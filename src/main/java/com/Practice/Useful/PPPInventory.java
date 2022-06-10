package com.Practice.Useful;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;


public class PPPInventory {

    public Inventory i;
    public String name;

    public PPPInventory (int size, String name) {
        if ( name != null ) {
            this.i = Bukkit.createInventory(CustomInvOwner.Inventory, size, name);
        } else {
            this.i = Bukkit.createInventory(CustomInvOwner.Inventory, size);
        }
        this.name = name;
    }

    public PPPInventory (Inventory inventory, String name) {
        this.i = inventory;
        this.name = name;
    }

}
