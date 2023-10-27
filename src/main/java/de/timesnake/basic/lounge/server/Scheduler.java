/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.game.util.game.Map;
import de.timesnake.basic.lounge.main.BasicLounge;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.library.basic.util.Loggers;
import de.timesnake.library.basic.util.Status;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;

public class Scheduler {

  private static final String STARTING_BAR_TEXT = "§fStarting in §c%s§fs";
  private static final String WAITING_BAR_TEXT = "§fWaiting for players/server ...";
  private static final String STARTING_TEXT = "§pGame starts in §v%s §ps";

  private final BossBar infoBar;
  private int gameCountdown = 60;
  private boolean isGameCountdownRunning = false;
  private BukkitTask gameCountdownTask;
  private boolean wait = false;

  public Scheduler() {
    this.infoBar = Server.createBossBar(WAITING_BAR_TEXT, BarColor.WHITE, BarStyle.SOLID);
  }

  public void checkCountdown() {
    if (this.wait) {
      LoungeServer.broadcastLoungeTDMessage("§pWaiting...");
      return;
    }

    int size = Server.getPreGameUsers().size();
    int autoStartSize = LoungeServer.getGame().getAutoStartPlayerNumber();
    TmpGameServer.State state = LoungeServer.getGameServer().getState();

    if (size >= autoStartSize) {
      if (LoungeServer.getGame().isEqualTimeSizeRequired() && size % LoungeServer.getGame().getTeams().size() != 0) {
        LoungeServer.broadcastLoungeTDMessage("§pWaiting for players to create equal teams.");
        this.resetGameCountdown();
      } else if (state.equals(TmpGameServer.State.READY)) {
        Server.getSpectatorUsers().forEach(user -> {
          ((LoungeUser) user).loadSpectatorInventory();
          user.showTDTitle("", "§wClick the helmet to join", Duration.ofSeconds(7));
        });
        this.startGameCountdown();
      } else {
        LoungeServer.getGameServer().start();
      }
    } else {
      this.resetGameCountdown();

      if (LoungeServer.getNotServiceUsers().size() >= autoStartSize) {
        Server.getSpectatorUsers().forEach(user -> {
          ((LoungeUser) user).loadSpectatorInventory();
          user.showTDTitle("", "§wClick the helmet to join the game", Duration.ofSeconds(7));
        });
      }
    }
  }

  public void startGameCountdown() {
    if (this.wait) {
      Loggers.LOUNGE.info("Waiting...");
      return;
    }

    if (!this.isGameCountdownRunning) {
      this.isGameCountdownRunning = true;
      int size = Server.getGameNotServiceUsers().size();
      if (size - LoungeServer.getGame().getAutoStartPlayerNumber() <= 0) {
        this.gameCountdown = 60;
      }
      LoungeServer.setState(LoungeServerManager.State.STARTING);

      this.infoBar.setColor(BarColor.GREEN);

      this.gameCountdownTask = Server.runTaskTimerAsynchrony(() -> {
        for (User user : Server.getGameUsers()) {
          this.infoBar.setTitle(STARTING_BAR_TEXT.formatted(this.gameCountdown));
          this.infoBar.setProgress(this.gameCountdown / ((double) 60));
          user.playNote(Instrument.STICKS, Note.natural(1, Note.Tone.A));
        }

        if (gameCountdown <= LoungeServer.JOINING_CLOSED) {
          if (!Server.getStatus().equals(Status.Server.IN_GAME)) {
            Server.setStatus(Status.Server.PRE_GAME);
          }
        }

        switch (gameCountdown) {
          case 60, 45, 30, 20, 15, 10 ->
              LoungeServer.broadcastLoungeTDMessage(STARTING_TEXT.formatted(this.gameCountdown));
          case 16 -> {
            Server.setStatus(Status.Server.PRE_GAME);
            if (LoungeServer.getGameServer().areMapsEnabled()) {
              Map map = LoungeServer.getMapManager().getVotedMap();
              LoungeServer.getGameServer().getDatabase().setMapName(map.getName());
              Server.getChannel().sendMessage(new ChannelServerMessage<>(Server.getName(),
                  MessageType.Server.GAME_MAP, map.getName()));
              LoungeServer.broadcastLoungeTDMessage("§wMap: §v" + map.getDisplayName());
              LoungeServer.getMapManager().resetMapVotes();
            } else {
              Server.getChannel().sendMessage(new ChannelServerMessage<>(Server.getName(),
                  MessageType.Server.GAME_WORLD, ""));
            }
          }
          case 14 -> {
            LoungeServer.getTeamManager().createTeams();
            Server.runTaskAsynchrony(() -> LoungeServer.getKitManager().loadUserKits(),
                BasicLounge.getPlugin());
          }
          case 11 -> {
            Server.getChannel().sendMessage(new ChannelServerMessage<>(Server.getName(),
                MessageType.Server.GAME_PLAYERS, Server.getGameUsers().size()));
            Loggers.LOUNGE.info("Estimated Players: " + Server.getGameUsers().size());
          }
          case 9 -> {
            if (LoungeServer.getGame().hasTexturePack()) {
              LoungeServer.broadcastTDTitle("", "§wLoading texture pack...", Duration.ofSeconds(3));
            }
          }
          case 8 -> {
            LoungeServer.startGame();
            gameCountdownTask.cancel();
          }
        }
        gameCountdown--;

      }, 0, 20, BasicLounge.getPlugin());
    }
  }

  public void resetGameCountdown() {
    if (this.gameCountdownTask != null) {
      this.gameCountdownTask.cancel();
      LoungeServer.broadcastCountdownCancelledMessage();
    }
    this.gameCountdown = 60;
    this.isGameCountdownRunning = false;
    this.infoBar.setTitle(WAITING_BAR_TEXT);
    this.infoBar.setProgress(1);
    this.infoBar.setColor(BarColor.WHITE);
  }

  public boolean isGameCountdownRunning() {
    return isGameCountdownRunning;
  }

  public int getGameCountdown() {
    return gameCountdown;
  }

  public void setGameCountdown(int gameCountdown) {
    this.gameCountdown = gameCountdown;
  }

  public BossBar getInfoBar() {
    return infoBar;
  }

  public boolean isWait() {
    return wait;
  }

  public void setWait(boolean wait) {
    this.wait = wait;

    if (wait) {
      if (this.isGameCountdownRunning) {
        this.resetGameCountdown();
      }
    } else {
      LoungeServer.getTimeManager().checkCountdown();
    }
  }
}
