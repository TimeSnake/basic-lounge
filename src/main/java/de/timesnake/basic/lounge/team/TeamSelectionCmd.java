/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.team;

import de.timesnake.basic.bukkit.util.chat.cmd.Argument;
import de.timesnake.basic.bukkit.util.chat.cmd.CommandListener;
import de.timesnake.basic.bukkit.util.chat.cmd.Completion;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.library.chat.ExTextColor;
import de.timesnake.library.commands.PluginCommand;
import de.timesnake.library.commands.simple.Arguments;
import de.timesnake.library.extension.util.chat.Code;
import net.kyori.adventure.text.Component;

public class TeamSelectionCmd implements CommandListener {

  private final Code perm = Plugin.LOUNGE.createPermssionCode("lounge.teamselection");

  @Override
  public void onCommand(Sender sender, PluginCommand PluginCommand, Arguments<Argument> args) {
    sender.hasPermissionElseExit(this.perm);
    args.isLengthEqualsElseExit(1, true);

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
  public Completion getTabCompletion() {
    return new Completion(this.perm)
        .addArgument(new Completion("toggle", "toggle_silent"));
  }

  @Override
  public String getPermission() {
    return this.perm.getPermission();
  }
}
