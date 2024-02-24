/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.team;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.game.util.user.TeamUser;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.main.BasicLounge;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.library.chat.ExTextColor;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class TeamCreator {

  private final Logger logger = LogManager.getLogger("lounge.team.creation");

  private final List<User> usersWithRandomTeam;
  private final int playerAmount;
  private final LinkedList<LoungeTeam> teams;
  private int sumMaxTeamPlayer = 0;

  public TeamCreator() {
    this.usersWithRandomTeam = new ArrayList<>();
    this.playerAmount = Server.getPreGameUsers().size();
    this.teams = new LinkedList(LoungeServer.getGame()
        .getTeamsSortedByRank(LoungeServer.getGameServer().getTeamAmount()));
    this.teams.sort(Comparator.comparingInt(Team::getRank));
  }

  public void createTeams() {

    this.logger.info("Team-selection closed");

    if (LoungeServer.getGameServer().getTeamAmount() == 0) {
      return;
    }

    this.logger.info("Team-creation:");

    if (LoungeServer.getTeamManager().getTeamSelection().isBlocked()) {
      for (Team team : LoungeServer.getGame().getTeams()) {
        ((LoungeTeam) team).clearUserSelected();
      }
    }

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
      this.setFixedSize(maxPerTeam);
    } else {
      this.setDynamically();
    }

    for (LoungeTeam team : this.teams) {
      this.logger.info("{}: {}", team.getName(), team.getMaxPlayers());
    }
  }

  private void setDynamically() {
    // calculate max players per team
    double ratioSum = 0;

    for (Team team : this.teams) {
      ratioSum += team.getRatio();
    }

    for (LoungeTeam team : this.teams) {
      int size = (int) (playerAmount * team.getRatio() / ratioSum);

      if (team.hasMinSize() && size < team.getMinSize()) {
        team.setMaxPlayers(team.getMinSize());
        size = team.getMinSize();
      } else {
        team.setMaxPlayers(size);
      }

      sumMaxTeamPlayer += size;
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

    while (sumMaxTeamPlayer > playerAmount) {
      boolean decreased = false;
      for (LoungeTeam team : this.teams) {
        if (sumMaxTeamPlayer <= playerAmount) {
          break;
        } else {
          if (team.hasMinSize() && team.getMaxPlayers() <= team.getMinSize()) {
            continue;
          }

          decreased = true;
          team.setMaxPlayers(team.getMaxPlayers() - 1);
          sumMaxTeamPlayer--;
        }
      }

      if (!decreased) {
        break;
      }
    }
  }

  private void setFixedSize(int maxPerTeam) {
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
  }

  private void divideUsersInTeams() {
    //selected teams
    List<User> users = new ArrayList<>(Server.getPreGameUsers());
    Collections.shuffle(users);

    if (!LoungeServer.getTeamManager().getTeamSelection().isBlocked()) {
      for (User user : users) {
        LoungeTeam selectedTeam = ((LoungeUser) user).getSelectedTeam();

        if (selectedTeam != null
            && selectedTeam.getUsers().size() < selectedTeam.getMaxPlayers()) {
          ((LoungeUser) user).setTeam(selectedTeam);
          ((LoungeUser) user).setSelectedTeam(selectedTeam);

          this.sendJoinedTeamMessage(user, selectedTeam);
        } else {
          usersWithRandomTeam.add(user);
        }
      }
    } else {
      this.logger.info("Random teams due to blocked selection");
      usersWithRandomTeam.addAll(users);
    }

    Collections.shuffle(this.usersWithRandomTeam);
    Collections.shuffle(this.teams);

    //random
    for (User user : usersWithRandomTeam) {
      for (LoungeTeam team : this.teams) {
        if (team.getUsers().size() < team.getMaxPlayers()) {
          ((LoungeUser) user).setSelectedTeam(team);
          ((LoungeUser) user).setTeam(team);
          this.sendJoinedTeamMessage(user, team);
          break;
        }

      }

      if (((LoungeUser) user).getTeam() == null) {
        user.getPlayer().kick(Component.text(
                "Error: Please contact an admin (team creation failed)",
                ExTextColor.WARNING).append(Component.newline())
            .append(Component.text("You can rejoin in a few seconds",
                ExTextColor.PUBLIC)).append(Component.newline())
            .append(Component.text("USE /support", ExTextColor.VALUE)));
      }
    }

    this.logger.info("Finished team creation");

    LoungeServer.getDiscordManager().init();
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
        Component.text("You joined team ", ExTextColor.PERSONAL)
            .append(Component.text(team.getDisplayName(), team.getTextColor())));
    this.logger.info("User '{}' joined team '{}'", user.getName(), team.getName());
  }

}
