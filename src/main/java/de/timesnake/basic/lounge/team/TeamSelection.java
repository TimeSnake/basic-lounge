/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.team;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.inventory.ExInventory;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.library.chat.ExTextColor;
import java.util.HashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

public class TeamSelection {

    private final ExItemStack invItem;
    private final ExInventory inventory;
    private final HashMap<ExItemStack, LoungeTeam> teams = new HashMap<>();

    private boolean blocked = false;
    private boolean silentBlocked = false;

    public TeamSelection() {
        this.invItem = ExItemStack.getLeatherArmor(Material.LEATHER_HELMET, "§6Teamselection",
                        Color.BLACK)
                .hideAll()
                .immutable()
                .onInteract(event -> {
                    LoungeUser user = ((LoungeUser) event.getUser());
                    Sender sender = user.asSender(Plugin.LOUNGE);
                    if (LoungeServer.getGameCountdown() <= LoungeServer.TEAM_SELECTION_CLOSED) {
                        sender.sendPluginMessage(
                                Component.text("The team selection is closed",
                                        ExTextColor.WARNING));
                        event.setCancelled(true);
                        return;
                    }
                    user.openInventoryTeamSelection();
                    event.setCancelled(true);
                });

        int invSize = (int) (9 * Math.ceil(LoungeServer.getGameServer().getTeamAmount() / 7.0));
        this.inventory = new ExInventory(invSize > 0 ? invSize : 9,
                Component.text("Teamselection"));

        ExItemStack randomTeamItem = ExItemStack.getLeatherArmor(Material.LEATHER_HELMET,
                        "§fRandom", Color.GRAY)
                .setLore(ChatColor.GRAY + "Join Random team")
                .hideAll()
                .immutable()
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

                    user.setSelectedTeam(null);
                    sender.sendPluginMessage(
                            Component.text("You selected team ", ExTextColor.PERSONAL)
                                    .append(Component.text("Random", ExTextColor.GRAY)));
                    Server.printText(Plugin.LOUNGE, user.getName() + " selected team random",
                            "Team");

                    user.closeInventory();
                    event.setCancelled(true);
                });

        this.inventory.setItemStack(0, randomTeamItem);
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
        for (Team team : LoungeServer.getGame()
                .getTeamsSortedByRank(LoungeServer.getGameServer().getTeamAmount())) {
            // first and second inventory column empty (for random selection)
            if (i % 9 == 0) {
                i += 2;
            }

            // create item
            ExItemStack item = ((LoungeTeam) team).createTeamItem(this, i);
            // team adds item
            this.teams.put(item, ((LoungeTeam) team));
            i++;
        }
    }

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
