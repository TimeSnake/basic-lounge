package de.timesnake.basic.lounge.user;

import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import net.kyori.adventure.text.Component;

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
}
