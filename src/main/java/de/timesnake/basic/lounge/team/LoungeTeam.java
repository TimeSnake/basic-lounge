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

import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.exception.UnsupportedGroupRankException;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.database.util.game.DbTeam;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LoungeTeam extends Team {

    private final Set<LoungeUser> usersSelected = new HashSet<>();
    private Integer maxPlayers;
    private Integer maxPlayersDisplay;

    private ExItemStack item;


    public LoungeTeam(DbTeam dbTeam) throws UnsupportedGroupRankException {
        super(dbTeam);
    }

    public ExItemStack createTeamItem(int slot) {
        this.item = ExItemStack.getLeatherArmor(Material.LEATHER_HELMET, this.getChatColor() + this.getDisplayName(),
                this.getColor()).setSlot(slot).hideAll();
        this.updateItem();
        return item;
    }

    private void updateItem() {
        if (this.maxPlayersDisplay != null) {
            item.setExLore(List.of("§f" + this.usersSelected.size() + " §7/ §f" + this.maxPlayersDisplay, "",
                    ChatColor.GRAY + "Join " + this.getDisplayName() + " team"));
        } else {
            item.setExLore(List.of(ChatColor.GRAY + "Join " + this.getDisplayName() + " team"));
        }
        LoungeServer.getTeamManager().getTeamSelectionExInventory().setItemStack(this.item);
    }

    public Set<LoungeUser> getUsersSelected() {
        return usersSelected;
    }

    public void addUserSelected(LoungeUser user) {
        this.usersSelected.add(user);
        if (this.item != null) {
            this.updateItem();
        }
    }

    public void removeUserSelected(LoungeUser user) {
        this.usersSelected.remove(user);
        if (this.item != null) {
            this.updateItem();
        }
    }

    public void clearUserSelected() {
        this.usersSelected.clear();
        if (this.item != null) {
            this.updateItem();
        }
    }

    public Integer getMaxPlayers() {
        return this.maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public Integer getMaxPlayersDisplay() {
        return maxPlayersDisplay;
    }

    public void setMaxPlayersDisplay(Integer maxPlayersDisplay) {
        this.maxPlayersDisplay = maxPlayersDisplay;
        if (this.item != null) {
            this.updateItem();
        }
    }

    public ExItemStack getItem() {
        return item;
    }
}
