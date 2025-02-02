/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.server;

import de.timesnake.basic.game.util.game.TmpGame;
import de.timesnake.basic.game.util.server.GameServer;
import de.timesnake.basic.lounge.kit.KitManager;
import de.timesnake.basic.lounge.map.LoungeMap;
import de.timesnake.basic.lounge.map.MapManager;
import de.timesnake.basic.lounge.scoreboard.LoungeScoreboardManager;
import de.timesnake.basic.lounge.team.TeamManager;
import de.timesnake.basic.lounge.user.InventoryManager;
import de.timesnake.library.chat.Plugin;
import de.timesnake.library.waitinggames.WaitingGameManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;

public class LoungeServer extends GameServer {

  public static final Plugin PLUGIN = new Plugin("Lounge", "BLB");

  public static final int MAP_SELECTION_CLOSED = 20;
  public static final int JOINING_CLOSED = 16;
  public static final int KIT_SELECTION_CLOSED = 14;
  public static final int TEAM_SELECTION_CLOSED = 14;

  public static final int MAP_SELECTION = 16;
  public static final int TEAM_CREATION = 14;
  public static final int KIT_LOADING = 14;

  public static TmpGame getGame() {
    return server.getGame();
  }

  public static InventoryManager getInventoryManager() {
    return server.getInventoryManager();
  }

  public static int getGameCountdown() {
    return server.getGameCountdown();
  }

  public static void prepareLounge() {
    server.prepareLounge();
  }

  public static void startGame() {
    server.startGame();
  }

  public static Location getSpawn() {
    return server.getSpawn();
  }

  public static void broadcastLoungeMessage(Component msg) {
    server.broadcastLoungeMessage(msg);
  }

  public static void broadcastLoungeTDMessage(String msg) {
    server.broadcastLoungeTDMessage(msg);
  }

  public static void broadcastCountdownCancelledMessage() {
    server.broadcastCountdownCancelledMessage();
  }

  public static LoungeScoreboardManager getLoungeScoreboardManager() {
    return server.getLoungeScoreboardManager();
  }

  public static Integer getTeamAmount() {
    return server.getTeamAmount();
  }

  public static TmpGameServer getGameServer() {
    return server.getGameServer();
  }

  public static LoungeMap getCurrentMap() {
    return server.getCurrentMap();
  }

  public static Scheduler getTimeManager() {
    return server.getTimeManager();
  }

  public static MapManager getMapManager() {
    return server.getMapManager();
  }

  public static KitManager getKitManager() {
    return server.getKitManager();
  }

  public static TeamManager getTeamManager() {
    return server.getTeamManager();
  }

  public static WaitingGameManager getWaitingGameManager() {
    return server.getWaitingGameManager();
  }

  public static DiscordManager getDiscordManager() {
    return server.getDiscordManager();
  }

  public static StateManager getStateManager() {
    return server.getStateManager();
  }

  public static ResourcePackManager getResourcePackManager() {
    return server.getResourcePackManager();
  }

  private static final LoungeServerManager server = LoungeServerManager.getInstance();
}
