package de.timesnake.basic.lounge.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.event.UserJoinEvent;
import de.timesnake.basic.bukkit.util.user.event.UserQuitEvent;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.HoloDisplay;
import de.timesnake.basic.lounge.main.BasicLounge;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.library.basic.util.statistics.Stat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class StatsManager implements Listener {

    private final Map<LoungeUser, HashMap<Integer, HoloDisplay>> displaysByUser = new HashMap<>();

    public StatsManager() {
        Server.registerListener(this, BasicLounge.getPlugin());
    }

    @EventHandler
    public void onUserJoin(UserJoinEvent e) {

        LoungeUser user = ((LoungeUser) e.getUser());

        this.displaysByUser.put(user, new HashMap<>());

        for (Map.Entry<Integer, HashMap<Integer, Stat<?>>> displayEntry : LoungeServer.getGame().getStatsByLineByDisplay().entrySet()) {

            Integer displayIndex = displayEntry.getKey();
            HashMap<Integer, Stat<?>> statsByLine = displayEntry.getValue();

            int maxLine = Collections.max(statsByLine.keySet());

            LinkedList<String> lines = new LinkedList<>();

            lines.addFirst("§c§lYour Stats:");

            for (int line = 1; line <= maxLine; ++line) {
                Stat<?> stat = statsByLine.get(line);
                if (stat != null) {
                    lines.addLast(this.getLine(user, stat));
                } else {
                    lines.addLast("");
                }
            }

            ExLocation loc = LoungeServer.getCurrentMap().getPersonalStatsDisplayLocation(displayIndex);

            HoloDisplay display = new HoloDisplay(loc, lines);

            this.displaysByUser.get(user).put(displayIndex, display);

            Server.getHoloDisplayManager().addHoloDisplay(display, user);
        }


    }

    private <Value> String getLine(LoungeUser user, Stat<Value> stat) {
        return "§f" + stat.getDisplayName() + ": §7" + stat.getType().valueToDisplay(user.getStat(stat));
    }

    @EventHandler
    public void onUserQuit(UserQuitEvent e) {
        LoungeUser user = ((LoungeUser) e.getUser());
        HashMap<Integer, HoloDisplay> displays = this.displaysByUser.remove(user);

        if (displays == null) {
            return;
        }

        for (HoloDisplay display : displays.values()) {
            Server.getHoloDisplayManager().removeHoloDisplay(display);
        }
    }
}
