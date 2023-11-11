/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.map;

import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.game.util.game.Map;
import de.timesnake.basic.game.util.server.GameServer;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.library.basic.util.Loggers;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class MapManager {

  private final Collection<Map> availableMaps = new ArrayList<>();
  private final MapSelection mapSelection;

  private final Random random = new Random();

  public MapManager() {
    for (Map map : GameServer.getGame().getMaps()) {

      if (map.getMaxPlayers() != null && map.getMinPlayers() > LoungeServer.getMaxPlayers()) {
        continue;
      }

      if (map.getMaxPlayers() != null && map.getMaxPlayers() < LoungeServer.getMaxPlayers()) {
        continue;
      }

      if (!map.getTeamAmounts().isEmpty() && !map.getTeamAmounts().contains(LoungeServer.getTeamAmount())) {
        continue;
      }

      this.availableMaps.add(map);

      Loggers.LOUNGE.info("Loaded game map: " + map.getName());
    }

    this.mapSelection = new MapSelection(this.availableMaps);
  }

  public Map getVotedMap() {
    if (!LoungeServer.getGameServer().areMapsEnabled()) {
      return null;
    }

    Loggers.LOUNGE.info("---- MAP VOTING ----");

    List<String> output = new ArrayList<>();

    List<Map> votedMaps = new ArrayList<>();
    int votes = -1;
    for (Map map : this.availableMaps) {
      if (map.getVotes() > votes) {
        votedMaps.clear();
        votedMaps.add(map);
        votes = map.getVotes();
      } else if (map.getVotes() == votes) {
        votedMaps.add(map);
      }
      Loggers.LOUNGE.info(map.getName() + ": " + map.getVotes());
    }
    Loggers.LOUNGE.info("---- MAP VOTING ----");

    Collections.shuffle(votedMaps);

    return votedMaps.get(this.random.nextInt(votedMaps.size()));
  }

  public void resetMapVotes() {
    for (Map map : LoungeServer.getGame().getMaps()) {
      map.resetVotes();
    }
  }

  public Collection<Map> getAvailableMaps() {
    return availableMaps;
  }

  public Inventory getMapSelectionInventory() {
    return this.mapSelection.getInventory();
  }

  public ExItemStack getMapSelectionItem() {
    return this.mapSelection.getItem();
  }
}
