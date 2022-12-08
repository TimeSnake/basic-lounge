/*
 * workspace.basic-lounge.main
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
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.game.util.game.Map;
import de.timesnake.basic.game.util.server.GameServer;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.server.LoungeServer;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class MapManager {

    private final Collection<Map> availableMaps = new ArrayList<>();
    private final MapSelection mapSelection;

    private final Random random = new Random();

    public MapManager() {
        for (Map map : GameServer.getGame().getMaps()) {

            // map to large
            if (map.getMaxPlayers() != null && map.getMinPlayers() > LoungeServer.getMaxPlayers()) {
                continue;
            }

            // map to small
            if (map.getMaxPlayers() != null && map.getMaxPlayers() < LoungeServer.getMaxPlayers()) {
                continue;
            }

            this.availableMaps.add(map);

            Server.printText(Plugin.LOUNGE, "Loaded game map: " + map.getName(), "Map");
        }

        this.mapSelection = new MapSelection(this.availableMaps);
    }

    public Map getVotedMap() {
        if (!LoungeServer.getGameServer().areMapsEnabled()) {
            return null;
        }

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
            output.add(map.getName() + ": " + map.getVotes());
        }

        Server.printSection(Plugin.LOUNGE, "Map Voting", output);

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
