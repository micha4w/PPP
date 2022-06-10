package com.Practice.Items;

import com.Practice.PracticePlugin;
import com.Practice.Savior;
import com.Practice.Useful.Items;
import com.Practice.Useful.PPPInventory;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TeleportInvItem implements DefaultPPPItem {

    public boolean add (ItemHandler.NewItem newItem) {
        int[] coords = new int[3];
        coords[0] = newItem.player.getLocation().getBlockX();
        coords[1] = newItem.player.getLocation().getBlockY();
        coords[2] = newItem.player.getLocation().getBlockZ();
        String world = newItem.player.getLocation().getWorld().getName();

        Inventory newInv = Bukkit.createInventory(null, 45);
        newInv.setContents(newItem.player.getInventory().getContents());

        int invID2 = PracticePlugin.inventories.add(new PPPInventory(newInv, null));

        ItemStack addItem = create(newItem.newItem.getType(), newItem.newItem.getItemMeta().getDisplayName(), newItem.newItem.getAmount(), coords, world, invID2);
        newItem.addedToInventory.setItem(newItem.newSlot, addItem);

        newItem.player.sendMessage(ChatColor.LIGHT_PURPLE + "You have added an " + ChatColor.GOLD + "Inventory Teleport" + ChatColor.LIGHT_PURPLE + " item, proud of you.");

        return true;
    }

    public ItemStack read (ConfigurationSection data) {
        String itemName = data.getString("name");
        Material material = Material.getMaterial(data.getString("item"));

        int count = data.getInt("count");
        int invID = data.getInt("inventory");

        int[] coords = data.getIntegerList("coords").stream().mapToInt(i -> i).toArray();
        String world = data.getString("world");

        return create(material, itemName, count, coords, world, invID);
    }

    public ItemStack create (Material item, String itemName, int count, int[] coords, String world, int invID) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInt("PPPType", ItemType.TELEPORTINV.id);
        tag.setIntArray("PPPCoords", coords);
        tag.setString("PPPWorld", world);
        tag.setInt("PPPInvID", invID);

        return Items.createNBTItem(item, count, itemName, null, tag);
    }

    public void write (ConfigurationSection section, ItemStack item) {
        section.set("coords", Items.getIntArrayNBT(item, "PPPCoords"));
        section.set("world", Items.getStringNBT(item, "PPPWorld"));
        section.set("inventory", Items.getIntNBT(item, "PPPInvID"));
    }

    public void run (Player player, ItemStack clickedItem) {
        int invID = Items.getIntNBT(clickedItem, "PPPInvID");
        ItemStack[] items = new ItemStack[41];
        System.arraycopy(PracticePlugin.inventories.get(invID).i.getContents(), 0, items, 0, 41);

        player.getInventory().setContents(items);

        int[] coords = Items.getIntArrayNBT(clickedItem, "PPPCoords");
        String worldStr = Items.getStringNBT(clickedItem, "PPPWorld");
        World world = Bukkit.getWorld(worldStr);
        if (world == null) {
            PracticePlugin.plugin.getLogger().info("(Error) World " + worldStr + " doesn't exist...");
            return;
        }
        if (coords == null) {
            PracticePlugin.plugin.getLogger().info("(Error) Coordinates saved incorrectly...");
            return;
        }
        player.teleport(new Location(world, coords[0] + 0.5, (double) coords[1], coords[2] + 0.5));
    }

    public void delete ( ItemStack clickedItem ) {
        int invID = Items.getIntNBT(clickedItem, "PPPInvID");
        Savior.deleteInventory(invID);
    }
}