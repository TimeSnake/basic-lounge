package de.timesnake.basic.lounge.team;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExInventory;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.game.util.Team;
import de.timesnake.basic.game.util.TeamUser;
import de.timesnake.basic.lounge.main.BasicLounge;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.channel.util.message.ChannelDiscordMessage;
import de.timesnake.channel.util.message.MessageType;
import org.bukkit.inventory.Inventory;

import java.util.*;

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

    public void createTeams() {
        for (User user : Server.getUsers()) {
            ((TeamUser) user).setTeam(null);
        }
        Server.runTaskSynchrony(() -> Server.getUsersWithOpenInventory("Teamselection").forEach(User::closeInventory), BasicLounge.getPlugin());
        new TeamCreator().createTeams();
    }

    public void resetTeams() {
        for (Team team : LoungeServer.getGame().getTeams()) {
            team.setUsers(new HashSet<>());
            ((LoungeTeam) team).clearUserSelected();
            ((LoungeTeam) team).setMaxPlayers(null);
        }
    }

    public void initDiscord() {
        if (LoungeServer.getGameServer().isDiscord()) {
            LinkedHashMap<String, List<UUID>> uuidsByTeam = new LinkedHashMap<>();
            for (User user : LoungeServer.getUsers()) {
                List<UUID> uuids = uuidsByTeam.computeIfAbsent(((LoungeUser) user).getTeam().getDisplayName(), k -> new LinkedList<>());
                uuids.add(user.getUniqueId());
            }

            uuidsByTeam.put(LoungeServer.DISCORD_SPECTATOR, List.of());
            uuidsByTeam.put(LoungeServer.DISCORD_LOUNGE, List.of());

            Server.getChannel().sendMessage(new ChannelDiscordMessage<>(LoungeServer.getGameServer().getName(), MessageType.Discord.MOVE_TEAMS, new ChannelDiscordMessage.Allocation(uuidsByTeam)));
        }

    }
}
