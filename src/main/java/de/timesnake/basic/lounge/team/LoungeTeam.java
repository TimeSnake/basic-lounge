package de.timesnake.basic.lounge.team;

import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.exceptions.UnsupportedGroupRankException;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.game.util.Team;
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
        this.item = new ExItemStack(Material.LEATHER_HELMET, this.getChatColor() + this.getDisplayName(),
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
