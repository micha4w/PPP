package com.Practice.Items;

import com.Practice.PracticePlugin;
import com.Practice.Savior;
import com.Practice.Useful.Inventories;
import com.Practice.Useful.Items;
import com.Practice.Useful.PPPInventory;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryItem implements DefaultPPPItem {

    public boolean add (ItemHandler.NewItem newItem) {
        Inventories.add(newItem.player, Inventories.Type.ChooseInvSize, slot2 -> {
            if (slot2 < 11 || slot2 > 16) return false;

            int size = (slot2 - 10) * 9;
            Inventories.textInput("Choose a Title.", newItem.player, text-> {
                int invID2 = PracticePlugin.inventories.add(new PPPInventory(size, text));
                ItemStack addItem2 = create(newItem.newItem.getType(), newItem.newItem.getItemMeta().getDisplayName(), newItem.newItem.getAmount(), invID2);
                newItem.addedToInventory.setItem(newItem.newSlot, addItem2);
                newItem.player.closeInventory();
                newItem.player.sendMessage(ChatColor.LIGHT_PURPLE + "You have added an " + ChatColor.GOLD + "Inventory" + ChatColor.LIGHT_PURPLE + " item, GJ.");

                return true;
            });

            return true;
        });
        return false;
    }

    public ItemStack read (ConfigurationSection data) {
        String itemName = data.getString("name");
        Material material = Material.getMaterial(data.getString("item"));
        int count = data.getInt("count");

        int invID = data.getInt("inventory");

        return create(material, itemName, count, invID);
    }

    public ItemStack create (Material item, String itemName, int count, int inventoryID) {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setInt("PPPType", ItemType.INVENTORY.id);
        tag.setInt("PPPInvID", inventoryID);
        return Items.createNBTItem(item, count, itemName, null, tag);
    }


    public void write(ConfigurationSection section, ItemStack item) {
        section.set("inventory", Items.getIntNBT(item, "PPPInvID"));
    }

    public void run (Player player, ItemStack clickedItem) {
        int invID = Items.getIntNBT(clickedItem, "PPPInvID");

        player.openInventory(PracticePlugin.inventories.get(invID).i);
    }

    public void delete ( ItemStack clickedItem ) {
        int invID = Items.getIntNBT(clickedItem, "PPPInvID");
        Savior.deleteInventory(invID);
    }
}
