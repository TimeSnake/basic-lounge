/*
 * timesnake.basic-lounge.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.basic.lounge.scoreboard;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.chat.DisplayGroup;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.bukkit.util.user.scoreboard.TeamTablist;
import de.timesnake.basic.bukkit.util.user.scoreboard.TeamTablistBuilder;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.library.basic.util.Status;

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

        this.tablist = Server.getScoreboardManager().registerTeamTablist(
                new TeamTablistBuilder("lounge_side")
                        .colorType(TeamTablist.ColorType.WHITE)
                        .teams(List.of(this.gameTeam))
                        .teamType(this.gameTeam.getTeamType())
                        .groupTypes(DisplayGroup.MAIN_TABLIST_GROUPS)
                        .remainTeam(this.spectatorTeam)
                        .userJoin((e, tablist) -> {
                            if (e.getUser().getTask() != null
                                    && e.getUser().getTask().equalsIgnoreCase(LoungeServer.getGame().getName())
                                    && (e.getUser().getStatus().equals(Status.User.PRE_GAME)
                                    || e.getUser().getStatus().equals(Status.User.IN_GAME))) {
                                tablist.addEntry(e.getUser());
                            } else {
                                ((TeamTablist) tablist).addRemainEntry(e.getUser());
                            }
                        })
                        .userQuit((e, tablist) -> tablist.removeEntry(e.getUser())));

        if (LoungeServer.getGameServer().areKitsEnabled()) {
            this.tablist.setHeader("§6" + LoungeServer.getGame().getDisplayName() + " §bKits");
        } else {
            this.tablist.setHeader("§6" + LoungeServer.getGame().getDisplayName());
        }

        this.tablist.setFooter("§7Server: " + Server.getName() + "\n§cSupport: /ticket or \n" + Server.SUPPORT_EMAIL);

        Server.getScoreboardManager().setActiveTablist(this.tablist);
        Server.printText(Plugin.LOUNGE, "Lounge tablist loaded");


        // sideboard

        this.sideboard = Server.getScoreboardManager().registerSideboard("lounge",
                "§6§l" + LoungeServer.getGame().getDisplayName());

        this.spectatorSideboard = Server.getScoreboardManager().registerSideboard("lounge_spectator",
                "§6§l" + LoungeServer.getGame().getDisplayName());

        if (LoungeServer.getGameServer().areKitsEnabled()) {
            this.sideboard.setScore(4, "§lKit");
            // user kit
            if (LoungeServer.getGameServer().getTeamAmount() > 1) {
                this.sideboard.setScore(11, "§9§lPlayers");
                this.updateScoreboardPlayerNumber(10, 0);
                // more needed display
                this.sideboard.setScore(8, "§r§r" + Sideboard.SPACER);
                this.sideboard.setScore(7, "§9§lTeam");
                //user team
                this.sideboard.setScore(5, "§r" + Sideboard.SPACER);
                // kit
            } else {
                this.sideboard.setScore(8, "§9§lPlayers");
                this.updateScoreboardPlayerNumber(7, 0);
                // more needed display
                this.sideboard.setScore(5, "§r§r" + Sideboard.SPACER);
                // kit
            }
        } else if (LoungeServer.getGameServer().getTeamAmount() > 1) {
            this.sideboard.setScore(9, "§9§lPlayers");
            this.updateScoreboardPlayerNumber(7, 0);
            // more needed display
            this.sideboard.setScore(5, "§r§r" + Sideboard.SPACER);
            this.sideboard.setScore(4, "§9§lTeam");
            // user team
        } else {
            this.sideboard.setScore(5, "§l§9Players");
            this.updateScoreboardPlayerNumber(4, 0);
            // more needed display
        }
        this.sideboard.setScore(2, Sideboard.SPACER);
        this.sideboard.setScore(1, "§7§lServer");
        this.sideboard.setScore(0, "§7" + Server.getName());
        Server.printText(Plugin.LOUNGE, "Scoreboard loaded");

        this.spectatorSideboard.setScore(5, "§l§9Players");
        // player amount
        this.spectatorSideboard.setScore(2, Sideboard.SPACER);
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
