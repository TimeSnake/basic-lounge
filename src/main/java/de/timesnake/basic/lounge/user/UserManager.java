/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserDamageByUserEvent;
import de.timesnake.basic.bukkit.util.user.event.UserDamageEvent;
import de.timesnake.basic.bukkit.util.user.event.UserJoinEvent;
import de.timesnake.basic.bukkit.util.user.event.UserQuitEvent;
import de.timesnake.basic.lounge.main.BasicLounge;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.waitinggames.WaitingGameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class UserManager implements Listener {

  public UserManager() {
    Server.registerListener(this, BasicLounge.getPlugin());
  }

  @EventHandler
  public void onUserJoin(UserJoinEvent e) {
    User user = e.getUser();
    String task = user.getTask();

    // spectator
    if (user.getStatus().equals(Status.User.SPECTATOR)) {
      ((LoungeUser) user).joinSpectator();
      user.sendPluginTDMessage(LoungeServer.PLUGIN, "§sYou can join the game in a few moments");
      return;
    }

    if (LoungeServer.getGame().hasTexturePack()) {
      user.sendPluginTDMessage(LoungeServer.PLUGIN, "§wThis game uses a texture pack. "
                                                    + "It is highly recommended to use the texture pack. The texture " +
                                                    "pack will be loaded at the game start.");
    }

    // game user
    if (task != null) {
      if (task.equalsIgnoreCase(LoungeServer.getGame().getName())) {
        ((LoungeUser) user).joinLounge();
        LoungeServer.getStateManager().onPlayerUpdate();
        return;
      }
    }

    user.sendPluginTDMessage(LoungeServer.PLUGIN, "§wYou didn't joined the lounge!");
    user.asSender(LoungeServer.PLUGIN).sendTDMessageCommandHelp("Use", "service");
    user.getInventory().clear();
  }

  @EventHandler
  public void onUserQuit(UserQuitEvent e) {
    User user = e.getUser();
    ((LoungeUser) user).setSelectedMap(null);
    ((LoungeUser) user).setSelectedTeam(null);

    LoungeServer.getStateManager().onPlayerUpdate();
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent e) {
    if (!Server.getUser(e.getWhoClicked().getUniqueId()).isService()) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent e) {
    if (!Server.getUser(e.getPlayer().getUniqueId()).isService()) {
      if (e.getTo().getBlockY() <= e.getTo().getWorld().getMinHeight()) {
        e.getPlayer().teleport(LoungeServer.getSpawn());
      }
    }
  }

  @EventHandler
  public void onUserDamage(UserDamageEvent e) {
    if (!e.getDamageCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)
        && !e.getDamageCause().equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onUserDamageByUser(UserDamageByUserEvent e) {
    WaitingGameManager waitingGameManager = LoungeServer.getWaitingGameManager();

    boolean gameManaged = waitingGameManager.onUserDamageByUser(e);

    if (!gameManaged) {
      e.setCancelled(true);
    }
  }
}
