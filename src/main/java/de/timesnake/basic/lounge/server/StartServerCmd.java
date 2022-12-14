/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.basic.lounge.server;

import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Code;
import de.timesnake.library.extension.util.chat.Plugin;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import net.kyori.adventure.text.Component;

import java.util.List;

public class StartServerCmd implements CommandListener {

    private Code.Permission startServerPerm;

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (!sender.hasPermission(this.startServerPerm)) {
            return;
        }

        LoungeServer.getGameServer().start();
        sender.sendPluginMessage(Component.text("Game server started", ExTextColor.PERSONAL));
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        return null;
    }

    @Override
    public void loadCodes(Plugin plugin) {
        this.startServerPerm = plugin.createPermssionCode("ssc", "lounge.start.server");
    }
}
