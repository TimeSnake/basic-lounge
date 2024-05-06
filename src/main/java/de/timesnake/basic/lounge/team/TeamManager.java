/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.team;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.inventory.ExInventory;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.game.util.user.TeamUser;
import de.timesnake.basic.lounge.main.BasicLounge;
import de.timesnake.basic.lounge.server.LoungeServer;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;

public class TeamManager {

  private final TeamSelection teamSelection;

  public TeamManager() {
    Integer maxPlayersPerTeam = LoungeServer.getGameServer().getMaxPlayersPerTeam();
    for (Team team : LoungeServer.getGame().getTeams()) {
      ((LoungeTeam) team).clearUserSelected();
      ((LoungeTeam) team).setMaxPlayers(maxPlayersPerTeam);
      ((LoungeTeam) team).setMaxPlayersDisplay(maxPlayersPerTeam);
    }

    this.teamSelection = new TeamSelection();
  }

  public void loadTeamsIntoInventory() {
    this.teamSelection.loadTeams();
  }

  public Inventory getTeamSelectionInventory() {
    return this.teamSelection.getInventory();
  }

  public ExInventory getTeamSelectionExInventory() {
    return this.teamSelection.getExInventory();
  }

  public ExItemStack getTeamSelectionItem() {
    return this.teamSelection.getItem();
  }

  public TeamSelection getTeamSelection() {
    return teamSelection;
  }

  public void createTeams() {
    for (User user : Server.getUsers()) {
      ((TeamUser) user).setTeam(null);
    }
    Server.runTaskSynchrony(() -> this.getTeamSelectionExInventory().close(), BasicLounge.getPlugin());
    new TeamCreator().createTeams();
  }

  public void resetTeams() {
    for (Team team : LoungeServer.getGame().getTeams()) {
      team.setUsers(new HashSet<>());
      ((LoungeTeam) team).clearUserSelected();
      ((LoungeTeam) team).setMaxPlayers(null);
    }
  }
}
