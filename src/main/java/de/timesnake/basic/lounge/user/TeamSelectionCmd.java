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

package de.timesnake.basic.lounge.user;

import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Code;
import de.timesnake.library.extension.util.chat.Plugin;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import net.kyori.adventure.text.Component;

import java.util.List;

public class TeamSelectionCmd implements CommandListener {

    private Code.Permission teamSelectionPerm;

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> exCommand, Arguments<Argument> args) {
        if (!sender.hasPermission(this.teamSelectionPerm)) {
            return;
        }

        if (!args.isLengthEquals(1, true)) {
            return;
        }

        switch (args.getString(0).toLowerCase()) {
            case "toggle" -> {
                if (LoungeServer.getTeamManager().getTeamSelection().isBlocked()) {
                    LoungeServer.getTeamManager().getTeamSelection().block(false);
                    sender.sendPluginMessage(Component.text("Allowed team selection", ExTextColor.PERSONAL));
                } else {
                    LoungeServer.getTeamManager().getTeamSelection().block(true);
                    sender.sendPluginMessage(Component.text("Forbade team selection", ExTextColor.PERSONAL));
                }
            }
            case "toggle_silent" -> {
                if (LoungeServer.getTeamManager().getTeamSelection().isBlocked()) {
                    LoungeServer.getTeamManager().getTeamSelection().blockSilent(false);
                    sender.sendPluginMessage(Component.text("Allowed team selection", ExTextColor.PERSONAL));
                } else {
                    LoungeServer.getTeamManager().getTeamSelection().blockSilent(true);
                    sender.sendPluginMessage(Component.text("Forbade team selection silently", ExTextColor.PERSONAL));
                }
            }
        }
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> exCommand, Arguments<Argument> args) {
        if (args.length() == 1) {
            return List.of("toggle", "toggle_silent");
        }
        return List.of();
    }

    @Override
    public void loadCodes(Plugin plugin) {
        this.teamSelectionPerm = plugin.createPermssionCode("lts", "lounge.teamselection");
    }
}
