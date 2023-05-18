/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.event.UserJoinEvent;
import de.timesnake.basic.bukkit.util.user.event.UserQuitEvent;
import de.timesnake.basic.bukkit.util.world.entity.MapDisplay;
import de.timesnake.basic.bukkit.util.world.entity.MapDisplayBuilder;
import de.timesnake.basic.lounge.main.BasicLounge;
import de.timesnake.basic.lounge.map.StatDisplay;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.user.DbUser;
import de.timesnake.library.basic.util.Loggers;
import de.timesnake.library.basic.util.Triple;
import de.timesnake.library.basic.util.statistics.Stat;
import de.timesnake.library.basic.util.statistics.StatPeriod;
import de.timesnake.library.basic.util.statistics.StatType;
import java.awt.Color;
import java.awt.Font;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class StatsManager implements Listener, ChannelListener {

  private static final int FONT_SIZE = 24;

  private final Map<LoungeUser, HashMap<Integer, MapDisplay>> displayByUser = new HashMap<>();
  private final Map<Integer, MapDisplay> globalDisplayByIndex = new HashMap<>();

  public StatsManager() {
    Server.registerListener(this, BasicLounge.getPlugin());
    Server.getChannel().addListener(this);
  }

  @EventHandler
  public void onUserJoin(UserJoinEvent e) {

    LoungeUser user = ((LoungeUser) e.getUser());

    HashMap<Integer, MapDisplay> displays = this.displayByUser.computeIfAbsent(user,
        u -> new HashMap<>());

    for (Map.Entry<Integer, HashMap<Integer, StatType<?>>> displayEntry :
        LoungeServer.getGame().getStatByLineByDisplay().entrySet()) {

      Integer displayIndex = displayEntry.getKey();
      HashMap<Integer, StatType<?>> statsByLine = displayEntry.getValue();

      if (statsByLine.keySet().isEmpty()) {
        continue;
      }

      StatDisplay display = LoungeServer.getCurrentMap()
          .getPersonalStatsDisplayLocation(displayIndex);

      if (display == null) {
        continue;
      }

      int width = 3;
      int height = 4;

      MapDisplayBuilder displayBuilder = new MapDisplayBuilder(width, height);

      int maxLine = Collections.max(statsByLine.keySet());

      int yOffset = 90;
      int xOffset = 8;
      int xSoreOffset = width * 128 - 8;
      int lineOffset = 32 - FONT_SIZE;

      Color background = new Color(0, 0, 0, 0);
      Color titleColor = display.getTitleColor();
      Color nameColor = display.getStatNameColor();
      Color firstColor = display.getStatFirstColor();

      displayBuilder.drawText(width * 128 / 2, 18,
          new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.BOLD, FONT_SIZE + 8),
          titleColor,
          "Personal Stats", background, MapDisplayBuilder.Align.CENTER);

      for (int line = 0; line < maxLine; ++line) {
        StatType<?> stat = statsByLine.get(line + 1);
        String value = this.getPersonalStat(user, stat);
        if (stat != null && value != null) {
          int y = yOffset + line * (FONT_SIZE + lineOffset);
          displayBuilder.drawText(xOffset, y,
              new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.PLAIN, FONT_SIZE),
              nameColor,
              stat.getDisplayName(), background, MapDisplayBuilder.Align.LEFT);
          displayBuilder.drawText(xSoreOffset, y,
              new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.PLAIN, FONT_SIZE),
              firstColor, value, background, MapDisplayBuilder.Align.RIGHT);
        }
      }

      MapDisplay mapDisplay = displayBuilder.onBlock(user, display.getBlock(),
          BlockFace.valueOf(display.getFacing().name()),
          BlockFace.valueOf(display.getOrientation().name()), true);

      displays.put(displayIndex, mapDisplay);
    }

  }

  private <Value> String getPersonalStat(LoungeUser user, StatType<Value> type) {
    Stat<Value> stat = type != null ? user.getStat(type) : null;
    return stat != null ? type.valueToDisplay(stat.get(StatPeriod.QUARTER)) : null;
  }

  @EventHandler
  public void onUserQuit(UserQuitEvent e) {
    LoungeUser user = ((LoungeUser) e.getUser());
    HashMap<Integer, MapDisplay> displays = this.displayByUser.remove(user);

    if (displays == null) {
      return;
    }

    for (MapDisplay display : displays.values()) {
      Server.getEntityManager().unregisterEntity(display);
    }
  }

  public void updateGlobalDisplays() {

    if (LoungeServer.getCurrentMap() == null) {
      return;
    }

    for (MapDisplay display : this.globalDisplayByIndex.values()) {
      Server.getEntityManager().unregisterEntity(display);
    }

    this.globalDisplayByIndex.clear();

    for (Map.Entry<Integer, HashMap<Integer, StatType<?>>> displayEntry :
        LoungeServer.getGame().getGlobalStatByLineByDisplay().entrySet()) {

      Integer displayIndex = displayEntry.getKey();
      HashMap<Integer, StatType<?>> statsByLine = displayEntry.getValue();

      if (statsByLine.keySet().isEmpty()) {
        continue;
      }

      StatDisplay display = LoungeServer.getCurrentMap()
          .getGlobalStatsDisplayLocation(displayIndex);

      if (display == null) {
        return;
      }

      int width = 3;
      int height = 5;

      MapDisplayBuilder displayBuilder = new MapDisplayBuilder(width, height);

      int maxLine = Collections.max(statsByLine.keySet());

      int yOffset = 90;
      int xOffset = 8;
      int xSoreOffset = width * 128 - 8;
      int lineOffset = 32 - FONT_SIZE;

      Color background = new Color(0, 0, 0, 0);
      Color titleColor = display.getTitleColor();
      Color nameColor = display.getStatNameColor();
      Color firstColor = display.getStatFirstColor();
      Color secondColor = display.getStatSecondColor();
      Color thirdColor = display.getStatThirdColor();

      displayBuilder.drawText(width * 128 / 2, 18,
          new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.BOLD, FONT_SIZE + 8),
          titleColor,
          "Global Stats", background, MapDisplayBuilder.Align.CENTER);

      for (int line = 0; line < maxLine; ++line) {
        StatType<?> stat = statsByLine.get(line + 1);
        if (stat == null) {
          continue;
        }

        Triple<Triple<String, String, String>, Triple<String, String, String>, Triple<String, String, String>> places =
            this.getGlobalLine(stat);

        int y = yOffset + 4 * line * (FONT_SIZE + lineOffset) + line * 32;
        displayBuilder.drawText(width * 128 / 2, y,
            new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.BOLD, FONT_SIZE),
            nameColor,
            stat.getDisplayName(), background, MapDisplayBuilder.Align.CENTER);

        y += FONT_SIZE + lineOffset;

        displayBuilder.drawText(xOffset, y,
            new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.PLAIN, FONT_SIZE),
            firstColor,
            places.getA().getA() + ". " + places.getA().getB(), background,
            MapDisplayBuilder.Align.LEFT);
        displayBuilder.drawText(xSoreOffset, y,
            new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.PLAIN, FONT_SIZE),
            firstColor, places.getA().getC(), background,
            MapDisplayBuilder.Align.RIGHT);

        y += FONT_SIZE + lineOffset;

        displayBuilder.drawText(xOffset, y,
            new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.PLAIN, FONT_SIZE),
            secondColor, places.getB().getA() + ". " + places.getB().getB(),
            background, MapDisplayBuilder.Align.LEFT);
        displayBuilder.drawText(xSoreOffset, y,
            new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.PLAIN, FONT_SIZE),
            secondColor, places.getB().getC(), background,
            MapDisplayBuilder.Align.RIGHT);

        y += FONT_SIZE + lineOffset;

        displayBuilder.drawText(xOffset, y,
            new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.PLAIN, FONT_SIZE),
            thirdColor, places.getC().getA() + ". " + places.getC().getB(),
            background, MapDisplayBuilder.Align.LEFT);
        displayBuilder.drawText(xSoreOffset, y,
            new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.PLAIN, FONT_SIZE),
            thirdColor, places.getC().getC(), background,
            MapDisplayBuilder.Align.RIGHT);
      }

      MapDisplay mapDisplay = displayBuilder.onBlock(display.getBlock(),
          BlockFace.valueOf(display.getFacing().name()),
          BlockFace.valueOf(display.getOrientation().name()), true);

      this.globalDisplayByIndex.put(displayIndex, mapDisplay);
    }
  }

  private <Value> Triple<Triple<String, String, String>, Triple<String, String, String>, Triple<String, String, String>> getGlobalLine(
      StatType<Value> stat) {
    Triple<Integer, UUID, Value> first = new Triple<>(1, null, stat.getDefaultValue());
    Triple<Integer, UUID, Value> second = new Triple<>(2, null, stat.getDefaultValue());
    Triple<Integer, UUID, Value> third = new Triple<>(3, null, stat.getDefaultValue());

    for (Map.Entry<UUID, Value> userStat : LoungeServer.getGame().getDatabase()
        .getStatOfUsers(StatPeriod.QUARTER, stat).entrySet()) {
      if (userStat.getValue() == null) {
        continue;
      }
      if (stat.compare(userStat.getValue(), first.getC()) > 0) {
        third = second;
        third.setA(third.getA() + 1);
        second = first;
        second.setA(second.getA() + 1);
        first = new Triple<>(1, userStat.getKey(), userStat.getValue());
      } else if (stat.compare(userStat.getValue(), first.getC()) == 0) {
        third = second;
        third.setA(third.getA() + 1);
        second = first;
        first = new Triple<>(1, userStat.getKey(), userStat.getValue());
      } else if (stat.compare(userStat.getValue(), second.getC()) > 0) {
        third = second;
        third.setA(third.getA() + 1);
        second = new Triple<>(2, userStat.getKey(), userStat.getValue());
      } else if (stat.compare(userStat.getValue(), second.getC()) == 0) {
        third = second;
        second = new Triple<>(2, userStat.getKey(), userStat.getValue());
      } else if (stat.compare(userStat.getValue(), third.getC()) > 0) {
        third = new Triple<>(third.getA(), userStat.getKey(), userStat.getValue());
      }
    }

    DbUser firstUser = Database.getUsers().getUser(first.getB());
    DbUser secondUser = Database.getUsers().getUser(second.getB());
    DbUser thirdUser = Database.getUsers().getUser(third.getB());

    Triple<String, String, String> firstPlace = new Triple<>("-", "-", "-");
    Triple<String, String, String> secondPlace = new Triple<>("-", "-", "-");
    Triple<String, String, String> thirdPlace = new Triple<>("-", "-", "-");

    if (first.getA() != null && firstUser != null && firstUser.exists()) {
      firstPlace = new Triple<>(first.getA().toString(), firstUser.getName(),
          stat.valueToDisplay(first.getC()));
    }

    if (second.getA() != null && secondUser != null && secondUser.exists()) {
      secondPlace = new Triple<>(second.getA().toString(), secondUser.getName(),
          stat.valueToDisplay(second.getC()));
    }

    if (third.getA() != null && thirdUser != null && thirdUser.exists()) {
      thirdPlace = new Triple<>(third.getA().toString(), thirdUser.getName(),
          stat.valueToDisplay(third.getC()));
    }

    return new Triple<>(firstPlace, secondPlace, thirdPlace);
  }

  @ChannelHandler(type = ListenerType.SERVER_USER_STATS)
  public void onChannelMessage(ChannelServerMessage<String> msg) {
    if (msg.getValue().equals(LoungeServer.getGame().getName())) {
      this.updateGlobalDisplays();
      Loggers.LOUNGE.info("Updated global stats");
    }
  }

}
