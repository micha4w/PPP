package com.Practice.Items;

import com.Practice.MiniGames.MLGGame;
import com.Practice.PracticePlugin;
import com.Practice.Savior;
import com.Practice.Useful.Inventories;
import com.Practice.Useful.Items;
import com.Practice.Useful.PPPInventory;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MLGItem implements DefaultPPPItem {

    public boolean add(ItemHandler.NewItem newItem) {
        Inventory newInv = Bukkit.createInventory(null, 45);
        newInv.setContents(newItem.player.getInventory().getContents());

        int invID = PracticePlugin.inventories.add(new PPPInventory(newInv, null));
        int mlgID = PracticePlugin.mlgID.get();

        ItemStack addItem = create(newItem.newItem.getType(), newItem.newItem.getItemMeta().getDisplayName(), newItem.newItem.getAmount(), invID, mlgID, false);
        newItem.addedToInventory.setItem(newItem.newSlot, addItem);

        newItem.player.sendMessage(ChatColor.LIGHT_PURPLE + "You have added a " + ChatColor.GOLD + "MLG Teleport" + ChatColor.LIGHT_PURPLE + " item, WOW.");
        return true;
    }

    public ItemStack read (ConfigurationSection  data) {
        String itemName = data.getString("name");
        Material material = Material.getMaterial(data.getString("item"));

        int count = data.getInt("count");
        int invID = data.getInt("inventory");

        boolean isLadder = data.getBoolean("ladder");
        int mlgID = data.getInt("mlg");
        PracticePlugin.mlgID.add(mlgID);

        return create(material, itemName, count, invID, mlgID, isLadder);
    }

    public ItemStack create (Material material, String itemName, int count, int invID, int mlgID, boolean isLadder) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInt("PPPType", ItemType.TELEPORTMLG.id);
        tag.setInt("PPPInvID", invID);
        tag.setInt("PPPMLGID", mlgID);
        tag.setBoolean("PPPMLGLadder", isLadder);

        return Items.createNBTItem(material, count, itemName, null, tag);
    }

    public void write (ConfigurationSection section, ItemStack item) {
        section.set("inventory", Items.getIntNBT(item, "PPPInvID"));
        section.set("mlg", Items.getIntNBT(item, "PPPMLGID"));
        section.set("ladder", Items.getBooleanNBT(item, "PPPMLGLadder"));
    }

    public void run (Player player, ItemStack clickedItem) {
        int invID = Items.getIntNBT(clickedItem, "PPPInvID");

        ItemStack[] items = new ItemStack[41];
        System.arraycopy(PracticePlugin.inventories.get(invID).i.getContents(), 0, items, 0, 41);

        int mlgID = Items.getIntNBT(clickedItem, "PPPMLGID");
        boolean isLadder = Items.getBooleanNBT(clickedItem, "PPPMLGLadder");
        if ( isLadder ) {
            Inventories.add(player, Inventories.Type.LadderDifficulty, (difficultyMin1) -> {
                Inventories.add(player, Inventories.Type.FloorChose, (withFloor) -> {
                    Inventories.heightInput(player, height -> MLGGame.addPlayer(player, items, height, mlgID, difficultyMin1+1, withFloor!=3));
                    return true;
                });
                return false;
            });
        } else {
            Inventories.heightInput(player, height -> MLGGame.addPlayer(player, items, height, mlgID, 0, true));
        }
    }

    public void delete ( ItemStack clickedItem ) {
        int invID = Items.getIntNBT(clickedItem, "PPPInvID");
        Savior.deleteInventory(invID);
        PracticePlugin.mlgID.remove(Items.getIntNBT(clickedItem, "PPPMLGID"));
    }
}
