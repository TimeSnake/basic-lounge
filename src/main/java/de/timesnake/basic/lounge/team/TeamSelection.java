/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.team;

import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.basic.bukkit.util.user.inventory.ExInventory;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.user.LoungeUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

public class TeamSelection {

  private final Logger logger = LogManager.getLogger("lounge.team.selection");

  private final ExItemStack invItem;
  private final ExInventory inventory;
  private final HashMap<ExItemStack, LoungeTeam> teams = new HashMap<>();

  private boolean blocked = false;
  private boolean silentBlocked = false;

  public TeamSelection() {
    this.invItem = ExItemStack.getLeatherArmor(Material.LEATHER_HELMET, "§6Teamselection", Color.BLACK)
        .hideAll()
        .immutable()
        .onInteract(event -> {
          LoungeUser user = ((LoungeUser) event.getUser());
          Sender sender = user.asSender(Plugin.LOUNGE);
          if (LoungeServer.getGameCountdown() <= LoungeServer.TEAM_SELECTION_CLOSED) {
            sender.sendPluginTDMessage("§wThe team selection is closed");
            event.setCancelled(true);
            return;
          }
          user.openInventoryTeamSelection();
          event.setCancelled(true);
        });

    int invSize = (int) (9 * Math.ceil(LoungeServer.getGameServer().getTeamAmount() / 7.0));
    this.inventory = new ExInventory(invSize > 0 ? invSize : 9, "Teamselection");

    ExItemStack randomTeamItem = ExItemStack.getLeatherArmor(Material.LEATHER_HELMET, "§fRandom", Color.GRAY)
        .setLore(ChatColor.GRAY + "Join Random team")
        .hideAll()
        .immutable()
        .onClick(event -> {
          LoungeUser user = ((LoungeUser) event.getUser());
          Sender sender = user.asSender(Plugin.LOUNGE);
          if (LoungeServer.getGameCountdown() <= LoungeServer.TEAM_SELECTION_CLOSED) {
            sender.sendPluginTDMessage("§wTeam selection is closed");
            user.closeInventory();
            event.setCancelled(true);
            return;
          }

          user.setSelectedTeam(null);
          sender.sendPluginTDMessage("§sYou selected team §vRandom");
          this.logger.info("'{}' selected team random", user.getName());

          user.closeInventory();
          event.setCancelled(true);
        });

    this.inventory.setItemStack(0, randomTeamItem);
  }

  public boolean isBlocked() {
    return blocked;
  }

  public void block(boolean allowed) {
    this.blocked = allowed;
    if (allowed) {
      this.silentBlocked = false;
    }
  }

  public boolean isSilentBlocked() {
    return silentBlocked;
  }

  public void blockSilent(boolean blockSilent) {
    this.block(blockSilent);
    this.silentBlocked = blockSilent;
  }

  protected void loadTeams() {
    int i = 2;
    for (Team team : LoungeServer.getGame()
        .getTeamsSortedByRank(LoungeServer.getGameServer().getTeamAmount())) {
      // first and second inventory column empty (for random selection)
      if (i % 9 == 0) {
        i += 2;
      }

      // create item
      ExItemStack item = ((LoungeTeam) team).createTeamItem(this, i);
      // team adds item
      this.teams.put(item, ((LoungeTeam) team));
      i++;
    }
  }

  public Inventory getInventory() {
    return inventory.getInventory();
  }

  public ExInventory getExInventory() {
    return this.inventory;
  }

  public ExItemStack getItem() {
    return invItem;
  }

}
