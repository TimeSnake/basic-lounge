package de.timesnake.basic.lounge.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.event.UserJoinEvent;
import de.timesnake.basic.bukkit.util.user.event.UserQuitEvent;
import de.timesnake.basic.bukkit.util.world.entity.MapDisplay;
import de.timesnake.basic.bukkit.util.world.entity.MapDisplayBuilder;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.main.BasicLounge;
import de.timesnake.basic.lounge.map.StatDisplay;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ChannelListener;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.database.util.Database;
import de.timesnake.database.util.user.DbUser;
import de.timesnake.library.basic.util.Triple;
import de.timesnake.library.basic.util.Tuple;
import de.timesnake.library.basic.util.statistics.Stat;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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


        HashMap<Integer, MapDisplay> displays = this.displayByUser.computeIfAbsent(user, u -> new HashMap<>());

        for (Map.Entry<Integer, HashMap<Integer, Stat<?>>> displayEntry :
                LoungeServer.getGame().getStatByLineByDisplay().entrySet()) {

            Integer displayIndex = displayEntry.getKey();
            HashMap<Integer, Stat<?>> statsByLine = displayEntry.getValue();

            if (statsByLine.keySet().isEmpty()) {
                continue;
            }

            StatDisplay display = LoungeServer.getCurrentMap().getPersonalStatsDisplayLocation(displayIndex);

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
                    new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.BOLD, FONT_SIZE + 8), titleColor,
                    "Personal Stats", background, MapDisplayBuilder.Align.CENTER);

            for (int line = 0; line < maxLine; ++line) {
                Stat<?> stat = statsByLine.get(line + 1);
                if (stat != null) {
                    int y = yOffset + line * (FONT_SIZE + lineOffset);
                    displayBuilder.drawText(xOffset, y,
                            new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.PLAIN, FONT_SIZE), nameColor,
                            stat.getDisplayName(), background, MapDisplayBuilder.Align.LEFT);
                    displayBuilder.drawText(xSoreOffset, y,
                            new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.PLAIN, FONT_SIZE),
                            firstColor, this.getPersonalStat(user, stat), background, MapDisplayBuilder.Align.RIGHT);
                }
            }

            MapDisplay mapDisplay = displayBuilder.onBlock(user, display.getBlock(),
                    BlockFace.valueOf(display.getFacing().name()),
                    BlockFace.valueOf(display.getOrientation().name()), true);

            displays.put(displayIndex, mapDisplay);
        }

    }

    private <Value> String getPersonalStat(LoungeUser user, Stat<Value> stat) {
        return stat.getType().valueToDisplay(user.getStat(stat));
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

        for (Map.Entry<Integer, HashMap<Integer, Stat<?>>> displayEntry :
                LoungeServer.getGame().getGlobalStatByLineByDisplay().entrySet()) {

            Integer displayIndex = displayEntry.getKey();
            HashMap<Integer, Stat<?>> statsByLine = displayEntry.getValue();

            if (statsByLine.keySet().isEmpty()) {
                continue;
            }

            StatDisplay display = LoungeServer.getCurrentMap().getGlobalStatsDisplayLocation(displayIndex);

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
                    new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.BOLD, FONT_SIZE + 8), titleColor,
                    "Global Stats", background, MapDisplayBuilder.Align.CENTER);

            for (int line = 0; line < maxLine; ++line) {
                Stat<?> stat = statsByLine.get(line + 1);
                if (stat != null) {
                    Triple<Tuple<String, String>, Tuple<String, String>, Tuple<String, String>> places =
                            this.getGlobalLine(stat);

                    int y = yOffset + 4 * line * (FONT_SIZE + lineOffset) + line * 32;
                    displayBuilder.drawText(width * 128 / 2, y,
                            new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.BOLD, FONT_SIZE), nameColor,
                            stat.getDisplayName(), background, MapDisplayBuilder.Align.CENTER);

                    y += FONT_SIZE + lineOffset;

                    displayBuilder.drawText(xOffset, y,
                            new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.PLAIN, FONT_SIZE), firstColor,
                            "1. " + places.getA().getA(), background, MapDisplayBuilder.Align.LEFT);
                    displayBuilder.drawText(xSoreOffset, y,
                            new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.PLAIN, FONT_SIZE),
                            firstColor, places.getA().getB(), background, MapDisplayBuilder.Align.RIGHT);

                    y += FONT_SIZE + lineOffset;

                    displayBuilder.drawText(xOffset, y,
                            new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.PLAIN, FONT_SIZE),
                            secondColor, "2. " + places.getB().getA(), background, MapDisplayBuilder.Align.LEFT);
                    displayBuilder.drawText(xSoreOffset, y,
                            new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.PLAIN, FONT_SIZE),
                            secondColor, places.getB().getB(), background, MapDisplayBuilder.Align.RIGHT);

                    y += FONT_SIZE + lineOffset;

                    displayBuilder.drawText(xOffset, y,
                            new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.PLAIN, FONT_SIZE),
                            thirdColor, "3. " + places.getC().getA(), background, MapDisplayBuilder.Align.LEFT);
                    displayBuilder.drawText(xSoreOffset, y,
                            new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.PLAIN, FONT_SIZE),
                            thirdColor, places.getC().getB(), background, MapDisplayBuilder.Align.RIGHT);
                }
            }

            MapDisplay mapDisplay = displayBuilder.onBlock(display.getBlock(),
                    BlockFace.valueOf(display.getFacing().name()),
                    BlockFace.valueOf(display.getOrientation().name()), true);

            this.globalDisplayByIndex.put(displayIndex, mapDisplay);
        }
    }

    private <Value> Triple<Tuple<String, String>, Tuple<String, String>, Tuple<String, String>> getGlobalLine(Stat<Value> stat) {
        Tuple<UUID, Value> first = new Tuple<>(null, stat.getDefaultValue());
        Tuple<UUID, Value> second = new Tuple<>(null, stat.getDefaultValue());
        Tuple<UUID, Value> third = new Tuple<>(null, stat.getDefaultValue());

        for (Tuple<UUID, Value> userStat : LoungeServer.getGame().getDatabase().getStatOfUsers(stat)) {
            if (stat.compare(userStat.getB(), first.getB()) >= 0) {
                third = second;
                second = first;
                first = userStat;
            } else if (stat.compare(userStat.getB(), second.getB()) >= 0) {
                third = second;
                second = userStat;
            } else if (stat.compare(userStat.getB(), third.getB()) >= 0) {
                third = userStat;
            }
        }

        DbUser firstUser = Database.getUsers().getUser(first.getA());
        DbUser secondUser = Database.getUsers().getUser(second.getA());
        DbUser thirdUser = Database.getUsers().getUser(third.getA());

        Tuple<String, String> firstPlace = new Tuple<>("-", "-");
        Tuple<String, String> secondPlace = new Tuple<>("-", "-");
        Tuple<String, String> thirdPlace = new Tuple<>("-", "-");

        if (first.getA() != null && firstUser != null && firstUser.exists()) {
            firstPlace = new Tuple<>(firstUser.getName(), stat.getType().valueToDisplay(first.getB()));
        }

        if (second.getA() != null && secondUser != null && secondUser.exists()) {
            secondPlace = new Tuple<>(secondUser.getName(), stat.getType().valueToDisplay(second.getB()));
        }

        if (third.getA() != null && thirdUser != null && thirdUser.exists()) {
            thirdPlace = new Tuple<>(thirdUser.getName(), stat.getType().valueToDisplay(third.getB()));
        }

        return new Triple<>(firstPlace, secondPlace, thirdPlace);
    }

    @ChannelHandler(type = ListenerType.SERVER_USER_STATS)
    public void onChannelMessage(ChannelServerMessage<String> msg) {
        if (msg.getValue().equals(LoungeServer.getGame().getName())) {
            this.updateGlobalDisplays();
            Server.printText(Plugin.LOUNGE, "Updated global stats");
        }
    }

}
