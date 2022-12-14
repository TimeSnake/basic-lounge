/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.basic.lounge.scoreboard;

import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroupType;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistableGroup;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistableRemainTeam;
import org.bukkit.ChatColor;

public class GameTeam implements TablistableGroup, TablistableRemainTeam {

    private final String rank;
    private final String name;
    private final String prefix;
    private final ChatColor prefixChatColor;
    private final ChatColor chatColor;

    public GameTeam(String rank, String name, String prefix, ChatColor prefixChatColor, ChatColor chatColor) {
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
    public String getTablistRank() {
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
    public ChatColor getTablistPrefixChatColor() {
        return this.prefixChatColor;
    }

    @Override
    public ChatColor getTablistChatColor() {
        return this.chatColor;
    }
}
