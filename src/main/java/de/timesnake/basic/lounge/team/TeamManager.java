/*
 * workspace.basic-lounge.main
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

package de.timesnake.basic.lounge.team;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExInventory;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
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
        Server.runTaskSynchrony(() -> Server.getUsersWithOpenInventory("Teamselection")
                .forEach(User::closeInventory), BasicLounge.getPlugin());
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
