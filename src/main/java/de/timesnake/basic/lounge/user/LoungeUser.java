/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroup;
import de.timesnake.basic.game.util.game.Kit;
import de.timesnake.basic.game.util.game.Map;
import de.timesnake.basic.game.util.game.TablistGroupType;
import de.timesnake.basic.game.util.user.StatUser;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.team.LoungeTeam;
import de.timesnake.library.basic.util.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class LoungeUser extends StatUser {

  private final Logger logger = LogManager.getLogger("lounge.user");

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
    LoungeServer.getLoungeScoreboardManager().updateScoreboardPlayerNumber(Server.getPreGameUsers().size());
  }

  @Override
  public TablistGroup getTablistGroup(de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroupType type) {
    if (type.equals(TablistGroupType.GAME_TEAM)) {
      if (this.hasStatus(Status.User.SPECTATOR)) {
        return null;
      }
      return LoungeServer.getLoungeScoreboardManager().getGameGroup();
    }
    return super.getTablistGroup(type);
  }

  public void joinLounge() {
    this.setDefault();
    this.setGameMode(GameMode.SURVIVAL);
    this.setItem(8, InventoryManager.LEAVE_ITEM);
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

    if (LoungeServer.getGame().hasTexturePack()) {
      this.setResourcePack(LoungeServer.getGame().getTexturePackLink(),
          LoungeServer.getGame().getTexturePackHash(), true);
    }
  }

  public void loadLoungeInventory() {
    if (LoungeServer.getGameServer().getTeamAmount() > 1) {
      this.setItem(0, LoungeServer.getTeamManager().getTeamSelectionItem());
    }
    if (LoungeServer.getGameServer().areKitsEnabled()) {
      this.setItem(1, LoungeServer.getKitManager().getKitSelectionItem());
    }
    if (LoungeServer.getGameServer().areMapsEnabled()) {
      this.setItem(2, LoungeServer.getMapManager().getMapSelectionItem());
    }

    if (this.hasPermission("lounge.settings")) {
      this.setItem(7, InventoryManager.SETTINGS_ITEM);
    }

    this.setItem(6, LoungeServer.getInventoryManager().getGameDescriptionItem());
    this.setItem(8, InventoryManager.LEAVE_ITEM);
  }

  @Override
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
    this.setItem(8, InventoryManager.LEAVE_ITEM);
    this.lockInventory();
  }

  public void loadSpectatorInventory() {
    this.setItem(InventoryManager.JOIN_LOUNGE_ITEM);
  }

  public void openInventoryTeamSelection() {
    this.openInventory(LoungeServer.getTeamManager().getTeamSelectionInventory());
  }

  public void openInventorySettings() {
    this.openInventory(LoungeServer.getInventoryManager().getSettingsInv());
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
    super.switchToServer(LoungeServer.getGameServer().getName());
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
      if (this.selectedTeam != null) {
        if (LoungeServer.getGameServer().areKitsEnabled()) {
          super.setSideboardScore(6, this.selectedTeam.getTDColor() + this.selectedTeam.getDisplayName());
        } else {
          super.setSideboardScore(3, this.selectedTeam.getTDColor() + this.selectedTeam.getDisplayName());
        }
      } else {
        if (LoungeServer.getGameServer().areKitsEnabled()) {
          super.setSideboardScore(6, "§7Random");
        } else {
          super.setSideboardScore(3, "§7Random");
        }
      }
    }

    if (this.getSelectedTeam() != null) {
      this.logger.info("{} selected team {}", this.getName(), this.getSelectedTeam().getName());
    }
  }


}
