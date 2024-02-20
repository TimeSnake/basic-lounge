/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.library.basic.util.Status;

import java.time.Duration;

public class StateManager {

  private State state;
  private boolean manualWaiting = false;

  public void onPlayerUpdate() {
    LoungeServer.getLoungeScoreboardManager().updateScoreboardPlayerNumber(Server.getPreGameUsers().size());
    this.checkStart();
    this.checkCancelStart();
  }

  public void onServerUpdate(TmpGameServer.State state) {
    switch (state) {
      case ONLINE, STARTING -> this.updateState(State.WAITING_SERVER);
      case READY -> this.checkStart();
      case OFFLINE -> this.checkCancelStart();
      case PREGAME -> this.updateState(State.PRE_GAME);
      case IN_GAME -> this.updateState(State.IN_GAME);
      case POST_GAME -> this.updateState(State.POST_GAME);
    }
  }

  public void checkStart() {
    if (!LoungeServer.getGameServer().getState().equals(TmpGameServer.State.READY)) {
      LoungeServer.getGameServer().start();
    }

    if (this.manualWaiting) {
      LoungeServer.broadcastLoungeTDMessage("§pWaiting...");
      return;
    }

    int playerNumber = Server.getPreGameUsers().size();

    if (playerNumber < LoungeServer.getGame().getAutoStartPlayerNumber()) {
      this.updateState(State.WAITING_PLAYERS);

      if (LoungeServer.getGameServer().getState().equals(TmpGameServer.State.READY)) {
        Server.getSpectatorUsers().forEach(user -> {
          ((LoungeUser) user).loadSpectatorInventory();
          user.showTDTitle("", "§wClick the helmet to join", Duration.ofSeconds(7));
        });
      }
      return;
    }

    if (!LoungeServer.getGameServer().getState().equals(TmpGameServer.State.READY)) {
      this.updateState(State.WAITING_SERVER);
      return;
    }

    if (LoungeServer.getGame().isEqualTimeSizeRequired() && playerNumber % LoungeServer.getGame().getTeams().size() != 0) {
      return;
    }

    LoungeServer.getTimeManager().startGameCountdown();
  }

  public void checkCancelStart() {
    int playerNumber = Server.getPreGameUsers().size();

    if (playerNumber < LoungeServer.getGame().getAutoStartPlayerNumber()) {
      LoungeServer.getTimeManager().resetGameCountdown();
    } else if (LoungeServer.getGame().isEqualTimeSizeRequired() && playerNumber % LoungeServer.getGame().getTeams().size() != 0) {
      LoungeServer.broadcastLoungeTDMessage("§pWaiting for players to create equal teams.");
      LoungeServer.getTimeManager().resetGameCountdown();
    } else if (!LoungeServer.getGameServer().getState().equals(TmpGameServer.State.READY)) {
      LoungeServer.getTimeManager().resetGameCountdown();
    }
  }

  public void updateState(State state) {
    this.state = state;
    switch (state) {
      case PREPARING, PRE_GAME -> Server.setStatus(Status.Server.PRE_GAME);
      case IN_GAME -> {
        Server.setStatus(Status.Server.IN_GAME);
        LoungeServer.prepareLounge();
      }
      case POST_GAME -> Server.setStatus(Status.Server.POST_GAME);
      case WAITING_SERVER, WAITING_PLAYERS, STARTING -> Server.setStatus(Status.Server.ONLINE);
    }
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

  public enum State {
    PREPARING,
    WAITING_PLAYERS,
    WAITING_SERVER,
    STARTING,
    PRE_GAME,
    IN_GAME,
    POST_GAME
  }
}
