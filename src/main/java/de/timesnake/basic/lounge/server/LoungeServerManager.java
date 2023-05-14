/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.chat.Chat;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.exception.UnsupportedGroupRankException;
import de.timesnake.basic.bukkit.util.exception.WorldNotExistException;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.bukkit.util.user.scoreboard.Tablist;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.game.util.game.Map;
import de.timesnake.basic.game.util.game.TmpGame;
import de.timesnake.basic.game.util.server.GameServerManager;
import de.timesnake.basic.game.util.user.SpectatorManager;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.kit.KitManager;
import de.timesnake.basic.lounge.main.BasicLounge;
import de.timesnake.basic.lounge.map.LoungeMap;
import de.timesnake.basic.lounge.map.MapManager;
import de.timesnake.basic.lounge.scoreboard.ScoreboardManager;
import de.timesnake.basic.lounge.team.LoungeTeam;
import de.timesnake.basic.lounge.team.TeamManager;
import de.timesnake.basic.lounge.user.InventoryManager;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.basic.lounge.user.UserManager;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.database.util.game.DbLoungeMap;
import de.timesnake.database.util.game.DbMap;
import de.timesnake.database.util.game.DbTeam;
import de.timesnake.database.util.game.DbTmpGame;
import de.timesnake.database.util.server.DbLoungeServer;
import de.timesnake.database.util.server.DbTmpGameServer;
import de.timesnake.library.basic.util.Loggers;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.waitinggames.WaitingGameManager;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

public class LoungeServerManager extends GameServerManager<TmpGame> implements Listener,
        ChannelListener {

    public static LoungeServerManager getInstance() {
        return (LoungeServerManager) ServerManager.getInstance();
    }

    protected final List<LoungeMap> loungeMaps = new ArrayList<>();
    private final UserManager userManager = new UserManager();
    protected LoungeMap currentMap;
    private TmpGameServer tmpGameServer;
    private InventoryManager inventoryManager;
    private de.timesnake.basic.lounge.scoreboard.ScoreboardManager scoreboardManager;
    private MapManager mapManager;
    private KitManager kitManager;
    private TeamManager teamManager;
    private Scheduler scheduler;
    private WaitingGameManager waitingGameManager;
    private StatsManager statsManager;
    private DiscordManager discordManager;
    private State state;

    public void onLoungeEnable() {
        super.onGameEnable();

        Server.registerListener(this, BasicLounge.getPlugin());

        DbTmpGameServer gameDbServer = ((DbLoungeServer) Server.getDatabase()).getTwinServer();

        if (gameDbServer == null || !gameDbServer.exists()) {
            Loggers.LOUNGE.warning("Game server not defined");
            Bukkit.shutdown();
            return;
        }

        this.tmpGameServer = new TmpGameServer(gameDbServer);

        Server.registerListener(this.userManager, BasicLounge.getPlugin());

        // lounge maps
        for (DbLoungeMap map : Database.getLounges().getCachedMaps()) {
            LoungeMap loungeMap;
            try {
                loungeMap = new LoungeMap(map);
            } catch (WorldNotExistException e) {
                Loggers.LOUNGE.warning("Map '" + map.getName() + "' could not loaded, world '" +
                        e.getWorldName() + "' not exists");
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
            Loggers.LOUNGE.info("Loaded map " + loungeMap.getName() + " successfully");
        }

        if (this.loungeMaps.isEmpty()) {
            Loggers.LOUNGE.warning("No lounge-map found");
            Bukkit.shutdown();
        }

        this.loadRandomLoungeMap();

        if (Server.getTask() == null) {
            Loggers.LOUNGE.warning("Task is null");
            Bukkit.shutdown();
        }

        this.mapManager = new MapManager();

        this.kitManager = new KitManager();

        this.teamManager = new TeamManager();
        this.teamManager.loadTeamsIntoInventory();

        inventoryManager = new InventoryManager();

        this.scheduler = new Scheduler();

        this.scoreboardManager = new ScoreboardManager();

        this.waitingGameManager = new WaitingGameManager();

        this.statsManager = new StatsManager();
        this.discordManager = new DiscordManager();

        this.state = State.WAITING;

        Loggers.LOUNGE.info("Server loaded");
    }

    public final void onLoungeDisable() {
        this.discordManager.cleanup();
        this.tmpGameServer.getDatabase().setTwinServerName(null);
        ((DbLoungeServer) Server.getDatabase()).setTask(null);
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

            @Override
            public Map loadMap(DbMap dbMap, boolean loadWorld) {
                return new Map(dbMap, loadWorld);
            }
        };
    }

    @Override
    protected SpectatorManager initSpectatorManager() {
        return new SpectatorManager() {

            @Override
            public @Nullable Sideboard getSpectatorSideboard() {
                return LoungeServerManager.this.getLoungeScoreboardManager()
                        .getSpectatorSideboard();
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
        if (this.state == State.WAITING || this.state == State.PREPARING) {
            return;
        }

        this.state = State.PREPARING;
        Loggers.LOUNGE.info("Preparing lounge...");
        this.scheduler.resetGameCountdown();
        this.loadRandomLoungeMap();
        this.teamManager.resetTeams();
        Loggers.LOUNGE.info("Prepared lounge");
        this.state = State.WAITING;
    }

    public void startGame() {
        this.setState(State.PRE_GAME);
        Server.getChat().broadcastJoinQuit(false);

        Server.runTaskLoopAsynchrony((user) -> ((LoungeUser) user).switchToGameServer(),
                Server.getGameUsers(),
                BasicLounge.getPlugin());

        Server.runTaskLaterSynchrony(() -> {
            Server.getChat().broadcastJoinQuit(true);
            this.prepareLounge();
        }, 5 * 20, BasicLounge.getPlugin());
    }

    public Location getSpawn() {
        return this.currentMap.getSpawn();
    }

    @Deprecated
    public void broadcastLoungeMessage(String msg) {
        Server.broadcastTDMessage(Plugin.LOUNGE, msg);
    }


    public void broadcastLoungeMessage(Component msg) {
        Server.broadcastMessage(Plugin.LOUNGE, msg);
    }


    public void broadcastCountdownCancelledMessage() {
        if (this.state.equals(State.STARTING)) {
            this.broadcastLoungeMessage(ChatColor.WARNING + "Countdown canceled");
        }
    }

    public void loadRandomLoungeMap() {
        Server.runTaskSynchrony(() -> {
            int index = (int) (Math.random() * this.loungeMaps.size());
            this.currentMap = this.loungeMaps.get(index);
            this.currentMap.getWorld().loadChunk(this.currentMap.getSpawn().getChunk());
            this.statsManager.updateGlobalDisplays();
            Loggers.LOUNGE.info("Loaded map " + this.currentMap.getName());
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

    public State getState() {
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
        switch (state) {
            case PREPARING, PRE_GAME -> Server.setStatus(Status.Server.PRE_GAME);
            case IN_GAME -> Server.setStatus(Status.Server.IN_GAME);
            case POST_GAME -> Server.setStatus(Status.Server.POST_GAME);
            case WAITING, STARTING -> Server.setStatus(Status.Server.ONLINE);
        }
    }

    public void resetGameCountdown() {
        this.scheduler.resetGameCountdown();
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

    public ScoreboardManager getLoungeScoreboardManager() {
        return this.scoreboardManager;
    }

    public WaitingGameManager getWaitingGameManager() {
        return waitingGameManager;
    }

    public DiscordManager getDiscordManager() {
        return discordManager;
    }

    public enum State {
        PREPARING,
        WAITING,
        STARTING,
        PRE_GAME,
        IN_GAME,
        POST_GAME;
    }
}
