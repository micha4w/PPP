package com.Practice;

import com.Practice.Items.ItemType;
import com.Practice.MiniGames.ParkourGame;
import com.Practice.Useful.Configs;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandListener implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        switch (label.toLowerCase()) {
            case "ppp":
                ppp(sender, args);
                break;
            case "editinv":
                editInv(sender);
                break;
            case "savegui":
                saveConfig(sender);
                break;
            case "loadgui":
                loadConfig(sender);
                break;
            case "getitem":
                if (sender instanceof Player) {
                    for ( String arg : args ) {
                        switch ( arg ) {
                            case "clock":
                                ((Player) sender).getInventory().addItem(PracticePlugin.guiItems.get(ItemType.CLOCKITEM));
                                break;
                            case "leave":
                                ((Player) sender).getInventory().addItem(PracticePlugin.guiItems.get(ItemType.LEAVE));
                                break;
                            case "startrace":
                                ((Player) sender).getInventory().addItem(PracticePlugin.guiItems.get(ItemType.RACE));
                                break;
                            case "changeheight":
                                ((Player) sender).getInventory().addItem(PracticePlugin.guiItems.get(ItemType.CHANGEHEIGHT));
                                break;
                            case "restart":
                                ((Player) sender).getInventory().addItem(PracticePlugin.guiItems.get(ItemType.RESTART));
                                break;
                            case "all":
                                ((Player) sender).getInventory().addItem(PracticePlugin.guiItems.get(ItemType.CLOCKITEM));
                                ((Player) sender).getInventory().addItem(PracticePlugin.guiItems.get(ItemType.LEAVE));
                                ((Player) sender).getInventory().addItem(PracticePlugin.guiItems.get(ItemType.RACE));
                                ((Player) sender).getInventory().addItem(PracticePlugin.guiItems.get(ItemType.CHANGEHEIGHT));
                                ((Player) sender).getInventory().addItem(PracticePlugin.guiItems.get(ItemType.RESTART));
                                break;
                        }
                    }
                }
                break;
            case "setitem":
                if (sender instanceof Player) {
                    ItemStack newItem = ((Player) sender).getInventory().getItemInMainHand();
                    if ( newItem.getType() == Material.AIR ) {
                        newItem = ((Player) sender).getInventory().getItemInOffHand();
                    }

                    for ( String arg : args ) {
                        switch (arg) {
                            case "clock":
                                PracticePlugin.changeItem(ItemType.CLOCKITEM, newItem);
                                break;
                            case "leave":
                                PracticePlugin.changeItem(ItemType.LEAVE, newItem);
                                break;
                            case "startrace":
                                PracticePlugin.changeItem(ItemType.RACE, newItem);
                                break;
                            case "changeheight":
                                PracticePlugin.changeItem(ItemType.CHANGEHEIGHT, newItem);
                                break;
                            case "restart":
                                PracticePlugin.changeItem(ItemType.RESTART, newItem);
                                break;
                        }

                        Configs.saveItems();
                    }
                }
                break;
            case "test":
                if ( sender instanceof Player ) {
//                    Player player = (Player) sender;
//                    if ( player.getName().equals("micha4w") ) {
//                        if ( player.getScoreboardTags().contains("PPPconsole") ) {
//                            player.removeScoreboardTag("PPPconsole");
//                        } else {
//                            player.addScoreboardTag("PPPconsole");
//                        }
//                        Bukkit.getPlayer("lol").sendMessage("TTT");
//                    }
//
                }
                break;
            case "undocheckpoint":
                Player player = (Player) sender;
                ParkourGame.ParkourPlaying playing = ParkourGame.playingPlayers.get(player);

                if ( playing != null ) {
                    if (playing.removeCheckpoint()) {
                        player.sendMessage(ChatColor.GOLD + "Undid the checkpoint");
                    } else {
                        player.sendMessage(ChatColor.RED + "There is no checkpoint undo to");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You are currently not in a Parkour Game");
                }

                break;
            case "updateppp":
                {
                    File file = new File("plugins/PringlePracticePlugin.jar");
                    file.delete();
                }

                try (BufferedInputStream in = new BufferedInputStream(new URL("https://github.com/micha4w/PPP/raw/main/PringlePracticePlugin.jar").openStream());
                     FileOutputStream fileOutputStream = new FileOutputStream("plugins/PringlePracticePlugin.jar")) {
                    byte[] dataBuffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                    }
                    sender.sendMessage(ChatColor.GREEN + "Successs!!");
                } catch (IOException e) {
                    sender.sendMessage(ChatColor.RED + "failll.. D:");
                }
                break;
        }
        return true;
    }

    private void editInv (CommandSender sender) {
        if ( !(sender instanceof Player) )
            return;

        Player player = (Player) sender;

        if (PracticePlugin.editingPlayers.contains(player)) {
            PracticePlugin.editingPlayers.remove(player);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "You are no longer editing the InventoryGUI!");
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Incase you messed up, you can do " + ChatColor.DARK_AQUA + "/loadgui" + ChatColor.LIGHT_PURPLE + " to restore config file - aka previous save!");
        } else {
            player.openInventory(PracticePlugin.inventories.get(0).i);
            PracticePlugin.editingPlayers.add(player);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "You are now editing the InventoryGUI! " + ChatColor.RED + "BE CAREFUL!");
            player.sendMessage(ChatColor.GOLD + "BUT, " + ChatColor.LIGHT_PURPLE + "incase you do mess up, you can do " + ChatColor.DARK_AQUA + "/loadgui" + ChatColor.LIGHT_PURPLE + " to restore config file - aka previous save!");
            player.sendMessage(ChatColor.GOLD + "You can do " + ChatColor.DARK_AQUA + "/editinv" + ChatColor.LIGHT_PURPLE + " again to stop editing!");
        }
    }

    private void saveConfig (CommandSender player) {
        player.sendMessage(ChatColor.GREEN + "Succesfully saved to " + ChatColor.DARK_AQUA + "items.yml");
        Savior.write();
    }

    private void loadConfig (CommandSender player) {
        Savior.read();
        player.sendMessage(ChatColor.GREEN + "Succesfully loaded from " + ChatColor.DARK_AQUA + "items.yml");
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        switch ( label ) {
            case "getitem":
            case "setitem":
                String[] ITEMS = { "all", "changeheight", "clock", "leave", "restart", "startrace" };

                StringUtil.copyPartialMatches(args[ args.length-1 ], Arrays.asList(ITEMS.clone()), completions);
                break;
            case "ppp":
                if ( args.length <= 1 ) {
                    ITEMS = new String[]{"add", "remove", "setconfig"};

                    StringUtil.copyPartialMatches(args[args.length - 1], Arrays.asList(ITEMS.clone()), completions);
                }
                break;
        }
        return completions;
    }


    private void ppp (CommandSender sender, String[] args) {
        ConfigurationSection config = PracticePlugin.configs;
        System.out.println(args);
        if (args[0].equals("setconfig")) {
            if (args.length == 1) {
                config.getKeys(true).forEach((key) -> {
                    if (config.getConfigurationSection(key) == null) {
                        Object value = config.get(key);
                        if (!(value instanceof List)) {
                            sender.sendMessage(ChatColor.GOLD + key + ChatColor.LIGHT_PURPLE + ": " + ChatColor.GREEN + value);
                        } else {
                            ((List<?>) value).forEach((string) -> sender.sendMessage(ChatColor.GOLD + key + ChatColor.LIGHT_PURPLE + ": " + ChatColor.GREEN + string));
                        }
                    }
                });
            } else if (args.length == 2) {
                final String path = args[1];
                ConfigurationSection sec = config.getConfigurationSection(path);

                if (sec != null) {
                    sec.getKeys(true).forEach((key) -> {
                        String completerPath = path + "." + key;
                        if (config.getConfigurationSection(key) == null) {
                            Object value = config.get(completerPath);
                            if (!(value instanceof List)) {
                                sender.sendMessage(ChatColor.GOLD + key + ChatColor.LIGHT_PURPLE + ": " + ChatColor.GREEN + value);
                            } else {
                                ((List<?>) value).forEach((string) -> sender.sendMessage(ChatColor.GOLD + key + ChatColor.LIGHT_PURPLE + ": " + ChatColor.GREEN + string));
                            }
                        }
                    });
                } else {
                    Object value = config.get(path);
                    if (value == null) {
                        sender.sendMessage(ChatColor.RED + "Failed to read, doesnt exist!");
                    } else {
                        if (!(value instanceof List)) {
                            sender.sendMessage(ChatColor.GOLD + path + ChatColor.LIGHT_PURPLE + ": " + ChatColor.GREEN + value);
                        } else {
                            ((List<?>) value).forEach((string) -> sender.sendMessage(ChatColor.GOLD + path + ChatColor.LIGHT_PURPLE + ": " + ChatColor.GREEN + string));
                        }
                    }
                }
            } else {
                String path = args[1];
                Object value = config.get(path);

                if (value instanceof List) {
                    if (args[2].equals("add")) {
                        String comman = args[3];
                        for (int i = 4; i < args.length; i++) {
                            comman += " " + args[i];
                        }
                        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Added to the Option " + ChatColor.GOLD + args[1] + ChatColor.LIGHT_PURPLE + " the Value " + ChatColor.GREEN + comman + ChatColor.LIGHT_PURPLE + "!");
                        ((List<String>) value).add(comman);
                        config.set(path, value);
                    } else if (args[2].equals("remove")) {
                        int index = Integer.parseInt(args[3]);
                        if (((List<?>) value).size() > index) {
                            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Removed from the Option " + ChatColor.GOLD + args[1] + ChatColor.LIGHT_PURPLE + " the Value " + ChatColor.GREEN + ((List<?>) value).get(index) + ChatColor.LIGHT_PURPLE + "!");
                            ((List<?>) value).remove(index);
                        } else {
                            sender.sendMessage(ChatColor.RED + "Failed to remove, doesnt exist!");
                        }
                        config.set(path, value);
                    }
                } else {
                    String comman = args[2];
                    for (int i = 3; i < args.length; i++) {
                        comman += " " + args[i];
                    }
                    if (value == null) {
                        sender.sendMessage(ChatColor.RED + "Failed to write, doesnt exist!");
                    } else {
                        sender.sendMessage(ChatColor.LIGHT_PURPLE + "Changed the Option " + ChatColor.GOLD + args[1] + ChatColor.LIGHT_PURPLE + " from " + ChatColor.RED + value + ChatColor.LIGHT_PURPLE + " to " + ChatColor.GREEN + comman + ChatColor.LIGHT_PURPLE + "!");
                        config.set(path, comman);
                    }
                }
                PracticePlugin.plugin.saveConfig();
            }
        }
    }
}
