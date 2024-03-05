/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.library.basic.util.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

public class StateManager {

  private final Logger logger = LogManager.getLogger("lounge.state");

  private State state;
  private boolean gameServerReady = false;
  private boolean manualWaiting = false;

  public void onPlayerUpdate() {
    LoungeServer.getLoungeScoreboardManager().updateScoreboardPlayerNumber(Server.getPreGameUsers().size());
    this.checkStart();
    this.checkCancelStart();
  }

  public void onServerUpdate(TmpGameServer.State state) {
    switch (state) {
      case STARTING -> this.updateState(State.WAITING_SERVER);
      case READY -> {
        this.gameServerReady = true;
        this.checkStart();
      }
      case OFFLINE -> {
        this.gameServerReady = false;
        this.checkCancelStart();
      }
      case PREGAME -> this.updateState(State.PRE_GAME);
      case IN_GAME -> {
        this.gameServerReady = false;
        this.updateState(State.IN_GAME);
      }
      case POST_GAME -> this.updateState(State.POST_GAME);
    }
  }

  public void checkStart() {
    if (!this.gameServerReady) {
      LoungeServer.getGameServer().start();
    }

    if (this.manualWaiting) {
      LoungeServer.broadcastLoungeTDMessage("§pWaiting...");
      return;
    }

    if (LoungeServer.getTimeManager().isGameCountdownRunning()) {
      return;
    }

    int playerNumber = Server.getPreGameUsers().size();

    if (playerNumber < LoungeServer.getGame().getAutoStartPlayerNumber()) {
      this.updateState(State.WAITING_PLAYERS);

      Server.getSpectatorUsers().forEach(user -> {
        ((LoungeUser) user).loadSpectatorInventory();
        user.showTDTitle("", "§wClick the helmet to join", Duration.ofSeconds(7));
      });
      this.logger.info("Enabled spectator join");
      return;
    }

    if (!this.gameServerReady) {
      this.updateState(State.WAITING_SERVER);
      return;
    }

    if (LoungeServer.getGame().isEqualTimeSizeRequired() && playerNumber % LoungeServer.getGame().getTeams().size() != 0) {
      return;
    }

    Server.getSpectatorUsers().forEach(user -> {
      ((LoungeUser) user).loadSpectatorInventory();
      user.showTDTitle("", "§wClick the helmet to join", Duration.ofSeconds(7));
    });
    this.logger.info("Enabled spectator join");

    LoungeServer.getTimeManager().startGameCountdown();
  }

  public void checkCancelStart() {
    int playerNumber = Server.getPreGameUsers().size();

    if (playerNumber < LoungeServer.getGame().getAutoStartPlayerNumber()) {
      LoungeServer.getTimeManager().resetGameCountdown();
    } else if (LoungeServer.getGame().isEqualTimeSizeRequired() && playerNumber % LoungeServer.getGame().getTeams().size() != 0) {
      LoungeServer.broadcastLoungeTDMessage("§pWaiting for players to create equal teams.");
      LoungeServer.getTimeManager().resetGameCountdown();
    } else if (!this.gameServerReady) {
      LoungeServer.getTimeManager().resetGameCountdown();
    }
  }

  public void updateState(State state) {
    this.state = state;
    switch (state) {
      case PRE_GAME -> Server.setStatus(Status.Server.PRE_GAME);
      case IN_GAME -> {
        Server.setStatus(Status.Server.IN_GAME);
        LoungeServer.prepareLounge();
      }
      case POST_GAME -> Server.setStatus(Status.Server.POST_GAME);
      case WAITING_SERVER, WAITING_PLAYERS, STARTING -> Server.setStatus(Status.Server.ONLINE);
    }

    this.logger.info("Updated state to {}", state.name().toLowerCase());
  }

  public void setManualWaiting(boolean wait) {
    this.manualWaiting = wait;

    if (wait) {
      LoungeServer.getTimeManager().resetGameCountdown();
    } else {
      this.checkStart();
    }
  }

  public boolean isManualWaiting() {
    return manualWaiting;
  }

  public State getState() {
    return state;
  }

  public boolean isState(State state) {
    return this.state.equals(state);
  }

  public boolean isGameServerReady() {
    return gameServerReady;
  }

  public enum State {
    WAITING_PLAYERS,
    WAITING_SERVER,
    STARTING,
    PRE_GAME,
    IN_GAME,
    POST_GAME
  }
}
