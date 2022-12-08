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

package de.timesnake.basic.lounge.server;

import de.timesnake.basic.game.util.game.TmpGame;
import de.timesnake.basic.game.util.server.GameServer;
import de.timesnake.basic.lounge.kit.KitManager;
import de.timesnake.basic.lounge.map.LoungeMap;
import de.timesnake.basic.lounge.map.MapManager;
import de.timesnake.basic.lounge.scoreboard.ScoreboardManager;
import de.timesnake.basic.lounge.team.TeamManager;
import de.timesnake.basic.lounge.user.InventoryManager;
import de.timesnake.library.waitinggames.WaitingGameManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;

public class LoungeServer extends GameServer {

    public static final Integer MAP_SELECTION_CLOSED = 20;
    public static final Integer JOINING_CLOSED = 15;
    public static final Integer KIT_SELECTION_CLOSED = 14;
    public static final Integer TEAM_SELECTION_CLOSED = 14;

    public static TmpGame getGame() {
        return (TmpGame) server.getGame();
    }

    public static InventoryManager getInventoryManager() {
        return server.getInventoryManager();
    }

    public static int getGameCountdown() {
        return server.getGameCountdown();
    }

    public static void prepareLounge() {
        server.prepareLounge();
    }

    public static void startGame() {
        server.startGame();
    }

    public static Location getSpawn() {
        return server.getSpawn();
    }

    @Deprecated
    public static void broadcastLoungeMessage(String msg) {
        server.broadcastLoungeMessage(msg);
    }

    public static void broadcastLoungeMessage(Component msg) {
        server.broadcastLoungeMessage(msg);
    }

    public static void broadcastCountdownCancelledMessage() {
        server.broadcastCountdownCancelledMessage();
    }

    public static ScoreboardManager getLoungeScoreboardManager() {
        return server.getLoungeScoreboardManager();
    }

    public static TmpGameServer getGameServer() {
        return server.getGameServer();
    }

    public static LoungeMap getCurrentMap() {
        return server.getCurrentMap();
    }

    public static LoungeServerManager.State getState() {
        return server.getState();
    }

    public static void setState(LoungeServerManager.State state) {
        server.setState(state);
    }

    public static Scheduler getTimeManager() {
        return server.getTimeManager();
    }

    public static void resetGameCountdown() {
        server.resetGameCountdown();
    }

    public static MapManager getMapManager() {
        return server.getMapManager();
    }

    public static KitManager getKitManager() {
        return server.getKitManager();
    }

    public static TeamManager getTeamManager() {
        return server.getTeamManager();
    }

    public static WaitingGameManager getWaitingGameManager() {
        return server.getWaitingGameManager();
    }

    public static DiscordManager getDiscordManager() {
        return server.getDiscordManager();
    }

    private static final LoungeServerManager server = LoungeServerManager.getInstance();

}
