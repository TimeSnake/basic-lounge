/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.user;

import de.timesnake.basic.bukkit.util.chat.cmd.Argument;
import de.timesnake.basic.bukkit.util.chat.cmd.CommandListener;
import de.timesnake.basic.bukkit.util.chat.cmd.Completion;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.library.chat.Code;
import de.timesnake.library.commands.PluginCommand;
import de.timesnake.library.commands.simple.Arguments;

public class GameCmd implements CommandListener {

  private final Code perm = Plugin.LOUNGE.createPermssionCode("lounge.game.start");
  ;

  @Override
  public void onCommand(Sender sender, PluginCommand cmd, Arguments<Argument> args) {
    sender.hasPermissionElseExit(this.perm);
    LoungeServer.getTimeManager().startGameCountdown();
  }

  @Override
  public Completion getTabCompletion() {
    return new Completion(this.perm);
  }

  @Override
  public String getPermission() {
    return this.perm.getPermission();
  }
}
