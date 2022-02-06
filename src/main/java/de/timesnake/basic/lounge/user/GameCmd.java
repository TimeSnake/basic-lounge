package de.timesnake.basic.lounge.user;

import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.library.basic.util.cmd.Arguments;
import de.timesnake.library.basic.util.cmd.ExCommand;

import java.util.List;

public class GameCmd implements CommandListener {

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {

        if (!sender.hasPermission("lounge.game.start")) {
            return;
        }

        LoungeServer.getTimeManager().startGameCountdown();
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        return null;
    }
}
