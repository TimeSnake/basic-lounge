/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.map;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.inventory.ExInventory;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.game.util.game.Map;
import de.timesnake.basic.game.util.server.GameServer;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.library.basic.util.chat.ExTextColor;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

public class MapSelection {

    private static final org.bukkit.ChatColor NAME_COLOR = ChatColor.GOLD;
    private static final org.bukkit.ChatColor DESCRIPTION_COLOR = ChatColor.WHITE;
    private static final org.bukkit.ChatColor AUTHORS_COLOR = ChatColor.BLUE;

    private final ExInventory inventory;
    private final ExItemStack item;
    private final HashMap<ExItemStack, Map> mapsByItem = new HashMap<>();

    public MapSelection(Collection<Map> maps) {
        this.item = new ExItemStack(Material.MAP)
                .setDisplayName("§6Maps")
                .onClick(event -> {
                    LoungeUser user = ((LoungeUser) event.getUser());
                    Sender sender = user.asSender(Plugin.LOUNGE);
                    if (LoungeServer.getGameCountdown() <= LoungeServer.MAP_SELECTION_CLOSED) {
                        sender.sendPluginMessage(
                                Component.text("The map voting is closed", ExTextColor.WARNING));
                        event.setCancelled(true);
                        return;
                    }
                    user.openInventoryMapSelection();
                    event.setCancelled(true);
                });

        int invSize = (int) (9 * Math.ceil(GameServer.getGame().getMaps().size() / 7.0));
        this.inventory = new ExInventory(invSize > 0 ? invSize : 9, Component.text("Map-Voting"));

        ExItemStack randomMapItem = new ExItemStack(Material.GRAY_WOOL)
                .setDisplayName("§fRandom")
                .setLore(ChatColor.GRAY + "Vote for a random map")
                .onClick(event -> {
                    LoungeUser user = ((LoungeUser) event.getUser());
                    Sender sender = user.asSender(Plugin.LOUNGE);

                    if (LoungeServer.getGameCountdown() <= LoungeServer.MAP_SELECTION_CLOSED) {
                        sender.sendPluginMessage(
                                Component.text("The map voting is closed", ExTextColor.WARNING));
                        event.setCancelled(true);
                        return;
                    }

                    user.setSelectedMap(null);
                    sender.sendPluginMessage(
                            Component.text("Voted for a random map", ExTextColor.PERSONAL));
                    user.closeInventory();
                    event.setCancelled(true);
                });

        this.inventory.setItemStack(0, randomMapItem);

        int slot = 2;
        for (Map map : maps) {
            // first and second inventory column empty (for random selection)
            if (slot % 9 == 0) {
                slot += 2;
            }

            ExItemStack item = this.createMapItem(map);

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
    }

    private ExItemStack createMapItem(Map map) {
        ExItemStack item = map.getItem()
                .cloneWithId()
                .setDisplayName(NAME_COLOR + map.getDisplayName())
                .onClick(event -> {
                    LoungeUser user = ((LoungeUser) event.getUser());
                    Sender sender = user.asSender(Plugin.LOUNGE);

                    if (LoungeServer.getGameCountdown() <= LoungeServer.MAP_SELECTION_CLOSED) {
                        sender.sendPluginMessage(
                                Component.text("The map voting is closed", ExTextColor.WARNING));
                        event.setCancelled(true);
                        return;
                    }

                    user.setSelectedMap(map);
                    sender.sendPluginMessage(Component.text("Voted for map ", ExTextColor.PERSONAL)
                            .append(Component.text(map.getDisplayName(), ExTextColor.VALUE)));
                    user.closeInventory();
                    event.setCancelled(true);
                });

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
        return item;
    }

    public Inventory getInventory() {
        return inventory.getInventory();
    }

    public ExItemStack getItem() {
        return item;
    }
}
