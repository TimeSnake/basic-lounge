package de.timesnake.basic.lounge.main;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.lounge.server.LoungeServerManager;
import de.timesnake.basic.lounge.server.StartServerCmd;
import de.timesnake.basic.lounge.user.GameCmd;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;


public class BasicLounge extends JavaPlugin {

    public static BasicLounge plugin;

    public static Plugin getPlugin() {
        return plugin;
    }

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

        LoungeServerManager.getInstance().onLoungeEnable();

    }

    @Override
    public void onDisable() {
        LoungeServerManager.getInstance().onLoungeDisable();
    }

}
