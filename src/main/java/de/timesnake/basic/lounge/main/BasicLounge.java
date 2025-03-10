/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.main;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.server.LoungeServerManager;
import de.timesnake.basic.lounge.server.StartServerCmd;
import de.timesnake.basic.lounge.team.TeamSelectionCmd;
import de.timesnake.basic.lounge.user.GameCmd;
import org.bukkit.plugin.java.JavaPlugin;


public class BasicLounge extends JavaPlugin {

  public static JavaPlugin getPlugin() {
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

    Server.getCommandManager().addCommand(this, "startserver", new StartServerCmd(), LoungeServer.PLUGIN);
    Server.getCommandManager().addCommand(this, "startgame", new GameCmd(), LoungeServer.PLUGIN);
    Server.getCommandManager().addCommand(this, "teamselection", new TeamSelectionCmd(), LoungeServer.PLUGIN);

    LoungeServerManager.getInstance().onLoungeEnable();

  }

  @Override
  public void onDisable() {
    LoungeServerManager.getInstance().onLoungeDisable();
  }

}
