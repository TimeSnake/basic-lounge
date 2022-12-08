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
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.ExInventory;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickListener;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryInteractEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryInteractListener;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.library.basic.util.chat.ExTextColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;

public class TeamSelection implements UserInventoryClickListener, UserInventoryInteractListener, InventoryHolder {

    private final ExItemStack invItem;
    private final ExInventory inventory;
    private final HashMap<ExItemStack, LoungeTeam> teams = new HashMap<>();

    private boolean blocked = false;
    private boolean silentBlocked = false;

    public TeamSelection() {
        this.invItem = ExItemStack.getLeatherArmor(Material.LEATHER_HELMET, "§6Teamselection", Color.BLACK)
                .hideAll();

        int invSize = (int) (9 * Math.ceil(LoungeServer.getGameServer().getTeamAmount() / 7.0));
        this.inventory = new ExInventory(invSize > 0 ? invSize : 9, Component.text("Teamselection"), this);

        // random team
        ExItemStack randomTeamItem =
                ExItemStack.getLeatherArmor(Material.LEATHER_HELMET, "§fRandom", Color.GRAY)
                        .setLore(ChatColor.GRAY + "Join Random team").hideAll();

        this.inventory.setItemStack(0, randomTeamItem);

        Server.getInventoryEventManager().addClickListener(this, this);
        Server.getInventoryEventManager().addInteractListener(this, invItem);
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void block(boolean allowed) {
        this.blocked = allowed;
        if (allowed) {
            this.silentBlocked = false;
        }
    }

    public boolean isSilentBlocked() {
        return silentBlocked;
    }

    public void blockSilent(boolean blockSilent) {
        this.block(blockSilent);
        this.silentBlocked = blockSilent;
    }

    protected void loadTeams() {
        int i = 2;
        for (Team team :
                LoungeServer.getGame().getTeamsSortedByRank(LoungeServer.getGameServer().getTeamAmount()).values()) {
            // first and second inventory column empty (for random selection)
            if (i % 9 == 0) i += 2;

            // create item
            ExItemStack item = ((LoungeTeam) team).createTeamItem(i);
            // team adds item
            this.teams.put(item, ((LoungeTeam) team));
            i++;
        }
    }

    @Override
    public void onUserInventoryClick(UserInventoryClickEvent e) {
        LoungeUser user = ((LoungeUser) e.getUser());
        ExItemStack clickedItem = e.getClickedItem();
        Sender sender = user.asSender(Plugin.LOUNGE);
        if (LoungeServer.getGameCountdown() <= LoungeServer.TEAM_SELECTION_CLOSED) {
            sender.sendPluginMessage(Component.text("Team selection is closed", ExTextColor.WARNING));
            user.closeInventory();
            e.setCancelled(true);
            return;
        }

        LoungeTeam team = this.teams.get(clickedItem);
        if (team != null) {
            if (this.blocked && !this.silentBlocked) {
                sender.sendPluginMessage(Component.text("Team selection is forbidden", ExTextColor.WARNING));
                user.closeInventory();
                e.setCancelled(true);
                return;
            }

            if (team.getMaxPlayers() != null && team.getUsersSelected().size() >= team.getMaxPlayers()) {
                sender.sendPluginMessage(Component.text("Team ", ExTextColor.WARNING)
                        .append(Component.text(team.getDisplayName(), team.getTextColor()))
                        .append(Component.text(" is full", ExTextColor.WARNING)));
                user.closeInventory();
                e.setCancelled(true);
                return;
            }

            user.setSelectedTeam(team);
            sender.sendPluginMessage(Component.text("You selected team ", ExTextColor.PERSONAL)
                    .append(Component.text(team.getDisplayName(), team.getTextColor())));
            Server.printText(Plugin.LOUNGE, user.getName() + " selected team " + team.getName(), "Team");
        } else {
            user.setSelectedTeam(null);
            sender.sendPluginMessage(Component.text("You selected team ", ExTextColor.PERSONAL)
                    .append(Component.text("Random", ExTextColor.GRAY)));
            Server.printText(Plugin.LOUNGE, user.getName() + " selected team random", "Team");
        }
        user.closeInventory();

        e.setCancelled(true);
    }

    @Override
    public void onUserInventoryInteract(UserInventoryInteractEvent e) {
        LoungeUser user = ((LoungeUser) e.getUser());
        Sender sender = user.asSender(Plugin.LOUNGE);
        if (LoungeServer.getGameCountdown() <= LoungeServer.TEAM_SELECTION_CLOSED) {
            sender.sendPluginMessage(Component.text("The team selection is closed", ExTextColor.WARNING));
            e.setCancelled(true);
            return;
        }
        user.openInventoryTeamSelection();
        e.setCancelled(true);
    }

    @Override
    public Inventory getInventory() {
        return inventory.getInventory();
    }

    public ExInventory getExInventory() {
        return this.inventory;
    }

    public ExItemStack getItem() {
        return invItem;
    }

}
