/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.chat.Chat;
import de.timesnake.basic.bukkit.util.exception.UnsupportedGroupRankException;
import de.timesnake.basic.bukkit.util.exception.WorldNotExistException;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.bukkit.util.user.scoreboard.Tablist;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.game.util.game.TmpGame;
import de.timesnake.basic.game.util.server.GameServerManager;
import de.timesnake.basic.game.util.user.SpectatorManager;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.kit.KitManager;
import de.timesnake.basic.lounge.main.BasicLounge;
import de.timesnake.basic.lounge.map.LoungeMap;
import de.timesnake.basic.lounge.map.MapManager;
import de.timesnake.basic.lounge.scoreboard.LoungeScoreboardManager;
import de.timesnake.basic.lounge.team.LoungeTeam;
import de.timesnake.basic.lounge.team.TeamManager;
import de.timesnake.basic.lounge.user.InventoryManager;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.basic.lounge.user.UserManager;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.database.util.game.DbLoungeMap;
import de.timesnake.database.util.game.DbTeam;
import de.timesnake.database.util.game.DbTmpGame;
import de.timesnake.database.util.server.DbLoungeServer;
import de.timesnake.database.util.server.DbTmpGameServer;
import de.timesnake.library.pets.PetManager;
import de.timesnake.library.waitinggames.WaitingGameManager;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LoungeServerManager extends GameServerManager<TmpGame> implements Listener,
    ChannelListener {

  public static LoungeServerManager getInstance() {
    return (LoungeServerManager) ServerManager.getInstance();
  }

  private final Logger logger = LogManager.getLogger("lounge.server");

  protected final List<LoungeMap> loungeMaps = new ArrayList<>();

  private InventoryManager inventoryManager;
  private LoungeScoreboardManager scoreboardManager;

  private StateManager stateManager;
  private Scheduler scheduler;

  private MapManager mapManager;
  private KitManager kitManager;
  private TeamManager teamManager;

  private WaitingGameManager waitingGameManager;
  private StatsManager statsManager;
  private PetManager petManager;

  private DiscordManager discordManager;

  protected LoungeMap currentMap;
  private TmpGameServer tmpGameServer;

  public void onLoungeEnable() {
    super.onGameEnable();

    Server.registerListener(this, BasicLounge.getPlugin());

    DbTmpGameServer gameDbServer = ((DbLoungeServer) Server.getDatabase()).getTwinServer();

    if (gameDbServer == null || !gameDbServer.exists()) {
      this.logger.error("Game server not defined");
      Bukkit.shutdown();
      return;
    }

    this.tmpGameServer = new TmpGameServer(gameDbServer);

    // lounge maps
    for (DbLoungeMap map : Database.getLounges().getCachedMaps()) {
      LoungeMap loungeMap;
      try {
        loungeMap = new LoungeMap(map);
      } catch (WorldNotExistException e) {
        this.logger.warn("Map '{}' could not loaded, world '{}' not exists", map.getName(), e.getWorldName());
        continue;
      }
      loungeMap.getWorld().setExceptService(true);
      loungeMap.getWorld().restrict(ExWorld.Restriction.FOOD_CHANGE, true);
      loungeMap.getWorld().restrict(ExWorld.Restriction.ENTITY_EXPLODE, true);
      loungeMap.getWorld().restrict(ExWorld.Restriction.DROP_PICK_ITEM, true);
      loungeMap.getWorld().restrict(ExWorld.Restriction.BLOCK_BREAK, true);
      loungeMap.getWorld().restrict(ExWorld.Restriction.BLOCK_PLACE, true);
      loungeMap.getWorld().restrict(ExWorld.Restriction.BLOCK_BURN_UP, true);
      loungeMap.getWorld().restrict(ExWorld.Restriction.FIRE_SPREAD_SPEED, 0f);
      loungeMap.getWorld().restrict(ExWorld.Restriction.PLACE_IN_BLOCK, true);
      loungeMap.getWorld().setGameRule(GameRule.DO_WEATHER_CYCLE, false);
      this.loungeMaps.add(loungeMap);
      this.logger.info("Loaded map '{}'", loungeMap.getName());
    }

    if (this.loungeMaps.isEmpty()) {
      this.logger.error("No lounge-map found");
      Bukkit.shutdown();
    }

    this.loadRandomLoungeMap();

    if (Server.getTask() == null) {
      this.logger.error("Task is null");
      Bukkit.shutdown();
    }

    new UserManager();

    this.mapManager = new MapManager();

    this.kitManager = new KitManager();

    this.teamManager = new TeamManager();
    this.teamManager.loadTeamsIntoInventory();

    inventoryManager = new InventoryManager();

    this.scheduler = new Scheduler();

    this.scoreboardManager = new LoungeScoreboardManager();

    this.waitingGameManager = new WaitingGameManager();
    this.petManager = new PetManager(BasicLounge.getPlugin());

    this.statsManager = new StatsManager();
    this.discordManager = new DiscordManager();

    this.stateManager = new StateManager();

    this.logger.info("Server loaded");
  }

  public final void onLoungeDisable() {
    this.discordManager.cleanup();
  }

  @Override
  public LoungeUser loadUser(Player player) {
    return new LoungeUser(player);
  }

  @Override
  public Sideboard getGameSideboard() {
    return LoungeServerManager.this.getLoungeScoreboardManager().getSideboard();
  }

  @Override
  public Tablist getGameTablist() {
    return LoungeServerManager.this.getLoungeScoreboardManager().getTablist();
  }

  @Override
  protected TmpGame loadGame(DbGame dbGame, boolean loadWorlds) {
    return new TmpGame((DbTmpGame) dbGame, false) {
      @Override
      public LoungeTeam loadTeam(DbTeam team) throws UnsupportedGroupRankException {
        return new LoungeTeam(team);
      }
    };
  }

  @Override
  protected SpectatorManager initSpectatorManager() {
    return new SpectatorManager() {

      @Override
      public @Nullable Sideboard getSpectatorSideboard() {
        return LoungeServerManager.this.getLoungeScoreboardManager().getSpectatorSideboard();
      }

      @Override
      public @Nullable Chat getSpectatorChat() {
        return null;
      }

      @Override
      public ExLocation getSpectatorSpawn() {
        return null;
      }

      @Override
      public boolean loadTools() {
        return false;
      }
    };
  }

  public InventoryManager getInventoryManager() {
    return inventoryManager;
  }

  public int getGameCountdown() {
    return this.scheduler.getGameCountdown();
  }

  public void prepareLounge() {
    this.scheduler.resetGameCountdown();
    this.loadRandomLoungeMap();
    this.teamManager.resetTeams();
    this.logger.info("Prepared lounge");
  }

  public void startGame() {
    Server.getChat().setBroadcastJoinQuit(false);

    Server.runTaskLoopAsynchrony((user) -> ((LoungeUser) user).switchToGameServer(), Server.getGameUsers(),
        BasicLounge.getPlugin());
    Server.runTaskLaterSynchrony(() -> Server.getChat().setBroadcastJoinQuit(true), 5 * 20, BasicLounge.getPlugin());
  }

  public Location getSpawn() {
    return this.currentMap.getSpawn();
  }

  public void broadcastLoungeMessage(Component msg) {
    Server.broadcastMessage(Plugin.LOUNGE, msg);
  }

  public void broadcastLoungeTDMessage(String msg) {
    Server.broadcastTDMessage(Plugin.LOUNGE, msg);
  }

  public void broadcastCountdownCancelledMessage() {
    if (this.stateManager.getState().equals(StateManager.State.STARTING)) {
      this.broadcastLoungeTDMessage("§wCountdown cancelled");
    }
  }

  public void loadRandomLoungeMap() {
    Server.runTaskSynchrony(() -> {
      int index = (int) (Math.random() * this.loungeMaps.size());
      this.currentMap = this.loungeMaps.get(index);
      this.currentMap.getWorld().loadChunk(this.currentMap.getSpawn().getChunk());
      this.currentMap.getWorld().setTime(0);
      this.statsManager.updateGlobalDisplays();
      this.logger.info("Loaded map " + this.currentMap.getName());
    }, BasicLounge.getPlugin());
  }

  public TmpGameServer getGameServer() {
    return tmpGameServer;
  }

  public LoungeMap getCurrentMap() {
    return this.currentMap;
  }

  public Scheduler getTimeManager() {
    return scheduler;
  }

  public KitManager getKitManager() {
    return kitManager;
  }

  public MapManager getMapManager() {
    return mapManager;
  }

  public TeamManager getTeamManager() {
    return teamManager;
  }

  public LoungeScoreboardManager getLoungeScoreboardManager() {
    return this.scoreboardManager;
  }

  public Integer getTeamAmount() {
    return this.getGameServer().getTeamAmount();
  }

  public WaitingGameManager getWaitingGameManager() {
    return waitingGameManager;
  }

  public DiscordManager getDiscordManager() {
    return discordManager;
  }

  public StateManager getStateManager() {
    return stateManager;
  }

}
