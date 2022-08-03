package de.timesnake.basic.lounge.user;

import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.library.basic.util.chat.ChatColor;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;

import java.util.List;

public class TeamSelectionCmd implements CommandListener {

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> exCommand, Arguments<Argument> args) {
        if (!sender.hasPermission("lounge.teamselection", 1508)) {
            return;
        }

        if (!args.isLengthEquals(1, true)) {
            return;
        }

        switch (args.getString(0).toLowerCase()) {
            case "toggle" -> {
                if (LoungeServer.getTeamManager().getTeamSelection().isBlocked()) {
                    LoungeServer.getTeamManager().getTeamSelection().block(false);
                    sender.sendPluginMessage(ChatColor.PERSONAL + "Allowed team selection");
                } else {
                    LoungeServer.getTeamManager().getTeamSelection().block(true);
                    sender.sendPluginMessage(ChatColor.PERSONAL + "Forbade team selection");
                }
            }
            case "toggle_silent" -> {
                if (LoungeServer.getTeamManager().getTeamSelection().isBlocked()) {
                    LoungeServer.getTeamManager().getTeamSelection().blockSilent(false);
                    sender.sendPluginMessage(ChatColor.PERSONAL + "Allowed team selection");
                } else {
                    LoungeServer.getTeamManager().getTeamSelection().blockSilent(true);
                    sender.sendPluginMessage(ChatColor.PERSONAL + "Forbade team selection silently");
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
}
