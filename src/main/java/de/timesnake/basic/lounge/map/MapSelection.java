/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.basic.lounge.map;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.ExInventory;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickListener;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryInteractEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryInteractListener;
import de.timesnake.basic.game.util.game.Map;
import de.timesnake.basic.game.util.server.GameServer;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.library.basic.util.chat.ExTextColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class MapSelection implements UserInventoryClickListener, UserInventoryInteractListener, InventoryHolder {

    private static final org.bukkit.ChatColor NAME_COLOR = ChatColor.GOLD;
    private static final org.bukkit.ChatColor DESCRIPTION_COLOR = ChatColor.WHITE;
    private static final org.bukkit.ChatColor AUTHORS_COLOR = ChatColor.BLUE;

    private final ExInventory inventory;
    private final ExItemStack item;
    private final HashMap<ExItemStack, Map> mapsByItem = new HashMap<>();

    public MapSelection(Collection<Map> maps) {
        this.item = new ExItemStack(Material.MAP).setDisplayName("§6Maps");

        int invSize = (int) (9 * Math.ceil(GameServer.getGame().getMaps().size() / 7.0));
        this.inventory = new ExInventory(invSize > 0 ? invSize : 9, Component.text("Map-Voting"), this);

        ExItemStack randomMapItem = new ExItemStack(Material.GRAY_WOOL).setDisplayName("§fRandom")
                .setLore(ChatColor.GRAY + "Vote for a random map");

        this.inventory.setItemStack(0, randomMapItem);

        int slot = 2;
        for (Map map : maps) {
            // first and second inventory column empty (for random selection)
            if (slot % 9 == 0) slot += 2;

            ExItemStack item = map.getItem().cloneWithId().setDisplayName(NAME_COLOR + map.getDisplayName());

            LinkedList<String> lore = new LinkedList<>();
            lore.addLast("");

            for (String descriptionLine : map.getDescription()) {
                lore.add("§f" + descriptionLine);
            }

            if (map.getAuthors() != null) {
                lore.add("");
                lore.add(DESCRIPTION_COLOR + "by ");
                for (String authors : map.getAuthors(20)) {
                    lore.addLast(AUTHORS_COLOR + authors);
                }
            }
            item.setLore(lore);

            this.inventory.setItemStack(slot, item);
            this.mapsByItem.put(item, map);
            slot++;
        }

        if (this.mapsByItem.isEmpty()) {
            if (LoungeServer.getGameServer().areMapsEnabled()) {
                Server.printWarning(Plugin.LOUNGE, "No map for player amount found");
                Bukkit.shutdown();
            }
        }

        Server.getInventoryEventManager().addClickListener(this, this);
        Server.getInventoryEventManager().addInteractListener(this, this.item);
    }

    @Override
    public void onUserInventoryClick(UserInventoryClickEvent e) {
        LoungeUser user = ((LoungeUser) e.getUser());
        ExItemStack clickedItem = e.getClickedItem();

        Sender sender = user.asSender(Plugin.LOUNGE);
        if (LoungeServer.getGameCountdown() <= LoungeServer.MAP_SELECTION_CLOSED) {
            sender.sendPluginMessage(Component.text("The map voting is closed", ExTextColor.WARNING));
            e.setCancelled(true);
            return;
        }

        Map map = this.mapsByItem.get(clickedItem);
        if (map != null) {
            user.setSelectedMap(map);
            sender.sendPluginMessage(Component.text("Voted for map ", ExTextColor.PERSONAL)
                    .append(Component.text(map.getDisplayName(), ExTextColor.VALUE)));
        } else {
            user.setSelectedMap(null);
            sender.sendPluginMessage(Component.text("Voted for a random map", ExTextColor.PERSONAL));
        }
        user.closeInventory();
        e.setCancelled(true);
    }

    @Override
    public void onUserInventoryInteract(UserInventoryInteractEvent e) {
        LoungeUser user = ((LoungeUser) e.getUser());
        Sender sender = user.asSender(Plugin.LOUNGE);
        if (LoungeServer.getGameCountdown() <= LoungeServer.MAP_SELECTION_CLOSED) {
            sender.sendPluginMessage(Component.text("The map voting is closed", ExTextColor.WARNING));
            e.setCancelled(true);
            return;
        }
        user.openInventoryMapSelection();
        e.setCancelled(true);
    }

    @Override
    public Inventory getInventory() {
        return inventory.getInventory();
    }

    public ExItemStack getItem() {
        return item;
    }
}
