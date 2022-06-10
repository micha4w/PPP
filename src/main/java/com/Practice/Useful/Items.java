package com.Practice.Useful;

import com.Practice.Items.ItemType;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagList;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class Items {

    public static ItemStack createGUIItem (ItemStack copy, ItemType type) {
        return Items.createEnchantPPPItem(copy.getType(), 1, copy.getItemMeta().getDisplayName(), copy.getItemMeta().getLore(), type, "unbreaking", 0);
    }

    public static ItemStack createEnchantPPPItem (Material type, int amount, String name, List<String> lore, ItemType pppType, String enchantment, int lvl) {
        if ( !enchantment.startsWith("minecraft:") ) enchantment = "minecraft:" + enchantment;

        net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(new ItemStack(type, amount));

        NBTTagCompound tag = new NBTTagCompound();
        tag.setInt("PPPType", pppType.id);
        NBTTagList list = new NBTTagList();
        NBTTagCompound ench = new NBTTagCompound();
        ench.setString("id", enchantment);
        ench.setInt("lvl", lvl);
        list.add(ench);
        tag.set("Enchantments", list);

        nmsItem.setTag(tag);
        ItemStack item = CraftItemStack.asBukkitCopy(nmsItem);
        renameItem(item, name, lore);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack createNBTItem(Material type, int amount, String name, List<String> lore, NBTTagCompound tag) {
        net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(new ItemStack(type, amount));
        nmsItem.setTag(tag);
        ItemStack item = CraftItemStack.asBukkitCopy(nmsItem);
        renameItem(item, name, lore);
        return item;
    }

    public static String getStringNBT(ItemStack item, String path) {
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getTag();
        if ( tag == null ) return null;
        return tag.getString(path);
    }

    public static int getIntNBT(ItemStack item, String path) {
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getTag();
        if ( tag == null ) return 0;
        return tag.getInt(path);
    }

    public static boolean getBooleanNBT(ItemStack item, String path) {
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getTag();
        if ( tag == null ) return false;
        return tag.getBoolean(path);
    }

    public static int[] getIntArrayNBT(ItemStack item, String path) {
        NBTTagCompound tag = CraftItemStack.asNMSCopy(item).getTag();
        if ( tag == null ) return null;
        return tag.getIntArray(path);
    }

    public static ItemType getTypeNBT (ItemStack item) {
        try {
            return ItemType.fromID(getIntNBT(item, "PPPType"));
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static ItemStack createNamedItem (Material type, int amount, String name, List<String> lore) {
        ItemStack item = new ItemStack(type, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static void renameItem (ItemStack item, String name, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public static ItemStack attachNBTToItem(ItemStack item, NBTTagCompound tag) {
        net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        return createNBTItem(item.getType(), item.getAmount(), item.getItemMeta().getDisplayName(), item.getItemMeta().getLore(), nmsItem.getTag());
    }
}
