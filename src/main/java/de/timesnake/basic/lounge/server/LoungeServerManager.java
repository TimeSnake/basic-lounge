package de.timesnake.basic.lounge.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.exceptions.UnsupportedGroupRankException;
import de.timesnake.basic.bukkit.util.exceptions.WorldNotExistException;
import de.timesnake.basic.game.util.Game;
import de.timesnake.basic.game.util.GameServerManager;
import de.timesnake.basic.game.util.Kit;
import de.timesnake.basic.game.util.Map;
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
import de.timesnake.channel.util.message.ChannelDiscordMessage;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.game.*;
import de.timesnake.database.util.server.DbLoungeServer;
import de.timesnake.database.util.server.DbTempGameServer;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.waitinggames.WaitingGameManager;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class LoungeServerManager extends GameServerManager implements Listener, ChannelListener {

    public static LoungeServerManager getInstance() {
        return (LoungeServerManager) ServerManager.getInstance();
    }

    protected final List<LoungeMap> loungeMaps = new ArrayList<>();
    private final UserManager userManager = new UserManager();
    protected LoungeMap currentMap;
    private TempGameServer tempGameServer;
    private InventoryManager inventoryManager;
    private de.timesnake.basic.lounge.scoreboard.ScoreboardManager scoreboardManager;
    private MapManager mapManager;
    private KitManager kitManager;
    private TeamManager teamManager;
    private Scheduler scheduler;
    private WaitingGameManager waitingGameManager;
    private StatsManager statsManager;
    private State state;

    public void onLoungeEnable() {
        super.onGameEnable();

        Server.registerListener(this, BasicLounge.getPlugin());

        DbTempGameServer gameDbServer = ((DbLoungeServer) Server.getDatabase()).getTwinServer();

        if (gameDbServer == null || !gameDbServer.exists()) {
            Server.printError(Plugin.LOUNGE, "Game server not defined");
            Bukkit.shutdown();
        }

        this.tempGameServer = new TempGameServer(gameDbServer);

        Server.registerListener(this.userManager, BasicLounge.getPlugin());

        // lounge maps
        for (DbLoungeMap map : Database.getLounges().getCachedMaps()) {
            LoungeMap loungeMap;
            try {
                loungeMap = new LoungeMap(map);
            } catch (WorldNotExistException e) {
                Server.printWarning(Plugin.LOUNGE, "Map " + map.getName() + " could not loaded, world not exists",
                        "lounge", "Map");
                continue;
            }
            loungeMap.getWorld().setExceptService(true);
            loungeMap.getWorld().allowFoodChange(false);
            loungeMap.getWorld().allowEntityExplode(false);
            loungeMap.getWorld().allowDropPickItem(false);
            loungeMap.getWorld().allowBlockBreak(false);
            loungeMap.getWorld().allowBlockPlace(false);
            loungeMap.getWorld().allowBlockBurnUp(false);
            loungeMap.getWorld().allowFireSpread(false);
            loungeMap.getWorld().allowPlaceInBlock(false);
            loungeMap.getWorld().setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            this.loungeMaps.add(loungeMap);
            Server.printText(Plugin.LOUNGE, "Loaded map " + loungeMap.getName() + " successfully", "Map");
        }

        if (this.loungeMaps.isEmpty()) {
            Server.printError(Plugin.LOUNGE, "No lounge-map found", "Map");
            Bukkit.shutdown();
        }

        this.loadRandomLoungeMap();

        if (Server.getTask() == null) {
            Server.printError(Plugin.LOUNGE, "Task is null");
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

        this.state = State.WAITING;

        Server.printText(Plugin.LOUNGE, "Server loaded");
    }

    public final void onLoungeDisable() {
        Server.getChannel().sendMessage(new ChannelDiscordMessage<>(LoungeServer.getGameServer().getName(),
                MessageType.Discord.DESTROY_TEAMS, List.of()));
        this.tempGameServer.getDatabase().setTwinServerPort(null);
        ((DbLoungeServer) Server.getDatabase()).setTask(null);
    }

    @Override
    public LoungeUser loadUser(Player player) {
        return new LoungeUser(player);
    }

    @Override
    protected Game loadGame(DbGame dbGame, boolean loadWorlds) {
        return new Game(dbGame, false) {
            @Override
            public LoungeTeam loadTeam(DbTeam team) throws UnsupportedGroupRankException {
                return new LoungeTeam(team);
            }

            @Override
            public Map loadMap(DbMap dbMap, boolean loadWorld) {
                return new Map(dbMap, loadWorld);
            }

            @Override
            public Kit loadKit(DbKit kit) {
                return new Kit(kit);
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
        Server.printText(Plugin.LOUNGE, "Preparing lounge...");
        this.scheduler.resetGameCountdown();
        this.loadRandomLoungeMap();
        this.teamManager.resetTeams();
        Server.printText(Plugin.LOUNGE, "Prepared lounge");
        this.state = State.WAITING;
    }

    public void startGame() {
        this.setState(State.PRE_GAME);
        Server.getChannel().sendMessage(new ChannelServerMessage<>(this.getPort(), MessageType.Server.CUSTOM,
                "estimatedPlayers:" + this.getGameUsers().size()));
        Server.printText(Plugin.LOUNGE, "Estimated Players: " + this.getGameUsers().size());
        Server.getChat().broadcastJoinQuit(false);

        Server.runTaskLoopAsynchrony((user) -> ((LoungeUser) user).switchToGameServer(), Server.getGameUsers(),
                BasicLounge.getPlugin());

        Server.runTaskLaterSynchrony(() -> {
            Server.getChat().broadcastJoinQuit(true);
            this.prepareLounge();
        }, 5 * 20, BasicLounge.getPlugin());
    }

    public Location getSpawn() {
        return this.currentMap.getSpawn();
    }

    public void broadcastLoungeMessage(String msg) {
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
            Server.printText(Plugin.LOUNGE, "Loaded map " + this.currentMap.getName(), "Map");
        }, BasicLounge.getPlugin());

    }

    public TempGameServer getGameServer() {
        return tempGameServer;
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

    public enum State {
        PREPARING,
        WAITING,
        STARTING,
        PRE_GAME,
        IN_GAME,
        POST_GAME;
    }
}
