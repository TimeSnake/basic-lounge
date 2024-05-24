/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.scoreboard;

import de.timesnake.basic.bukkit.core.user.scoreboard.tablist.Tablist2;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.group.DisplayGroup;
import de.timesnake.basic.bukkit.util.user.scoreboard.ScoreboardManager;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.bukkit.util.user.scoreboard.SideboardBuilder;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroup;
import de.timesnake.basic.game.util.game.TablistGroupType;
import de.timesnake.basic.lounge.server.LoungeServer;

import java.util.ArrayList;
import java.util.List;

public class LoungeScoreboardManager {

  private final Tablist2 tablist;
  private final TablistGroup gameGroup;
  private final TablistGroup spectatorGroup;

  private final Sideboard sideboard;
  private final Sideboard spectatorSideboard;

  public LoungeScoreboardManager() {
    this.gameGroup = new TablistGroup() {

      @Override
      public int getTablistRank() {
        return 0;
      }

      @Override
      public String getTablistName() {
        return "game";
      }
    };
    this.spectatorGroup = new TablistGroup() {

      @Override
      public int getTablistRank() {
        return 1;
      }

      @Override
      public String getTablistName() {
        return "spec";
      }
    };

    List<de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroupType> types = new ArrayList<>();
    types.add(TablistGroupType.GAME_TEAM);
    types.addAll(DisplayGroup.MAIN_TABLIST_GROUPS);

    this.tablist = Server.getScoreboardManager().registerTablist(
        new Tablist2.Builder("lounge_side")
            .colorGroupType(TablistGroupType.GAME_TEAM)
            .groupTypes(types)
            .addDefaultGroup(TablistGroupType.GAME_TEAM, this.spectatorGroup));

    if (LoungeServer.getGameServer().areKitsEnabled()) {
      this.tablist.setHeader("§6" + LoungeServer.getGame().getDisplayName() + " §bKits");
    } else {
      this.tablist.setHeader("§6" + LoungeServer.getGame().getDisplayName());
    }

    this.tablist.setFooter(ScoreboardManager.getDefaultFooter());

    Server.getScoreboardManager().setActiveTablist(this.tablist);

    // sideboard

    this.sideboard = Server.getScoreboardManager().registerSideboard(new SideboardBuilder()
        .name("lounge")
        .title("§6§l" + LoungeServer.getGame().getDisplayName()));

    this.spectatorSideboard = Server.getScoreboardManager()
        .registerSideboard(new SideboardBuilder()
            .name("lounge_spectator")
            .title("§6§l" + LoungeServer.getGame().getDisplayName()));

    if (LoungeServer.getGameServer().areKitsEnabled()) {
      this.sideboard.setScore(4, "§lKit");
      // user kit
      if (LoungeServer.getGameServer().getTeamAmount() > 1) {
        this.sideboard.setScore(11, "§9§lPlayers");
        this.updateScoreboardPlayerNumber(10, 0);
        // more needed display
        this.sideboard.setScore(8, "§r§r" + Sideboard.SPACER);
        this.sideboard.setScore(7, "§9§lTeam");
        //user team
        this.sideboard.setScore(5, "§r" + Sideboard.SPACER);
        // kit
      } else {
        this.sideboard.setScore(8, "§9§lPlayers");
        this.updateScoreboardPlayerNumber(7, 0);
        // more needed display
        this.sideboard.setScore(5, "§r§r" + Sideboard.SPACER);
        // kit
      }
    } else if (LoungeServer.getGameServer().getTeamAmount() > 1) {
      this.sideboard.setScore(9, "§9§lPlayers");
      this.updateScoreboardPlayerNumber(7, 0);
      // more needed display
      this.sideboard.setScore(5, "§r§r" + Sideboard.SPACER);
      this.sideboard.setScore(4, "§9§lTeam");
      // user team
    } else {
      this.sideboard.setScore(5, "§l§9Players");
      this.updateScoreboardPlayerNumber(4, 0);
      // more needed display
    }
    this.sideboard.setScore(2, Sideboard.SPACER);
    this.sideboard.setScore(1, "§7§lServer");
    this.sideboard.setScore(0, "§7" + Server.getName());

    this.spectatorSideboard.setScore(5, "§l§9Players");
    // player amount
    this.spectatorSideboard.setScore(2, Sideboard.SPACER);
    this.spectatorSideboard.setScore(1, "§7§lServer");
    this.spectatorSideboard.setScore(0, "§7" + Server.getName());
  }

  public Sideboard getSideboard() {
    return this.sideboard;
  }

  public Sideboard getSpectatorSideboard() {
    return this.spectatorSideboard;
  }

  public void updateScoreboardPlayerNumber(int line, int online) {
    int needMore = LoungeServer.getGame().getAutoStartPlayerNumber() - online;
    int max = Server.getMaxPlayers();
    this.sideboard.setScore(line, online + "§7/§f" + max);
    this.spectatorSideboard.setScore(4, online + "§7/§f" + max);
    if (needMore > 0) {
      this.sideboard.setScore(line - 1, "§7" + needMore + " more needed");
      this.spectatorSideboard.setScore(3, "§7" + needMore + " more needed");
    } else {
      this.sideboard.setScore(line - 1, "");
      this.spectatorSideboard.setScore(3, "");
    }
  }

  public void updateScoreboardPlayerNumber(int online) {
    if (LoungeServer.getGameServer().getTeamAmount() > 1) {
      if (LoungeServer.getGameServer().areKitsEnabled()) {
        this.updateScoreboardPlayerNumber(10, online);
      } else {
        this.updateScoreboardPlayerNumber(7, online);
      }
    } else {
      if (LoungeServer.getGameServer().areKitsEnabled()) {
        this.updateScoreboardPlayerNumber(7, online);
      } else {
        this.updateScoreboardPlayerNumber(4, online);
      }
    }
  }

  public Tablist2 getTablist() {
    return tablist;
  }

  public TablistGroup getGameGroup() {
    return gameGroup;
  }

  public TablistGroup getSpectatorGroup() {
    return spectatorGroup;
  }
}
