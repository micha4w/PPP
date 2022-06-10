package com.Practice.Items;

import com.Practice.PracticePlugin;
import com.Practice.Useful.Items;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TeleportItem implements DefaultPPPItem {

    public boolean add(ItemHandler.NewItem newItem) {
        int[] coords = new int[3];
        coords[0] = newItem.player.getLocation().getBlockX();
        coords[1] = newItem.player.getLocation().getBlockY();
        coords[2] = newItem.player.getLocation().getBlockZ();
        String world = newItem.player.getLocation().getWorld().getName();

        ItemStack addItem = create(newItem.newItem.getType(), newItem.newItem.getItemMeta().getDisplayName(), newItem.newItem.getAmount(), coords, world);
        newItem.addedToInventory.setItem(newItem.newSlot, addItem);

        newItem.player.sendMessage(ChatColor.LIGHT_PURPLE + "You have added a " + ChatColor.GOLD + "Simple teleport" + ChatColor.LIGHT_PURPLE + " item, YAY.");
        return true;
    }

    public ItemStack read(ConfigurationSection data) {
        String itemName = data.getString("name");
        Material material = Material.getMaterial(data.getString("item"));
        int count = data.getInt("count");

        int[] coords = data.getIntegerList("coords").stream().mapToInt(i -> i).toArray();
        String world = data.getString("world");

        return create(material, itemName, count, coords, world);
    }

    public ItemStack create (Material item, String itemName, int count, int[] coords, String world) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInt("PPPType", ItemType.TELEPORT.id);
        tag.setIntArray("PPPCoords", coords);
        tag.setString("PPPWorld", world);

        return Items.createNBTItem(item, count, itemName, null, tag);
    }

    public void write (ConfigurationSection section, ItemStack item) {
        section.set("coords", Items.getIntArrayNBT(item, "PPPCoords"));
        section.set("world", Items.getStringNBT(item, "PPPWorld"));
    }

    public void run (Player player, ItemStack clickedItem) {
        int[] c = Items.getIntArrayNBT(clickedItem, "PPPCoords");
        String worldStr = Items.getStringNBT(clickedItem, "PPPWorld");
        World world = Bukkit.getWorld(worldStr);
        if (world == null) {
            PracticePlugin.plugin.getLogger().info("(Error) World " + worldStr + " doesn't exist...");
            return;
        }
        if (c == null) {
            PracticePlugin.plugin.getLogger().info("(Error) Coordinates saved incorrectly...");
            return;
        }
        player.teleport(new Location(world, c[0] + 0.5, (double) c[1], c[2] + 0.5));
    }

    public void delete ( ItemStack clickedItem ) {

    }
}
