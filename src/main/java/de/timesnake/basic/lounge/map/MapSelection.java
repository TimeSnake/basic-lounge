/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.map;

import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.user.inventory.ExInventory;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.game.util.game.Map;
import de.timesnake.basic.game.util.server.GameServer;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.user.LoungeUser;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class MapSelection {

  private static final org.bukkit.ChatColor NAME_COLOR = ChatColor.GOLD;
  private static final org.bukkit.ChatColor DESCRIPTION_COLOR = ChatColor.WHITE;
  private static final org.bukkit.ChatColor AUTHORS_COLOR = ChatColor.BLUE;

  private final Logger logger = LogManager.getLogger("lounge.map.selection");

  private final ExInventory inventory;
  private final ExItemStack item;

  public MapSelection(Collection<Map> maps) {
    this.item = new ExItemStack(Material.MAP)
        .setDisplayName("§6Maps")
        .onInteract(event -> {
          LoungeUser user = ((LoungeUser) event.getUser());
          if (LoungeServer.getGameCountdown() <= LoungeServer.MAP_SELECTION_CLOSED) {
            user.sendPluginTDMessage(Plugin.LOUNGE, "§wMap voting is closed");
            return;
          }
          user.openInventory(this.getInventory());
        }, true);

    int invSize = (int) (9 * Math.ceil(GameServer.getGame().getMaps().size() / 7.0));
    this.inventory = new ExInventory(invSize > 0 ? invSize : 9, Component.text("Map-Voting"));

    ExItemStack randomMapItem = new ExItemStack(Material.GRAY_WOOL)
        .setDisplayName("§fRandom")
        .setLore(ChatColor.GRAY + "Vote for a random map")
        .onClick(event -> {
          LoungeUser user = ((LoungeUser) event.getUser());

          if (LoungeServer.getGameCountdown() <= LoungeServer.MAP_SELECTION_CLOSED) {
            user.sendPluginTDMessage(Plugin.LOUNGE, "§wMap voting is closed");
            return;
          }

          user.setSelectedMap(null);
          user.sendPluginTDMessage(Plugin.LOUNGE, "§sVoted for a random map");
          user.closeInventory();
        }, true);

    this.inventory.setItemStack(0, randomMapItem);

    int slot = 2;
    HashMap<ExItemStack, Map> mapsByItem = new HashMap<>();
    for (Map map : maps) {
      // first and second inventory column empty (for random selection)
      if (slot % 9 == 0) {
        slot += 2;
      }

      ExItemStack item = this.createMapItem(map);

      this.inventory.setItemStack(slot, item);
      mapsByItem.put(item, map);
      slot++;
    }

    if (mapsByItem.isEmpty()) {
      if (LoungeServer.getGameServer().areMapsEnabled()) {
        this.logger.warn("No map for player amount found");
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
          if (LoungeServer.getGameCountdown() <= LoungeServer.MAP_SELECTION_CLOSED) {
            user.sendPluginTDMessage(Plugin.LOUNGE, "§wMap voting is closed");
            return;
          }

          user.setSelectedMap(map);
          user.sendPluginTDMessage(Plugin.LOUNGE, "§sVoted for map §v" + map.getDisplayName());
          user.closeInventory();
        }, true);

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
