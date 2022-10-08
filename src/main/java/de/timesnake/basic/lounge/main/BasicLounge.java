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

package de.timesnake.basic.lounge.main;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.lounge.server.LoungeServerManager;
import de.timesnake.basic.lounge.server.StartServerCmd;
import de.timesnake.basic.lounge.user.GameCmd;
import de.timesnake.basic.lounge.user.TeamSelectionCmd;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;


public class BasicLounge extends JavaPlugin {

    public static Plugin getPlugin() {
        return plugin;
    }

    public static BasicLounge plugin;

    @Override
    public void onLoad() {
        ServerManager.setInstance(new LoungeServerManager());
    }

    @Override
    public void onEnable() {

        BasicLounge.plugin = this;

        Server.getCommandManager().addCommand(this, "startserver", new StartServerCmd(),
                de.timesnake.basic.lounge.chat.Plugin.LOUNGE);
        Server.getCommandManager().addCommand(this, "startgame", new GameCmd(),
                de.timesnake.basic.lounge.chat.Plugin.LOUNGE);
        Server.getCommandManager().addCommand(this, "teamselection", new TeamSelectionCmd(),
                de.timesnake.basic.lounge.chat.Plugin.LOUNGE);

        LoungeServerManager.getInstance().onLoungeEnable();

    }

    @Override
    public void onDisable() {
        LoungeServerManager.getInstance().onLoungeDisable();
    }

}
