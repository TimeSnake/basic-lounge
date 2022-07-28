package de.timesnake.basic.lounge.team;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.game.util.Team;
import de.timesnake.basic.game.util.TeamUser;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.main.BasicLounge;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.library.extension.util.chat.Chat;
import net.kyori.adventure.text.Component;

import java.util.*;

public class TeamCreator {

    private final List<LoungeUser> usersWithRandomTeam;
    private final int playerAmount;
    private final LinkedList<LoungeTeam> teams;
    private int sumMaxTeamPlayer = 0;

    public TeamCreator() {
        this.usersWithRandomTeam = new ArrayList<>();
        this.playerAmount = Server.getPreGameUsers().size();
        this.teams =
                new LinkedList(LoungeServer.getGame().getTeamsSortedByRank(LoungeServer.getGameServer().getTeamAmount()).values());
        this.teams.sort(Comparator.comparingInt(Team::getRank));
    }

    public void createTeams() {

        Server.printText(Plugin.LOUNGE, "Team-selection closed", "Team");

        if (LoungeServer.getGameServer().getTeamAmount() == 0) {
            return;
        }

        Server.printText(Plugin.LOUNGE, "Team-creation:", "Team");

        Server.runTaskAsynchrony(() -> {
            if (LoungeServer.getGameServer().getTeamAmount() == 1) {
                this.createSingleTeam();
            } else {
                this.createMultipleTeams();
            }
        }, BasicLounge.getPlugin());

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
            for (LoungeTeam team : this.teams) {
                team.setMaxPlayers(maxPerTeam);
            }
            if (LoungeServer.getGameServer().isMergeTeams()) {
                int smallestTeamAmount = (int) Math.ceil(this.playerAmount / ((double) maxPerTeam));

                // prevent merge in one team
                if (smallestTeamAmount <= 1) {
                    smallestTeamAmount = 2;
                }

                // try to keep teams with higher user selections
                this.teams.sort(Comparator.comparingInt(t -> t.getUsersSelected().size()));
                Collections.reverse(this.teams);

                // remove not used teams
                while (smallestTeamAmount < this.teams.size()) {
                    this.teams.removeLast();
                }

                // adjust team size to player size (decrease size team size)
                int teamPlayerSum = this.teams.size() * maxPerTeam;

                Iterator<LoungeTeam> teamIterator = this.teams.descendingIterator();
                while (teamPlayerSum > playerAmount) {
                    if (!teamIterator.hasNext()) {
                        teamIterator = this.teams.descendingIterator();
                    }

                    LoungeTeam team = teamIterator.next();
                    team.setMaxPlayers(team.getMaxPlayers() - 1);
                    teamPlayerSum--;
                }
            }
        } else {
            // calculate max players per team
            double ratioSum = 0;
            for (Team team : this.teams) {
                ratioSum += team.getRatio();
            }
            for (LoungeTeam team : this.teams) {
                team.setMaxPlayers((int) (playerAmount * team.getRatio() / ratioSum));
                sumMaxTeamPlayer += team.getMaxPlayers();
            }

            // fix round fails
            while (sumMaxTeamPlayer < playerAmount) {
                for (LoungeTeam team : this.teams) {
                    if (sumMaxTeamPlayer >= playerAmount) {
                        break;
                    } else {
                        team.setMaxPlayers(team.getMaxPlayers() + 1);
                        sumMaxTeamPlayer++;
                    }
                }
            }
        }

        for (LoungeTeam team : this.teams) {
            Server.printText(Plugin.LOUNGE, team.getName() + ": " + team.getMaxPlayers());
        }
    }

    private void divideUsersInTeams() {
        //selected teams
        List<User> users = new ArrayList<>(Server.getPreGameUsers());
        Collections.shuffle(users);

        for (User user : users) {
            LoungeTeam selectedTeam = ((LoungeUser) user).getSelectedTeam();

            if (selectedTeam != null && selectedTeam.getUsers().size() < selectedTeam.getMaxPlayers()) {
                ((LoungeUser) user).setTeam(selectedTeam);
                ((LoungeUser) user).setSelectedTeam(selectedTeam);

                this.sendJoinedTeamMessage(user, selectedTeam);
            } else {
                usersWithRandomTeam.add(((LoungeUser) user));
            }
        }

        Collections.shuffle(this.usersWithRandomTeam);
        Collections.shuffle(this.teams);

        //random
        for (LoungeUser user : usersWithRandomTeam) {
            for (LoungeTeam team : this.teams) {
                if (team.getUsers().size() < team.getMaxPlayers()) {
                    user.setSelectedTeam(team);
                    user.setTeam(team);
                    this.sendJoinedTeamMessage(user, team);
                    break;
                }

            }

            if (user.getTeam() == null) {
                user.getPlayer().kick(Component.text(ChatColor.WARNING + "Error: Please contact an admin " + Chat.getMessageCode("E", 1700, Plugin.LOUNGE) + ChatColor.PUBLIC + "\nYou can rejoin in a few seconds\n" + ChatColor.VALUE + "§lUSE /support"));
            }
        }

        Server.printText(Plugin.LOUNGE, "Finished team creation", "Team");

        LoungeServer.getTeamManager().initDiscord();
    }

    private void createSingleTeam() {
        Team team = LoungeServer.getGame().getTeams().iterator().next();

        for (User user : Server.getGameNotServiceUsers()) {
            ((TeamUser) user).setTeam(team);
            ((LoungeUser) user).setSelectedTeam((LoungeTeam) team);
            this.sendJoinedTeamMessage(user, team);
        }
    }

    private void sendJoinedTeamMessage(User user, Team team) {
        user.sendPluginMessage(Plugin.LOUNGE,
                ChatColor.PERSONAL + "You joined team " + ChatColor.VALUE + team.getChatColor() + team.getDisplayName());
        Server.printText(Plugin.LOUNGE, "User " + user.getPlayer().getName() + " joined team " + team.getName(),
                "Team");
    }

}
