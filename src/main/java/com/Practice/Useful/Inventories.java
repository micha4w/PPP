package com.Practice.Useful;

import com.Practice.Items.ItemHandler;
import com.Practice.Items.ItemType;
import com.Practice.Items.MLGItem;
import com.Practice.PracticePlugin;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.Tuple;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class Inventories {


    public static Inventory confirmInv = Bukkit.createInventory(CustomInvOwner.FinalGUI, 27, "Are you sure?");
    public static Inventory inventorySizeInv = Bukkit.createInventory(CustomInvOwner.GUI, 27, "How big shall inv be?");
    public static Inventory ladderDifficultyInv = Bukkit.createInventory(CustomInvOwner.GUI, 9, "Choose a Pillar Height");
    public static Inventory floorChooseInv = Bukkit.createInventory(CustomInvOwner.FinalGUI, 9, "Choose whether you want a Floor");

    public static Inventory itemTypeInv = Bukkit.createInventory(CustomInvOwner.FinalGUI, 27, "Chose what ItemType to add");
    public static Map<Integer, Function<ItemHandler.NewItem, Boolean>> itemTypeFuncs = new HashMap<>();

    public static HashMap<Player, Function<Integer, Boolean>> playerList = new HashMap<>();

    public static void init () {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInt("PPPType", ItemType.GUIITEM.id);

//        Bukkit.getPlayer("micha4w").getInventory().addItem(Items.createNBTItem(Material.CHEST_MINECART, 1, "Open up an Inventory", null, tag));

//        List<String> lore = new ArrayList<>();
//        lore.add("Test1");
//        lore.add("Test2");
//        lore.add("Test3");
//        Bukkit.getPlayer("micha4w").getInventory().addItem(Items.createNamedItem(Material.BARRIER, 2, "Test", lore));

        confirmInv.setItem(12, Items.createNBTItem(Material.GREEN_CONCRETE, 1, "Yes, confirm", null, tag));
        confirmInv.setItem(14, Items.createNBTItem(Material.RED_CONCRETE, 1, "NOO, GO BACK", null, tag));

        inventorySizeInv.setItem(11, Items.createNBTItem(Material.DEAD_BUSH, 1, "9", null, tag));
        inventorySizeInv.setItem(12, Items.createNBTItem(Material.OAK_SAPLING, 1, "18", null, tag));
        inventorySizeInv.setItem(13, Items.createNBTItem(Material.OAK_LEAVES, 1, "27", null, tag));
        inventorySizeInv.setItem(14, Items.createNBTItem(Material.OAK_PLANKS, 1, "36", null, tag));
        inventorySizeInv.setItem(15, Items.createNBTItem(Material.BARREL, 1, "45", null, tag));
        inventorySizeInv.setItem(16, Items.createNBTItem(Material.CHEST, 1, "54", null, tag));

        ladderDifficultyInv.addItem(Items.createNBTItem(Material.BEDROCK, 1, "1", null, tag));
        ladderDifficultyInv.addItem(Items.createNBTItem(Material.NETHERITE_INGOT, 1, "2", null, tag));
        ladderDifficultyInv.addItem(Items.createNBTItem(Material.DIAMOND, 1, "3", null, tag));
        ladderDifficultyInv.addItem(Items.createNBTItem(Material.EMERALD, 1, "4", null, tag));
        ladderDifficultyInv.addItem(Items.createNBTItem(Material.GOLD_INGOT, 1, "5", null, tag));
        ladderDifficultyInv.addItem(Items.createNBTItem(Material.IRON_INGOT, 1, "6", null, tag));
        ladderDifficultyInv.addItem(Items.createNBTItem(Material.COAL, 1, "7", null, tag));
        ladderDifficultyInv.addItem(Items.createNBTItem(Material.COBBLESTONE, 1, "8", null, tag));
        ladderDifficultyInv.addItem(Items.createNBTItem(Material.OAK_WOOD, 1, "9", null, tag));

        floorChooseInv.setItem(3, Items.createNBTItem(Material.RED_CONCRETE, 1, "I'll manage", null, tag));
        floorChooseInv.setItem(5, Items.createNBTItem(Material.GREEN_CONCRETE, 1, "Yes, please!", null, tag));

        addItemType(11, Items.createNamedItem(Material.CHEST_MINECART, 1, "Open up an Inventory", Arrays.asList("Creates a new inventory")), ItemType.INVENTORY.item::add);
        addItemType(12, Items.createNamedItem(Material.ENDER_PEARL, 1, "Teleport Player", Arrays.asList("To your current location")), ItemType.TELEPORT.item::add);
        addItemType(14, Items.createNamedItem(Material.ENDER_EYE, 1, "Teleport Player, but with Inventory", Arrays.asList("To your current location", "with your current inv")), ItemType.TELEPORTINV.item::add);
        addItemType(15, Items.createNamedItem(Material.BLAZE_POWDER, 1, "MLG Item, Teleports to Arena", Arrays.asList("Very complicated")), ItemType.TELEPORTMLG.item::add);
        addItemType(13, Items.createNamedItem(Material.LADDER, 1, "MLG Item, Teleports to Arena, but with Pillar evtl. Floor", Arrays.asList("Even more complicated")), (newItem) -> {
            Inventory newInv = Bukkit.createInventory(null, 45);
            newInv.setContents(newItem.player.getInventory().getContents());

            int invID2 = PracticePlugin.inventories.add(new PPPInventory(newInv, null));
            int mlgID = PracticePlugin.mlgID.get();

            ItemStack addItem = ( (MLGItem) ItemType.TELEPORTMLG.item ).create(newItem.newItem.getType(), newItem.newItem.getItemMeta().getDisplayName(), newItem.newItem.getAmount(), invID2, mlgID, true);
            newItem.addedToInventory.setItem(newItem.newSlot, addItem);

            newItem.player.sendMessage(ChatColor.LIGHT_PURPLE + "You have added a " + ChatColor.GOLD + "Ladder MLG Teleport" + ChatColor.LIGHT_PURPLE + " item, WOW.");
            return true;
        });
        addItemType(22, Items.createNamedItem(Material.LEATHER_BOOTS, 1, "Parkour Item", Arrays.asList("Gets where you are right now", "Parkour ends when stood on Diamond Block")), ItemType.PARKOUR.item::add);
    }

    private static void addItemType (int slot, ItemStack item, Function<ItemHandler.NewItem, Boolean> func) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInt("PPPType", ItemType.GUIITEM.id);
        itemTypeInv.setItem(slot, Items.attachNBTToItem(item, tag));
        itemTypeFuncs.put(slot, func);
    }

    public static void addChooseItem (Player player, int slot, ItemStack heldItem, Inventory inventory) {
        player.openInventory(itemTypeInv);
        playerList.put(player, chosenSlot -> evaluateChooseItem(chosenSlot, slot, player, heldItem, inventory));
    }

    private static boolean evaluateChooseItem (int chosenSlot, int slot, Player player, ItemStack heldItem, Inventory inventory) {
        Function<ItemHandler.NewItem, Boolean> func = itemTypeFuncs.get(chosenSlot);

        if ( func == null ) return false;

        boolean success = func.apply(new ItemHandler.NewItem(player, slot, heldItem, inventory));
        if ( success ) player.closeInventory();
        return success;
    }

    public static void add (Player player, Type type, Function<Integer, Boolean> func) {
        switch ( type ) {
            case Confirm:
                player.openInventory(confirmInv);
                playerList.put(player, func);
                break;
            case ChooseInvSize:
                player.openInventory(inventorySizeInv);
                playerList.put(player, func);
                break;
            case ChooseItemType:
                player.openInventory(itemTypeInv);
                playerList.put(player, func);
                break;
            case LadderDifficulty:
                player.openInventory(ladderDifficultyInv);
                playerList.put(player, func);
                break;
            case FloorChose:
                player.openInventory(floorChooseInv);
                playerList.put(player, func);
                break;
        }
    }

    public static void textInput (String title, Player player, Function<String, Boolean> func) {
        new AnvilGUI.Builder().title(title).itemLeft(new ItemStack(Material.PAPER)).plugin(PracticePlugin.plugin).onComplete((p,s) -> {
            func.apply(s);
            return AnvilGUI.Response.close();
        }).open(player);
    }

    public static void heightInput (Player player, Consumer<Double> func/*ItemStack[] items, int mlgID*/) {
        new AnvilGUI.Builder().title("Type a good height.").text("").itemLeft(new ItemStack(Material.PAPER)).text(Configs.mlgHeightRange.a() + " - " + Configs.mlgHeightRange.b()).plugin(PracticePlugin.plugin).onComplete((p,s) -> {
            Tuple<Integer, Integer> range = Configs.mlgHeightRange;
            try {
                double y = Double.parseDouble(s);
                if ( y < range.a() || y > range.b() ) {
                    throw (new NumberFormatException());
                }

                func.accept(y);

                return AnvilGUI.Response.close();
            } catch (NumberFormatException e) {
                return AnvilGUI.Response.text("Between " + range.a() + " and " + range.b() + "!");
            }
        }).open(player);
    }

    public static void called (Player player, int slot) {
        boolean success = playerList.get(player).apply(slot);
        if ( success )
            playerList.remove(player);
    }

    public enum Type {
        Confirm,
        ChooseItemType,
        ChooseInvSize,
        LadderDifficulty,
        FloorChose
    }
}
