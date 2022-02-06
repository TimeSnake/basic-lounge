package de.timesnake.basic.lounge.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.game.util.GameServer;
import de.timesnake.basic.game.util.Map;
import de.timesnake.basic.lounge.main.BasicLounge;
import de.timesnake.database.util.object.Status;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitTask;

public class Scheduler {

    private static final String STARTING_BAR_TEXT = "§fStarting in §c";
    private static final String WAITING_BAR_TEXT = "§fWaiting for players/server ...";

    private int gameCountdown = 60;
    private boolean isGameCountdownRunning = false;
    private BukkitTask gameCountdownTask;

    private final BossBar infoBar;

    public Scheduler() {
        this.infoBar = Server.createBossBar(WAITING_BAR_TEXT, BarColor.WHITE, BarStyle.SOLID);
    }

    public void startWaitTask() {
        this.infoBar.setTitle(WAITING_BAR_TEXT);
        this.infoBar.setProgress(1);
        this.infoBar.setColor(BarColor.WHITE);
    }

    public void startGameCountdown() {
        if (!this.isGameCountdownRunning) {
            this.isGameCountdownRunning = true;
            if (Server.getGameNotServiceUsers().size() - GameServer.getGame().getAutoStart() <= 0) {
                this.gameCountdown = 60;
            }
            LoungeServer.setState(LoungeServerManager.State.STARTING);

            this.infoBar.setColor(BarColor.GREEN);

            this.gameCountdownTask = Server.runTaskTimerAsynchrony(() -> {
                for (User user : Server.getGameUsers()) {
                    this.infoBar.setTitle(STARTING_BAR_TEXT + this.gameCountdown + "§fs");
                    this.infoBar.setProgress(this.gameCountdown / ((double) 60));
                    user.playNote(Instrument.STICKS, Note.natural(1, Note.Tone.A));
                }

                if (gameCountdown <= LoungeServer.JOINING_CLOSED) {
                    if (!Server.getStatus().equals(Status.Server.IN_GAME)) {
                        Server.setStatus(Status.Server.PRE_GAME);
                    }
                }

                switch (gameCountdown) {
                    case 60:
                    case 45:
                    case 30:
                    case 20:
                    case 15:
                    case 10:
                        LoungeServer.broadcastLoungeMessage(ChatColor.PUBLIC + "The Game starts in " + ChatColor.VALUE + gameCountdown + ChatColor.PUBLIC + " seconds");
                        break;
                    case 16:
                        Server.setStatus(Status.Server.PRE_GAME);
                        if (LoungeServer.getGameServer().areMapsEnabled()) {
                            Map map = LoungeServer.getMapManager().getVotedMap();
                            LoungeServer.getGameServer().getDatabase().setMapName(map.getName());
                            LoungeServer.broadcastLoungeMessage(ChatColor.WARNING + "Map: " + ChatColor.VALUE + map.getDisplayName());
                            LoungeServer.getMapManager().resetMapVotes();
                        }
                        break;
                    case 14:
                        LoungeServer.getTeamManager().createTeams();
                        Server.runTaskAsynchrony(() -> LoungeServer.getKitManager().loadUserKits(), BasicLounge.getPlugin());
                        break;
                    case 8:
                        LoungeServer.startGame();
                        gameCountdownTask.cancel();
                        break;
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
