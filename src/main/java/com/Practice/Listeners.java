package com.Practice;

import com.Practice.Items.ItemHandler;
import com.Practice.Items.ItemType;
import com.Practice.MiniGames.MLGGame;
import com.Practice.Useful.CustomInvOwner;
import com.Practice.Useful.Inventories;
import com.Practice.Useful.Items;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Collections;

public class Listeners implements Listener {

//    @EventHandler
//    public void onPlaceBloc (BlockPlaceEvent event) {
//        event.setCancelled(true);
//        Bukkit.getScheduler().scheduleSyncDelayedTask(PracticePlugin.plugin, () -> {
//            event.getPlayer().sendBlockChange(event.getBlock().getLocation(), event.getBlock().getBlockData());
//        }, 1L);
//    }

    @EventHandler
    public void onExhaust (FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if ( player.getOpenInventory().countSlots() != 46 ) {
            return;
        }

        int typeID = Items.getIntNBT(event.getItem(), "PPPType");

        if ( typeID == ItemType.CLOCKITEM.id ) {
            player.openInventory(PracticePlugin.inventories.get(0).i);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDragItems (InventoryDragEvent event) {
        if ( Collections.min(event.getRawSlots()) < event.getView().getTopInventory().getSize() ) {
            if ( event.getView().getTopInventory().getHolder() instanceof CustomInvOwner ) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    @EventHandler
    public void onClickItem(InventoryClickEvent event)  {
        if ( event.getClickedInventory() == null || event.getAction() == InventoryAction.UNKNOWN )
            return;

        ItemStack clickedItem = event.getCurrentItem();
        ItemStack heldItem = event.getCursor();

        Player player = (Player) event.getWhoClicked();

        InventoryHolder owner = event.getClickedInventory().getHolder();
        CustomInvOwner.Type invType = null;
        if ( owner instanceof CustomInvOwner ) {
            invType = ((CustomInvOwner) owner).type;
            event.setResult(Event.Result.DENY);
        } else if ( event.getView().getTopInventory().getHolder() instanceof CustomInvOwner ) {
            if ( affectedInventory(event) == 2 ) {
                event.setResult(Event.Result.DENY);
            }
        }

        if ( invType == CustomInvOwner.Type.GUI || invType == CustomInvOwner.Type.FinalGUI && clickedItem != null ) {
            Inventories.called(player, event.getSlot());
            return;
        }

        ItemType type = ItemType.fromID(Items.getIntNBT(clickedItem, "PPPType"));
        int invID = Items.getIntNBT(clickedItem, "PPPInvID");

        if ( invType == CustomInvOwner.Type.Inventory ) {
            if (PracticePlugin.editingPlayers.contains(player)) {

                switch (event.getClick()) {
                    case RIGHT:
                        // Remove Item
                        if (clickedItem != null) {
                            event.setCursor(null);
                            Inventories.add(player, Inventories.Type.Confirm, slot -> {
                                if (slot == 12) {
                                    ItemHandler.delete(clickedItem);

                                    event.getInventory().setItem(event.getSlot(), null);
                                    player.closeInventory();
                                    player.sendMessage(ChatColor.LIGHT_PURPLE + "You have deleted an Item...");
                                    return true;
                                } else if (slot == 14) {
                                    player.closeInventory();
                                    return true;
                                }
                                return false;
                            });
                        }
                        return;
                    case LEFT:
                        event.setCursor(null);
                        if (clickedItem == null && heldItem.getType() != Material.AIR) { // Create Item
                            Inventories.addChooseItem(player, event.getSlot(), heldItem, event.getClickedInventory());
                            return;
                        }
                        break;
                }
            }

            if (type == null) {
                return;
            }

            if ( event.getCursor() != null ) {
                event.getWhoClicked().getInventory().addItem(event.getCursor());
                event.setCursor(null);
            }

            ItemHandler.run(type, player, clickedItem);
        }
    }

    private int affectedInventory (InventoryClickEvent event) {
        InventoryAction action = event.getAction();
        if ( action == InventoryAction.DROP_ALL_SLOT
          || action == InventoryAction.DROP_ONE_CURSOR
          || action == InventoryAction.PLACE_ALL
          || action == InventoryAction.PLACE_ONE
          || action == InventoryAction.PICKUP_ALL
          || action == InventoryAction.PICKUP_HALF
          || action == InventoryAction.PICKUP_SOME
          || action == InventoryAction.PICKUP_ONE
          || action == InventoryAction.SWAP_WITH_CURSOR
        ) { // Affects the current Inventory
            return 1;
        } else if ( action == InventoryAction.HOTBAR_SWAP ) { // Depends on which inventory got clicked
            if ( event.getClickedInventory() == event.getView().getBottomInventory() ) {
                return 1;
            } else {
                return 2;
            }
        } else if ( action == InventoryAction.COLLECT_TO_CURSOR ) {
            if ( event.getClickedInventory() == event.getView().getBottomInventory() ) {
                if ( event.getView().getTopInventory().containsAtLeast(event.getCursor(), 1) )
                    return 2;
            } else {
                if ( event.getView().getBottomInventory().containsAtLeast(event.getCursor(), 1) )
                    return 2;
            }
            return 1;
        } else if ( action == InventoryAction.MOVE_TO_OTHER_INVENTORY
                 || action == InventoryAction.HOTBAR_MOVE_AND_READD
        ) { // Affects Both inventories
            return 2;
        } else { // only affects hand
            return 0;
        }
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent event) {
        if ( event.getInventory().getHolder() instanceof CustomInvOwner && ((CustomInvOwner) event.getInventory().getHolder()).type == CustomInvOwner.Type.FinalGUI) {
            Inventories.playerList.remove(event.getPlayer());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().getInventory().setItem(4, PracticePlugin.guiItems.get(ItemType.CLOCKITEM));
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        MLGGame.removePlayerIfNeed(event.getPlayer());

        if ( !event.getPlayer().isOp() ) {
            final String uuid = event.getPlayer().getUniqueId().toString();
            Bukkit.getScheduler().scheduleSyncDelayedTask(PracticePlugin.plugin, () -> {
                File playerFile = new File("world/playerdata/" + uuid + ".dat");
                boolean b2 = playerFile.delete();

                playerFile = new File("world/playerdata/" + uuid + ".dat_old");
                boolean b1 = playerFile.delete();
            }, 1L);
        }
    }

}
