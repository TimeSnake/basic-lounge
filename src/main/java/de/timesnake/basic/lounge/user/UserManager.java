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
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.server.LoungeServerManager;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.chat.ExTextColor;
import de.timesnake.library.waitinggames.WaitingGameManager;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class UserManager implements Listener {

  @EventHandler
  public void onUserJoin(UserJoinEvent e) {
    User user = e.getUser();
    String task = user.getTask();

    // spectator
    if (user.getStatus().equals(Status.User.SPECTATOR)) {
      ((LoungeUser) user).joinSpectator();
      user.sendPluginMessage(Plugin.LOUNGE, Component.text("You can join the game in a few moments", ExTextColor.WARNING));
      return;
    }

    if (LoungeServer.getGame().hasTexturePack()) {
      user.sendPluginMessage(Plugin.LOUNGE, Component.text("This game uses a texture pack. "
              + "It is highly recommended to use the texture pack. The texture pack will be loaded at the game start.",
          ExTextColor.WARNING));
    }

    // game user
    if (task != null) {
      if (task.equalsIgnoreCase(LoungeServer.getGame().getName())) {
        ((LoungeUser) user).joinLounge();
        LoungeServer.getTimeManager().checkCountdown();
        LoungeServer.getLoungeScoreboardManager().updateScoreboardPlayerNumber();

        if (Server.getGameNotServiceUsers().size() == 1) {
          if (user.getLastServer() != null) {
            if (user.getLastServer().getPort()
                .equals(LoungeServer.getGameServer().getPort())) {
              LoungeServer.setState(LoungeServerManager.State.WAITING);
            }
          }
          LoungeServer.getCurrentMap().getWorld().setTime(0);
          LoungeServer.getGameServer().start();
        }
        return;
      }
    }

    user.sendPluginMessage(Plugin.LOUNGE,
        Component.text("You didn't joined the lounge!", ExTextColor.WARNING));
    user.asSender(Plugin.LOUNGE).sendTDMessageCommandHelp("Use", "service");
    user.getInventory().clear();
  }

  @EventHandler
  public void onUserQuit(UserQuitEvent e) {
    User user = e.getUser();
    String task = user.getTask();
    if (task != null) {
      if (task.equalsIgnoreCase(LoungeServer.getGame().getName())) {
        int size = Server.getPreGameUsers().size();
        LoungeServer.getLoungeScoreboardManager().updateScoreboardPlayerNumber(size);

        if (size <= LoungeServer.getGame().getAutoStartPlayerNumber()
            || (LoungeServer.getGame().isEqualTimeSizeRequired()
            && size % LoungeServer.getGame().getTeams().size() != 0)) {
          LoungeServer.resetGameCountdown();
        }

        ((LoungeUser) user).setSelectedMap(null);
        ((LoungeUser) user).setSelectedTeam(null);
      }
    }
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

    boolean gameManaged = waitingGameManager.onUserDamage(e);

    if (!gameManaged) {
      e.setCancelled(true);
    }
  }
}
