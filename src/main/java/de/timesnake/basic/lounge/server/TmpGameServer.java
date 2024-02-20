/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.lounge.main.BasicLounge;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.database.util.server.DbTmpGameServer;
import de.timesnake.library.basic.util.Loggers;
import de.timesnake.library.basic.util.Status;
import org.bukkit.Bukkit;

import java.util.Set;

public class TmpGameServer implements ChannelListener {

  private final DbTmpGameServer database;
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
    this.teamAmount = teamAmount != null ? teamAmount : 0;
    this.mergeTeams = database.isTeamMerging();
    this.discord = database.isDiscordEnabled();

    Server.getChannel().addListener(this, Set.of(this.getName()));
  }

  public DbTmpGameServer getDatabase() {
    return this.database;
  }

  public State getState() {
    return state;
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
      Loggers.LOUNGE.warning("Twin server not found, shutdown");
      Bukkit.shutdown();
      return;
    }

    if (!this.state.equals(State.OFFLINE)) {
      return;
    }

    this.state = State.STARTING;
    Server.getChannel().sendMessage(new ChannelServerMessage<>(Server.getNetwork().getProxyName(),
        MessageType.Server.COMMAND, "start server " + database.getName() + " " + this.maxPlayers));
    Loggers.LOUNGE.info("Starting game server");
    this.checkIfStarted();
  }

  private void checkIfStarted() {
    Server.runTaskLaterAsynchrony(() -> {
      if (this.state.equals(State.OFFLINE)) {
        Loggers.LOUNGE.info("Game server offline, trying a restart");
        this.start();
      }
    }, 20 * 130, BasicLounge.getPlugin());
  }

  @ChannelHandler(type = ListenerType.SERVER_STATE, filtered = true)
  public void onServerMessage(ChannelServerMessage<ChannelServerMessage.State> msg) {
    if (ChannelServerMessage.State.READY.equals(msg.getValue())) {
      this.state = State.READY;
      Loggers.LOUNGE.info("Game-Server is ready");
      LoungeServer.getStateManager().onServerUpdate(this.state);
    }
  }

  @ChannelHandler(type = ListenerType.SERVER_STATUS, filtered = true)
  public void onStatusMessage(ChannelServerMessage<Status.Server> msg) {
    Status.Server status = msg.getValue();

    if (Status.Server.ONLINE.equals(status)) {
      this.state = State.ONLINE;
    } else if (Status.Server.OFFLINE.equals(status)) {
      this.state = State.OFFLINE;
    } else if (Status.Server.LAUNCHING.equals(status) || Status.Server.LOADING.equals(status)) {
      this.state = State.STARTING;
    } else if (Status.Server.PRE_GAME.equals(status)) {
      this.state = State.PREGAME;
    } else if (Status.Server.IN_GAME.equals(status)) {
      this.state = State.IN_GAME;
    } else if (Status.Server.POST_GAME.equals(status)) {
      this.state = State.POST_GAME;
    }

    LoungeServer.getStateManager().onServerUpdate(this.state);
  }

  public enum State {
    OFFLINE,
    STARTING,
    ONLINE,
    READY,
    PREGAME,
    IN_GAME,
    POST_GAME
  }

}
