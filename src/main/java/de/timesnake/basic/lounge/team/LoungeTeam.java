/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.team;

import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.exception.UnsupportedGroupRankException;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.database.util.game.DbTeam;
import de.timesnake.library.basic.util.Loggers;
import de.timesnake.library.chat.ExTextColor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

public class LoungeTeam extends Team {

    private final Set<LoungeUser> usersSelected = new HashSet<>();
    private Integer maxPlayers;
    private Integer maxPlayersDisplay;

    private ExItemStack item;


    public LoungeTeam(DbTeam dbTeam)
            throws UnsupportedGroupRankException {
        super(dbTeam);
    }

    public ExItemStack createTeamItem(TeamSelection teamSelection, int slot) {
        this.item = ExItemStack.getLeatherArmor(Material.LEATHER_HELMET,
                        this.getChatColor() + this.getDisplayName(), this.getColor())
                .setSlot(slot)
                .hideAll()
                .onClick(event -> {
                    LoungeUser user = ((LoungeUser) event.getUser());
                    Sender sender = user.asSender(Plugin.LOUNGE);
                    if (LoungeServer.getGameCountdown() <= LoungeServer.TEAM_SELECTION_CLOSED) {
                        sender.sendPluginMessage(
                                Component.text("Team selection is closed", ExTextColor.WARNING));
                        user.closeInventory();
                        event.setCancelled(true);
                        return;
                    }

                    if (teamSelection.isBlocked() && !teamSelection.isSilentBlocked()) {
                        sender.sendPluginMessage(
                                Component.text("Selecting teams is forbidden",
                                        ExTextColor.WARNING));
                        user.closeInventory();
                        event.setCancelled(true);
                        return;
                    }

                    if (this.getMaxPlayers() != null
                            && this.getUsersSelected().size() >= this.getMaxPlayers()) {
                        sender.sendPluginMessage(Component.text("Team ", ExTextColor.WARNING)
                                .append(Component.text(this.getDisplayName(), this.getTextColor()))
                                .append(Component.text(" is full", ExTextColor.WARNING)));
                        user.closeInventory();
                        event.setCancelled(true);
                        return;
                    }

                    user.setSelectedTeam(this);
                    sender.sendPluginMessage(
                            Component.text("You selected team ", ExTextColor.PERSONAL)
                                    .append(Component.text(this.getDisplayName(),
                                            this.getTextColor())));
                    Loggers.LOUNGE.info(user.getName() + " selected team " + this.getName());
                    user.closeInventory();

                    event.setCancelled(true);
                });
        this.updateItem();
        return item;
    }

    private void updateItem() {
        if (this.maxPlayersDisplay != null) {
            item.setExLore(
                    List.of("§f" + this.usersSelected.size() + " §7/ §f" + this.maxPlayersDisplay,
                            "",
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
