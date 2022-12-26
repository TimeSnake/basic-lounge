/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.basic.lounge.user;

import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.library.extension.util.chat.Code;
import de.timesnake.library.extension.util.chat.Plugin;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;

import java.util.List;

public class GameCmd implements CommandListener {

    private Code.Permission gameStartPerm;

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {

        if (!sender.hasPermission(this.gameStartPerm)) {
            return;
        }

        LoungeServer.getTimeManager().startGameCountdown();
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        return null;
    }

    @Override
    public void loadCodes(Plugin plugin) {
        this.gameStartPerm = plugin.createPermssionCode("lgs", "lounge.game.start");
    }
}
