package com.Practice.Useful;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CustomInvOwner implements InventoryHolder {

    public static CustomInvOwner Inventory = new CustomInvOwner(Type.Inventory);
    public static CustomInvOwner GUI = new CustomInvOwner(Type.GUI);
    public static CustomInvOwner FinalGUI = new CustomInvOwner(Type.FinalGUI);
    public Type type;

    public CustomInvOwner (Type type) {
        this.type = type;
    }

    @Override
    public Inventory getInventory () {
        return null;
    }

    public enum Type {
        Inventory,
        GUI,
        FinalGUI;
    }

}
