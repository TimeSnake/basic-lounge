package de.timesnake.basic.lounge.team;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.game.util.GameServer;
import de.timesnake.basic.game.util.Team;
import de.timesnake.basic.game.util.TeamUser;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.library.extension.util.chat.Chat;
import net.kyori.adventure.text.Component;

import java.util.*;

public class TeamCreator {

    private final List<LoungeUser> usersWithRandomTeam;
    private int sumMaxTeamPlayer = 0;
    private final int playerAmount;

    private final LinkedList<Team> teams;

    public TeamCreator() {
        this.usersWithRandomTeam = new ArrayList<>();
        this.playerAmount = Server.getPreGameUsers().size();
        this.teams = new LinkedList<>(LoungeServer.getGame().getTeamsSortedByRank(LoungeServer.getGameServer().getTeamAmount()).values());
        this.teams.sort(Comparator.comparingInt(Team::getRank));
    }

    public void createTeams() {

        Server.printText(Plugin.LOUNGE, "Team-selection closed", "Team");

        if (LoungeServer.getGameServer().getTeamAmount() == 0) {
            return;
        }

        Server.printText(Plugin.LOUNGE, "Team-creation:", "Team");

        new Thread(() -> {
            if (LoungeServer.getGameServer().getTeamAmount() == 1) {
                this.createSingleTeam();
            } else {
                this.createMultipleTeams();
            }
        }).start();

        for (Team team : LoungeServer.getGame().getTeams()) {
            ((LoungeTeam) team).clearUserSelected();
        }
    }

    private synchronized void createMultipleTeams() {
        this.calcTeamSizes();
        this.divideUsersInTeams();
    }

    private void calcTeamSizes() {
        Integer maxPerTeam = LoungeServer.getGameServer().getMaxPlayersPerTeam();
        if (maxPerTeam != null) {
            for (Team team : this.teams) {
                ((LoungeTeam) team).setMaxPlayers(maxPerTeam);
            }
            if (LoungeServer.getGameServer().isMergeTeams()) {
                int smallestTeamAmount = (int) Math.ceil(this.playerAmount / ((double) maxPerTeam));

                // prevent merge in one team
                if (smallestTeamAmount <= 1) {
                    smallestTeamAmount = 2;
                }

                // remove not used teams
                while (smallestTeamAmount < this.teams.size()) {
                    this.teams.removeLast();
                }

                // adjust team size to player size (decrease size team size, begin last)
                int teamPlayerSum = this.teams.size() * maxPerTeam;
                LinkedList<Team> teamsSortedByPlayerSelectedAmount = new LinkedList<>(this.teams);
                teamsSortedByPlayerSelectedAmount.sort((team, team1) -> ((LoungeTeam) team1).getUsersSelected().size() - ((LoungeTeam) team).getUsersSelected().size());

                while (teamPlayerSum > playerAmount) {
                    Iterator<Team> teamIterator = teamsSortedByPlayerSelectedAmount.descendingIterator();
                    while (teamIterator.hasNext() && teamPlayerSum > playerAmount) {
                        LoungeTeam team = (LoungeTeam) teamIterator.next();
                        team.setMaxPlayers(team.getMaxPlayers() - 1);
                        teamPlayerSum--;
                    }
                }
            }
        } else {
            // calculate max players per team
            double ratioSum = 0;
            for (Team team : this.teams) {
                ratioSum += team.getRatio();
            }
            for (Team team : this.teams) {
                ((LoungeTeam) team).setMaxPlayers((int) (playerAmount * team.getRatio() / ratioSum));
                sumMaxTeamPlayer += ((LoungeTeam) team).getMaxPlayers();
            }

            // fix round fails
            while (sumMaxTeamPlayer < playerAmount) {
                for (Team team : this.teams) {
                    if (sumMaxTeamPlayer >= playerAmount) {
                        break;
                    } else {
                        ((LoungeTeam) team).setMaxPlayers(((LoungeTeam) team).getMaxPlayers() + 1);
                        sumMaxTeamPlayer++;
                    }
                }
            }
        }

        for (Team team : this.teams) {
            Server.printText(Plugin.LOUNGE, team.getName() + ": " + ((LoungeTeam) team).getMaxPlayers());
        }
    }

    private void divideUsersInTeams() {
        //selected teams
        List<User> users = new ArrayList<>(Server.getPreGameUsers());
        Collections.shuffle(users);

        for (User user : users) {
            LoungeTeam team = ((LoungeUser) user).getSelectedTeam();
            if (team == null) {
                usersWithRandomTeam.add(((LoungeUser) user));
            } else if (team.getUsers().size() < team.getMaxPlayers()) {
                user.sendPluginMessage(Plugin.LOUNGE, ChatColor.PERSONAL + "You joined team " + ChatColor.VALUE + team.getChatColor() + team.getDisplayName());
                ((LoungeUser) user).setTeam(team);
                ((LoungeUser) user).setSelectedTeam(team);
                Server.printText(Plugin.LOUNGE, "User " + user.getPlayer().getName() + " joined team " + team.getName(), "Team");
            } else {
                usersWithRandomTeam.add(((LoungeUser) user));
            }
        }

        Collections.shuffle(this.usersWithRandomTeam);
        Collections.shuffle(this.teams);

        //random
        for (LoungeUser user : usersWithRandomTeam) {
            for (Team team : this.teams) {
                if (team.getUsers().size() < ((LoungeTeam) team).getMaxPlayers()) {
                    user.sendPluginMessage(Plugin.LOUNGE, ChatColor.PERSONAL + "You joined team " + ChatColor.VALUE + team.getChatColor() + team.getDisplayName());
                    user.setSelectedTeam(((LoungeTeam) team));
                    user.setTeam(team);
                    Server.printText(Plugin.LOUNGE, "User " + user.getPlayer().getName() + " joined team " + user.getTeam().getName(), "Team");
                    break;
                }

            }

            if (user.getTeam() == null) {
                user.sendPluginMessage(Plugin.LOUNGE, ChatColor.WARNING + "Please contact a supporter");
                user.sendPluginMessage(Plugin.LOUNGE, ChatColor.WARNING + "Error while team creation " + Chat.getMessageCode("E", 1700, Plugin.LOUNGE));
                user.getPlayer().kick(Component.text(ChatColor.WARNING + "Error: Please contact an admin " + Chat.getMessageCode("E", 1700, Plugin.LOUNGE) + ChatColor.PUBLIC + "\nYou can rejoin in a few seconds\n" + ChatColor.VALUE + "§lUSE /support"));
            }
        }

        Server.printText(Plugin.LOUNGE, "Finished team creation", "Team");
    }

    private void createSingleTeam() {
        Team team = GameServer.getGame().getTeams().iterator().next();

        for (User user : Server.getGameNotServiceUsers()) {
            user.sendPluginMessage(Plugin.LOUNGE, ChatColor.WARNING + "You joined team " + ChatColor.VALUE + team.getChatColor() + team.getDisplayName());
            ((TeamUser) user).setTeam(team);
            ((LoungeUser) user).setSelectedTeam((LoungeTeam) team);
            Server.printText(Plugin.LOUNGE, "User " + user.getPlayer().getName() + " joined team " + team.getName(), "Team");
        }
    }

}
