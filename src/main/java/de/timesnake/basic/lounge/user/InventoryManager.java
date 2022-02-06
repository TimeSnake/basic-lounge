package de.timesnake.basic.lounge.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.user.ExInventory;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickListener;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryInteractEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryInteractListener;
import de.timesnake.basic.bukkit.util.user.scoreboard.ItemHoldClick;
import de.timesnake.basic.game.util.GameServer;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.server.LoungeServerManager;
import de.timesnake.basic.lounge.server.TempGameServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class InventoryManager implements UserInventoryClickListener, UserInventoryInteractListener {

    private static final Integer LEAVE_TIME = 1200;

    public static final ExItemStack LEAVE_ITEM = new ExItemStack(Material.ANVIL, "§cLeave (hold right)", Collections.emptyList());

    public static final ExItemStack JOIN_LOUNGE_ITEM = new ExItemStack(0, "§6Join", Color.GRAY, Material.LEATHER_HELMET);

    public static final ExItemStack SETTINGS_ITEM = new ExItemStack(Material.CLOCK, "§6Settings", Collections.emptyList());

    public static final ExItemStack QUICK_START = new ExItemStack(0, Material.NETHER_STAR, "§6Quick Start", List.of("§fClick to start the game in 30s"));
    public static final ExItemStack START_SERVER = new ExItemStack(1, Material.BEACON, "§cGame Server", List.of("§fClick to force the game server to start", "§fUse only if the game is not starting"));
    public static final ExInventory SETTINGS = Server.createExInventory(9, "Settings", QUICK_START, START_SERVER);


    private final ExItemStack gameDescriptionItem = new ExItemStack(Material.WRITTEN_BOOK, "§6Game Description");

    private final HashMap<User, ItemHoldClick> clickedLeaveUsers = new HashMap<>();

    public InventoryManager() {

        BookMeta meta = ((BookMeta) this.gameDescriptionItem.getItemMeta());

        List<BaseComponent[]> pages = new ArrayList<>();

        for (String page : LoungeServer.getGame().getDescription()) {
            String[] lines = page.split("\\\\n");
            BaseComponent[] baseComponent = new BaseComponent[lines.length];
            for (int i = 0; i < lines.length; i++) {
                baseComponent[i] = new TextComponent(lines[i] + "\n");
            }
            pages.add(baseComponent);
        }

        meta.spigot().setPages(pages);
        meta.setAuthor("Game System");
        meta.setTitle(LoungeServer.getGame().getDisplayName());

        this.gameDescriptionItem.setItemMeta(meta);


        Server.getInventoryEventManager().addInteractListener(this, LEAVE_ITEM, SETTINGS_ITEM, JOIN_LOUNGE_ITEM, START_SERVER);
        Server.getInventoryEventManager().addClickListener(this, QUICK_START, START_SERVER);
    }


    @Override
    public void onUserInventoryClick(UserInventoryClickEvent e) {
        User user = e.getUser();
        ExItemStack item = e.getClickedItem();

        if (item.equals(QUICK_START)) {
            if (LoungeServer.getGameServer().getState().equals(TempGameServer.State.READY)) {
                if (LoungeServer.getTimeManager().isGameCountdownRunning()) {
                    if (LoungeServer.getGameCountdown() <= 30) {
                        user.sendPluginMessage(Plugin.LOUNGE, ChatColor.WARNING + "The game is already starting");
                        user.closeInventory();
                        return;
                    }
                    LoungeServer.setState(LoungeServerManager.State.STARTING);
                    LoungeServer.getTimeManager().setGameCountdown(30);
                    user.sendPluginMessage(Plugin.LOUNGE, ChatColor.PERSONAL + "Forced quick start");
                } else {
                    user.sendPluginMessage(Plugin.LOUNGE, ChatColor.WARNING + "The countdown must running to " + "force a quick-start");
                }
            } else {
                user.sendPluginMessage(Plugin.LOUNGE, ChatColor.WARNING + "The game server is not ready");
            }
            user.closeInventory();
            e.setCancelled(true);
        } else if (item.equals(START_SERVER)) {
            user.runCommand("/startserver");
            user.closeInventory();
        }
    }

    @Override
    public void onUserInventoryInteract(UserInventoryInteractEvent e) {
        User user = e.getUser();
        ExItemStack item = e.getClickedItem();

        if (item.equals(LEAVE_ITEM)) {
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
                if (!this.clickedLeaveUsers.containsKey(user)) {
                    this.clickedLeaveUsers.put(user, new ItemHoldClick(LEAVE_TIME));
                } else {
                    if (this.clickedLeaveUsers.get(user).click()) {
                        user.sendActionBarText("");
                        ((LoungeUser) user).leaveLounge();
                        e.setCancelled(true);
                    } else {
                        user.sendActionBarText("§cLeaving...");
                    }
                }
            }
        } else if (item.equals(SETTINGS_ITEM)) {
            ((LoungeUser) user).openInventorySettings();
            e.setCancelled(true);
        } else if (item.equals(JOIN_LOUNGE_ITEM)) {
            if (Server.getGameNotServiceUsers().size() >= GameServer.getGame().getMaxPlayers()) {
                user.sendPluginMessage(Plugin.LOUNGE, ChatColor.WARNING + "The game is full");
                e.setCancelled(true);
            }
            ((LoungeUser) user).joinLounge();
            LoungeServer.getLoungeScoreboardManager().getTablist().addEntry(user);
            user.sendPluginMessage(Plugin.LOUNGE, ChatColor.PERSONAL + "Joined the game");
            LoungeServer.checkAutoStart();
            e.setCancelled(true);
        }
    }

    public ExItemStack getGameDescriptionItem() {
        return gameDescriptionItem;
    }
}
