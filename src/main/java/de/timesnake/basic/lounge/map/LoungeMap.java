/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.map;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.exception.WorldNotExistException;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.database.util.game.DbLoungeMap;
import de.timesnake.database.util.game.DbLoungeMapDisplay;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.GameRule;

import java.util.Collection;
import java.util.HashMap;

public class LoungeMap {

  private final Logger logger = LogManager.getLogger("lounge.map.map");

  private final String name;
  private final ExWorld world;
  private final ExLocation spawn;

  private final HashMap<Integer, StatDisplay> personalStatsDisplayLocationByIndex = new HashMap<>();
  private final HashMap<Integer, StatDisplay> globalStatsDisplayLocationByIndex = new HashMap<>();

  public LoungeMap(DbLoungeMap dbMap) throws WorldNotExistException {
    this.name = dbMap.getName();

    this.spawn = Server.getExLocationFromDbLocation(dbMap.getLocation());
    this.world = this.spawn.getExWorld();

    Collection<DbLoungeMapDisplay> displays = dbMap.getCachedMapDisplays();

    for (DbLoungeMapDisplay display : displays.stream().filter(d -> d.getIndex() >= 10).toList()) {
      this.personalStatsDisplayLocationByIndex.put(display.getIndex(), new StatDisplay(this.world, display));
      this.logger.info("Loaded private statistic display '{}' for map '{}'", display.getIndex(), this.name);
    }

    for (DbLoungeMapDisplay display : displays.stream().filter(d -> d.getIndex() < 10).toList()) {
      this.globalStatsDisplayLocationByIndex.put(display.getIndex(), new StatDisplay(this.world, display));
      this.logger.info("Loaded public statistic display '{}' for map '{}'", display.getIndex(), this.name);
    }

    if (this.world == null) {
      this.logger.warn("Map-World '{}' of map '{}' could not loaded, world not exists", dbMap.getWorldName(),
          this.name);
      return;
    }

    this.world.restrict(ExWorld.Restriction.ENTITY_EXPLODE, true);
    this.world.restrict(ExWorld.Restriction.NO_PLAYER_DAMAGE, false);
    this.world.restrict(ExWorld.Restriction.FOOD_CHANGE, true);
    this.world.restrict(ExWorld.Restriction.BLOCK_BURN_UP, true);
    this.world.restrict(ExWorld.Restriction.ENTITY_BLOCK_BREAK, true);
    this.world.restrict(ExWorld.Restriction.DROP_PICK_ITEM, true);
    this.world.restrict(ExWorld.Restriction.BLOCK_BREAK, true);
    this.world.setExceptService(true);
    this.world.setPVP(true);
    this.world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
    this.world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
    this.world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);

  }

  public String getName() {
    return name;
  }

  public ExWorld getWorld() {
    return world;
  }

  public ExLocation getSpawn() {
    return spawn;
  }

  public StatDisplay getPersonalStatsDisplayLocation(Integer index) {
    return personalStatsDisplayLocationByIndex.get(index);
  }

  public StatDisplay getGlobalStatsDisplayLocation(Integer index) {
    return globalStatsDisplayLocationByIndex.get(index);
  }
}
