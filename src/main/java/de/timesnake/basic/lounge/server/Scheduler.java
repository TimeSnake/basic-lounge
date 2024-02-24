/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.game.util.game.Map;
import de.timesnake.basic.lounge.main.BasicLounge;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
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

  public Scheduler() {
    this.infoBar = Server.createBossBar(WAITING_BAR_TEXT, BarColor.WHITE, BarStyle.SOLID);
  }

  public void startGameCountdown() {
    if (!this.isGameCountdownRunning) {
      this.isGameCountdownRunning = true;

      LoungeServer.getStateManager().updateState(StateManager.State.STARTING);

      this.infoBar.setColor(BarColor.GREEN);

      this.gameCountdownTask = Server.runTaskTimerAsynchrony(() -> {
        for (User user : Server.getGameUsers()) {
          this.infoBar.setTitle(STARTING_BAR_TEXT.formatted(this.gameCountdown));
          this.infoBar.setProgress(this.gameCountdown / ((double) 60));
          user.playNote(Instrument.STICKS, Note.natural(1, Note.Tone.A));
        }

        if (gameCountdown == LoungeServer.JOINING_CLOSED) {
          LoungeServer.getStateManager().updateState(StateManager.State.PRE_GAME);
        }

        if (gameCountdown == LoungeServer.MAP_SELECTION) {
          if (LoungeServer.getGameServer().areMapsEnabled()) {
            Map map = LoungeServer.getMapManager().getVotedMap();
            LoungeServer.getGameServer().getDatabase().setMapName(map.getName());
            LoungeServer.broadcastLoungeTDMessage("§wMap: §v" + map.getDisplayName());
            LoungeServer.getMapManager().resetMapVotes();
          } else {
            Server.getChannel().sendMessage(new ChannelServerMessage<>(Server.getName(),
                MessageType.Server.GAME_WORLD, ""));
          }
        }

        if (gameCountdown == LoungeServer.TEAM_CREATION) {
          LoungeServer.getTeamManager().createTeams();
        }

        if (gameCountdown == LoungeServer.KIT_LOADING) {
          Server.runTaskAsynchrony(() -> LoungeServer.getKitManager().loadUserKits(), BasicLounge.getPlugin());
        }

        switch (gameCountdown) {
          case 60, 45, 30, 20, 15, 10 ->
              LoungeServer.broadcastLoungeTDMessage(STARTING_TEXT.formatted(this.gameCountdown));
          case 11 -> {
            Server.getChannel().sendMessage(new ChannelServerMessage<>(Server.getName(),
                MessageType.Server.GAME_PLAYERS, Server.getGameUsers().size()));
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
}
