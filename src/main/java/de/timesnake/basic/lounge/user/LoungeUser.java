package de.timesnake.basic.lounge.user;

import de.timesnake.basic.bukkit.util.user.scoreboard.TablistableGroup;
import de.timesnake.basic.game.util.Kit;
import de.timesnake.basic.game.util.Map;
import de.timesnake.basic.game.util.StatUser;
import de.timesnake.basic.game.util.TablistGroupType;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.team.LoungeTeam;
import de.timesnake.library.basic.util.Status;
import org.bukkit.entity.Player;

public class LoungeUser extends StatUser {

    public static final String SPECTATOR_TABLIST_PREFIX = "§7";

    private boolean isLeaving;
    private Kit selectedKit;
    private Map selectedMap;
    private LoungeTeam selectedTeam;

    public LoungeUser(Player player) {
        super(player);
        this.isLeaving = false;
    }

    @Override
    public void setStatus(Status.User status) {
        super.setStatus(status);
        LoungeServer.getLoungeScoreboardManager().updateScoreboardPlayerNumber();
    }

    @Override
    public TablistableGroup getTablistGroup(de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroupType type) {
        if (type.equals(TablistGroupType.DUMMY)) {
            return LoungeServer.getLoungeScoreboardManager().getGameTeam();
        }
        return super.getTablistGroup(type);
    }

    public void joinLounge() {
        this.setDefault();
        this.addItemLeave();
        this.teleport(LoungeServer.getSpawn());
        this.setStatus(Status.User.PRE_GAME);
        this.setTask(LoungeServer.getTask());
        this.setSideboard(LoungeServer.getLoungeScoreboardManager().getSideboard());
        super.setBossBar(LoungeServer.getTimeManager().getInfoBar());
        this.setSelectedKit(Kit.RANDOM);
        this.setTeam(null);
        this.setSelectedKit(null);
        this.setSelectedMap(null);
        this.setSelectedTeam(null);
        this.lockInventoryItemMove();
        this.loadLoungeInventory();
    }

    public void loadLoungeInventory() {
        if (LoungeServer.getGameServer().getTeamAmount() > 1) {
            this.addItemTeamSelection();
        }
        if (LoungeServer.getGameServer().areKitsEnabled()) {
            this.addItemKitSelection();
        }
        if (LoungeServer.getGameServer().areMapsEnabled()) {
            this.addItemMapSelection();
        }
        if (this.hasPermission("lounge.settings")) {
            this.addItemSettings();
        }

        this.addItemGameDescription();
        this.addItemLeave();
    }

    public void joinSpectator() {
        this.setDefault();
        this.setStatus(Status.User.SPECTATOR);
        this.setTask(LoungeServer.getTask());
        this.setSideboard(LoungeServer.getLoungeScoreboardManager().getSpectatorSideboard());
        this.setTeam(null);
        this.setSelectedKit(null);
        this.setSelectedMap(null);
        this.setSelectedTeam(null);
        this.teleport(LoungeServer.getSpawn());
        this.addItemLeave();
        this.lockInventory();
    }

    public void loadSpectatorInventory() {
        this.setItem(InventoryManager.JOIN_LOUNGE_ITEM);
    }

    public void openInventoryTeamSelection() {
        this.openInventory(LoungeServer.getTeamManager().getTeamSelectionInventory());
    }

    public void openInventoryKitSelection() {
        this.openInventory(LoungeServer.getKitManager().getKitSelectionInventory());
    }

    public void openInventorySettings() {
        this.openInventory(LoungeServer.getInventoryManager().getSettingsInv());
    }

    public void openInventoryMapSelection() {
        this.openInventory(LoungeServer.getMapManager().getMapSelectionInventory());
    }

    public void addItemTeamSelection() {
        this.setItem(0, LoungeServer.getTeamManager().getTeamSelectionItem());
    }

    public void addItemKitSelection() {
        this.setItem(1, LoungeServer.getKitManager().getKitSelectionItem());
    }

    public void addItemSettings() {
        this.setItem(7, InventoryManager.SETTINGS_ITEM);
    }

    public void addItemMapSelection() {
        this.setItem(2, LoungeServer.getMapManager().getMapSelectionItem());
    }

    public void addItemGameDescription() {
        this.setItem(6, LoungeServer.getInventoryManager().getGameDescriptionItem());
    }

    public void addItemLeave() {
        this.setItem(8, InventoryManager.LEAVE_ITEM);
    }

    public void leaveLounge() {
        if (!this.isLeaving) {
            this.setLeaving(true);
            this.switchToLobbyLast();
        }
    }

    public boolean isLeaving() {
        return isLeaving;
    }

    public void setLeaving(boolean isLeaving) {
        this.isLeaving = isLeaving;
    }

    public Kit getSelectedKit() {
        return selectedKit;
    }

    public void setSelectedKit(Kit kit) {
        this.selectedKit = kit;
        if (kit != null) {
            this.getDatabase().setKit(kit.getId());
            if (LoungeServer.getGameServer().areKitsEnabled()) {
                super.setSideboardScore(3, kit.getName());
            }
        } else {
            this.getDatabase().setKit(null);
        }

    }

    public void switchToGameServer() {
        super.switchToServer(LoungeServer.getGameServer().getPort());
    }

    public Map getSelectedMap() {
        return selectedMap;
    }

    public void setSelectedMap(Map map) {
        if (this.selectedMap != null) {
            this.selectedMap.removeVote();
        }

        this.selectedMap = map;

        if (this.selectedMap != null) {
            this.selectedMap.addVote();
        }
    }

    public LoungeTeam getSelectedTeam() {
        return selectedTeam;
    }

    public void setSelectedTeam(LoungeTeam team) {
        if (this.selectedTeam != null) {
            this.selectedTeam.removeUserSelected(this);
        }

        this.selectedTeam = team;

        if (this.selectedTeam != null) {
            this.selectedTeam.addUserSelected(this);
        }


        if (LoungeServer.getGame().getTeams().size() > 1) {
            if (this.getSelectedTeam() != null) {
                if (LoungeServer.getGameServer().areKitsEnabled()) {
                    super.setSideboardScore(6, team.getChatColor() + team.getDisplayName());
                } else {
                    super.setSideboardScore(3, team.getChatColor() + team.getDisplayName());
                }
            } else {
                if (LoungeServer.getGameServer().areKitsEnabled()) {
                    super.setSideboardScore(6, "§7Random");
                } else {
                    super.setSideboardScore(3, "§7Random");
                }
            }
        }
    }


}
