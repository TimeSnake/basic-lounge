/*
 * basic-lounge.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.basic.lounge.map;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.exceptions.WorldNotExistException;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.database.util.game.DbLoungeMap;
import de.timesnake.database.util.game.DbLoungeMapDisplay;
import org.bukkit.GameRule;
import org.bukkit.Location;

import java.util.HashMap;

public class LoungeMap {

    private final String name;
    private final ExWorld world;
    private final ExLocation spawn;

    private final HashMap<Integer, StatDisplay> personalStatsDisplayLocationByIndex = new HashMap<>();
    private final HashMap<Integer, StatDisplay> globalStatsDisplayLocationByIndex = new HashMap<>();

    public LoungeMap(DbLoungeMap dbMap) throws WorldNotExistException {
        this.name = dbMap.getName();

        this.spawn = Server.getExLocationFromDbLocation(dbMap.getLocation());
        this.world = this.spawn.getExWorld();

        for (DbLoungeMapDisplay display : dbMap.getCachedMapDisplays()) {
            this.personalStatsDisplayLocationByIndex.put(display.getIndex(), new StatDisplay(this.world, display));
        }

        for (DbLoungeMapDisplay display : dbMap.getCachedMapDisplays()) {
            this.globalStatsDisplayLocationByIndex.put(display.getIndex(), new StatDisplay(this.world, display));
        }

        if (this.world == null) {
            Server.printWarning(Plugin.LOUNGE, "Map-World " + this.world.getName() + " of map " + this.name +
                    " could not loaded, world not exists", "lounge", "Map");
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

    public StatDisplay getPersonalStatsDisplayLocation(Integer index) {
        return personalStatsDisplayLocationByIndex.get(index);
    }

    public StatDisplay getGlobalStatsDisplayLocation(Integer index) {
        return globalStatsDisplayLocationByIndex.get(index);
    }
}
