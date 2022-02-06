package de.timesnake.basic.lounge.map;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.exceptions.WorldNotExistException;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.database.util.game.DbLoungeMap;
import org.bukkit.GameRule;
import org.bukkit.Location;

import java.util.HashMap;

public class LoungeMap {

    private static final String SPAWN = "spawn";
    private static final String PERSONAL_STATS_DISPLAY_0 = "personal_stats_display_0";
    private static final String PERSONAL_STATS_DISPLAY_1 = "personal_stats_display_1";
    private static final String PERSONAL_STATS_DISPLAY_2 = "personal_stats_display_2";

    private final String name;
    private final ExWorld world;
    private final ExLocation spawn;

    private final HashMap<Integer, ExLocation> personalStatsDisplayLocationByIndex = new HashMap<>();

    public LoungeMap(DbLoungeMap dbMap) throws WorldNotExistException {
        this.name = dbMap.getName();

        this.spawn = Server.getExLocationFromDbLocation(dbMap.getLocation(SPAWN));

        this.personalStatsDisplayLocationByIndex.put(0, Server.getExLocationFromDbLocation(dbMap.getLocation(PERSONAL_STATS_DISPLAY_0)));
        this.personalStatsDisplayLocationByIndex.put(1, Server.getExLocationFromDbLocation(dbMap.getLocation(PERSONAL_STATS_DISPLAY_1)));
        this.personalStatsDisplayLocationByIndex.put(2, Server.getExLocationFromDbLocation(dbMap.getLocation(PERSONAL_STATS_DISPLAY_2)));

        this.world = this.spawn.getExWorld();

        if (this.world == null) {
            Server.printWarning(Plugin.LOUNGE, "Map-World " + this.world.getName() + " of map " + this.name + " could not loaded, world not exists", "lounge", "Map");
            return;
        }

        this.world.allowEntityExplode(false);
        this.world.allowPlayerDamage(true);
        this.world.allowFoodChange(false);
        this.world.allowBlockBurnUp(false);
        this.world.allowEntityBlockBreak(false);
        this.world.allowDropPickItem(false);
        this.world.allowBlockBreak(false);
        this.world.setExceptService(true);
        this.world.setPVP(true);
        this.world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        this.world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

    }

    public String getName() {
        return name;
    }

    public ExWorld getWorld() {
        return world;
    }

    public Location getSpawn() {
        return spawn;
    }

    public ExLocation getPersonalStatsDisplayLocation(Integer index) {
        return personalStatsDisplayLocationByIndex.get(index);
    }
}
