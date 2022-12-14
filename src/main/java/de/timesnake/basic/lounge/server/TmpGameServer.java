/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.basic.lounge.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.main.BasicLounge;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.database.util.server.DbTmpGameServer;
import de.timesnake.library.basic.util.Status;
import org.bukkit.Bukkit;

import java.util.Collections;

public class TmpGameServer implements ChannelListener {

    private final DbTmpGameServer database;
    private final int port;
    private final String name;
    private final Integer maxPlayers;
    private final boolean mapsEnabled;
    private final boolean kitsEnabled;
    private final Integer teamAmount;
    private final Integer maxPlayersPerTeam;
    private final boolean mergeTeams;
    private State state;
    private boolean discord;

    public TmpGameServer(DbTmpGameServer database) {
        this.database = database;
        this.port = database.getPort();
        this.name = database.getName();
        if (database.getStatus().equals(Status.Server.ONLINE)) {
            this.state = State.READY;
        } else {
            this.state = State.OFFLINE;
        }

        this.maxPlayers = database.getMaxPlayers();
        this.kitsEnabled = database.areKitsEnabled();
        this.mapsEnabled = database.areMapsEnabled();
        this.maxPlayersPerTeam = database.getMaxPlayersPerTeam();
        Integer teamAmount = database.getTeamAmount();
        this.teamAmount = teamAmount != null ? teamAmount : LoungeServer.getGame().getTeams().size();
        this.mergeTeams = database.isTeamMerging();
        this.discord = database.isDiscordEnabled();

        Server.getChannel().addListener(this, () -> Collections.singleton(this.getName()));
    }

    public DbTmpGameServer getDatabase() {
        return this.database;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Integer getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public boolean areKitsEnabled() {
        return kitsEnabled;
    }

    public boolean areMapsEnabled() {
        return mapsEnabled;
    }

    public Integer getTeamAmount() {
        return teamAmount;
    }

    public Integer getMaxPlayersPerTeam() {
        return maxPlayersPerTeam;
    }

    public boolean isMergeTeams() {
        return mergeTeams;
    }

    public boolean isDiscord() {
        return discord;
    }

    public void setDiscord(boolean enabled) {
        this.discord = enabled;
        this.database.setDiscord(enabled);
    }

    public void start() {
        if (this.database.getTwinServerName() == null || !this.database.getTwinServerName().equals(Server.getName())) {
            Server.printWarning(Plugin.LOUNGE, "Twin server not found, shutdown");
            Bukkit.shutdown();
            return;
        }

        if (!this.state.equals(State.OFFLINE)) {
            return;
        }

        this.state = State.STARTING;
        Server.getChannel().sendMessage(new ChannelServerMessage<>(Server.getNetwork().getName(),
                MessageType.Server.COMMAND, "start server " + database.getName() + " " + this.maxPlayers));
        Server.printText(Plugin.LOUNGE, "Starting game server");
        this.checkIfStarted();
    }

    private void checkIfStarted() {
        Server.runTaskLaterAsynchrony(() -> {
            if (this.state.equals(State.OFFLINE)) {
                Server.printText(Plugin.LOUNGE, "Game server offline, try a restart");
                this.start();
            }
        }, 20 * 130, BasicLounge.getPlugin());
    }

    @ChannelHandler(type = {ListenerType.SERVER_STATUS, ListenerType.SERVER_STATE}, filtered = true)
    public void onServerMessage(ChannelServerMessage<?> msg) {
        if (msg.getMessageType().equals(MessageType.Server.STATE)) {
            if (msg.getValue().equals(ChannelServerMessage.State.READY)) {
                LoungeServer.setState(LoungeServerManager.State.WAITING);
                this.setState(State.READY);
                Server.printText(Plugin.LOUNGE, "Game-Server is ready");
                LoungeServer.getTimeManager().checkCountdown();
            }
        }

        if (msg.getMessageType().equals(MessageType.Server.STATUS)) {
            Status.Server status = this.getDatabase().getStatus();
            if (Status.Server.ONLINE.equals(status)) {
                LoungeServer.setState(LoungeServerManager.State.WAITING);
            } else if (Status.Server.OFFLINE.equals(status)) {
                this.setState(State.OFFLINE);
                LoungeServer.setState(LoungeServerManager.State.WAITING);
                LoungeServer.getTimeManager().resetGameCountdown();
            } else if (Status.Server.LAUNCHING.equals(status) || Status.Server.LOADING.equals(status)) {
                this.setState(State.STARTING);
                LoungeServer.setState(LoungeServerManager.State.WAITING);
                LoungeServer.getTimeManager().resetGameCountdown();
            } else if (Status.Server.PRE_GAME.equals(status)) {
                this.setState(State.PREGAME);
                LoungeServer.setState(LoungeServerManager.State.PRE_GAME);
            } else if (Status.Server.IN_GAME.equals(status)) {
                this.setState(State.INGAME);
                LoungeServer.setState(LoungeServerManager.State.IN_GAME);
                LoungeServer.prepareLounge();
            } else if (Status.Server.POST_GAME.equals(status)) {
                this.setState(State.POSTGAME);
                LoungeServer.setState(LoungeServerManager.State.POST_GAME);
            } else {
                LoungeServer.setState(LoungeServerManager.State.WAITING);
                Server.setStatus(Status.Server.SERVICE);
                LoungeServer.getTimeManager().resetGameCountdown();
            }
        }

    }

    public enum State {
        OFFLINE,
        STARTING,
        READY,
        PREGAME,
        INGAME,
        POSTGAME
    }

}
