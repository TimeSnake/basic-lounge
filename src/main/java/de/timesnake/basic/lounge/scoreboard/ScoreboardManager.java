package de.timesnake.basic.lounge.scoreboard;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.permission.Group;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.bukkit.util.user.scoreboard.Tablist;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroupType;
import de.timesnake.basic.bukkit.util.user.scoreboard.TeamTablist;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.extension.util.cmd.ChatDivider;

import java.util.LinkedList;
import java.util.List;

public class ScoreboardManager {

    private final TeamTablist tablist;
    private final GameTeam gameTeam;
    private final GameTeam spectatorTeam;

    private final Sideboard sideboard;
    private final Sideboard spectatorSideboard;

    public ScoreboardManager() {

        // tablist

        this.gameTeam = new GameTeam("0", "game", "", ChatColor.WHITE, ChatColor.WHITE);
        this.spectatorTeam = new GameTeam("0", "spec", "", ChatColor.WHITE, ChatColor.GRAY);

        LinkedList<TablistGroupType> types = new LinkedList<>();
        types.add(Group.getTablistType());

        this.tablist = Server.getScoreboardManager().registerNewTeamTablist("lounge_side", Tablist.Type.DUMMY,
                TeamTablist.ColorType.WHITE, List.of(this.gameTeam), this.gameTeam.getTeamType(), types,
                this.spectatorTeam, types,
                (e, tablist) -> {
                    User user = e.getUser();
                    String task = user.getTask();

                    if (task == null) {
                        ((TeamTablist) tablist).addRemainEntry(e.getUser());
                    } else if (task.equalsIgnoreCase(LoungeServer.getGame().getName())) {
                        if (e.getUser().getStatus().equals(Status.User.PRE_GAME) || e.getUser().getStatus().equals(Status.User.IN_GAME)) {
                            tablist.addEntry(e.getUser());
                        } else {
                            ((TeamTablist) tablist).addRemainEntry(e.getUser());
                        }
                    }
                }, (e, tablist) -> tablist.removeEntry(e.getUser()));

        if (LoungeServer.getGameServer().areKitsEnabled()) {
            this.tablist.setHeader("§6" + LoungeServer.getGame().getDisplayName() + " §bKits");
        } else {
            this.tablist.setHeader("§6" + LoungeServer.getGame().getDisplayName());
        }

        this.tablist.setFooter("§7Server: " + Server.getName() + "\n§cSupport: /ticket or \n" + Server.SUPPORT_EMAIL);

        Server.getScoreboardManager().setActiveTablist(this.tablist);
        Server.printText(Plugin.LOUNGE, "Lounge tablist loaded");


        // sideboard

        this.sideboard = Server.getScoreboardManager().registerNewSideboard("lounge",
                "§6§l" + LoungeServer.getGame().getDisplayName());

        this.spectatorSideboard = Server.getScoreboardManager().registerNewSideboard("lounge_spectator",
                "§6§l" + LoungeServer.getGame().getDisplayName());

        if (LoungeServer.getGameServer().areKitsEnabled()) {
            this.sideboard.setScore(4, "§lKit");
            // user kit
            if (LoungeServer.getGameServer().getTeamAmount() > 1) {
                this.sideboard.setScore(11, "§9§lPlayers");
                this.updateScoreboardPlayerNumber(10, 0);
                // more needed display
                this.sideboard.setScore(8, "§r§r" + ChatDivider.SECTION);
                this.sideboard.setScore(7, "§9§lTeam");
                //user team
                this.sideboard.setScore(5, "§r" + ChatDivider.SECTION);
                // kit
            } else {
                this.sideboard.setScore(8, "§9§lPlayers");
                this.updateScoreboardPlayerNumber(7, 0);
                // more needed display
                this.sideboard.setScore(5, "§r§r" + ChatDivider.SECTION);
                // kit
            }
        } else if (LoungeServer.getGameServer().getTeamAmount() > 1) {
            this.sideboard.setScore(9, "§9§lPlayers");
            this.updateScoreboardPlayerNumber(7, 0);
            // more needed display
            this.sideboard.setScore(5, "§r§r" + ChatDivider.SECTION);
            this.sideboard.setScore(4, "§9§lTeam");
            // user team
        } else {
            this.sideboard.setScore(5, "§l§9Players");
            this.updateScoreboardPlayerNumber(4, 0);
            // more needed display
        }
        this.sideboard.setScore(2, ChatDivider.SECTION);
        this.sideboard.setScore(1, "§7§lServer");
        this.sideboard.setScore(0, "§7" + Server.getName());
        Server.printText(Plugin.LOUNGE, "Scoreboard loaded");

        this.spectatorSideboard.setScore(5, "§l§9Players");
        // player amount
        this.spectatorSideboard.setScore(2, ChatDivider.SECTION);
        this.spectatorSideboard.setScore(1, "§7§lServer");
        this.spectatorSideboard.setScore(0, "§7" + Server.getName());
        Server.printText(Plugin.LOUNGE, "Spectator scoreboard loaded");
    }

    public Sideboard getSideboard() {
        return this.sideboard;
    }

    public Sideboard getSpectatorSideboard() {
        return this.spectatorSideboard;
    }

    public void updateScoreboardPlayerNumber(int line, int online) {
        int needMore = LoungeServer.getGame().getAutoStartPlayerNumber() - online;
        int max = Server.getMaxPlayers();
        this.sideboard.setScore(line, online + "§7/§f" + max);
        this.spectatorSideboard.setScore(4, online + "§7/§f" + max);
        if (needMore > 0) {
            if (needMore == 1) {
                this.sideboard.setScore(line - 1, "§7" + needMore + " more is needed");
                this.spectatorSideboard.setScore(3, "§7" + needMore + " more is needed");
            } else {
                this.sideboard.setScore(line - 1, "§7" + needMore + " more are needed");
                this.spectatorSideboard.setScore(3, "§7" + needMore + " more are needed");
            }
        } else {
            this.sideboard.setScore(line - 1, "");
            this.spectatorSideboard.setScore(3, "");
        }
    }

    public void updateScoreboardPlayerNumber(int online) {
        if (LoungeServer.getGameServer().getTeamAmount() > 1) {
            if (LoungeServer.getGameServer().areKitsEnabled()) {
                this.updateScoreboardPlayerNumber(10, online);
            } else {
                this.updateScoreboardPlayerNumber(7, online);
            }
        } else {
            if (LoungeServer.getGameServer().areKitsEnabled()) {
                this.updateScoreboardPlayerNumber(7, online);
            } else {
                this.updateScoreboardPlayerNumber(4, online);
            }
        }
    }

    public void updateScoreboardPlayerNumber() {
        this.updateScoreboardPlayerNumber(Server.getPreGameUsers().size());
    }

    public TeamTablist getTablist() {
        return tablist;
    }

    public GameTeam getGameTeam() {
        return gameTeam;
    }

    public GameTeam getSpectatorTeam() {
        return spectatorTeam;
    }
}
