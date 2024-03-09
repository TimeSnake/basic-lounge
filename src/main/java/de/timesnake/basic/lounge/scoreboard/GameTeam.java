/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.scoreboard;

import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroup;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroupType;
import de.timesnake.library.chat.ExTextColor;

public class GameTeam implements TablistGroup {

  private final int rank;
  private final String name;
  private final String prefix;
  private final ExTextColor prefixChatColor;
  private final ExTextColor chatColor;

  public GameTeam(int rank, String name, String prefix, ExTextColor prefixChatColor, ExTextColor chatColor) {
    this.rank = rank;
    this.name = name;
    this.prefix = prefix;
    this.prefixChatColor = prefixChatColor;
    this.chatColor = chatColor;
  }

  public TablistGroupType getTeamType() {
    return TablistGroupType.DUMMY;
  }

  @Override
  public int getTablistRank() {
    return this.rank;
  }

  @Override
  public String getTablistName() {
    return this.name;
  }

  @Override
  public String getTablistPrefix() {
    return this.prefix;
  }

  @Override
  public ExTextColor getTablistPrefixChatColor() {
    return this.prefixChatColor;
  }

  @Override
  public ExTextColor getTablistChatColor() {
    return this.chatColor;
  }
}
