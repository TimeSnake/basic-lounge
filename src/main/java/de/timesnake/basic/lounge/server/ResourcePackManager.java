/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.lounge.main.BasicLounge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import java.time.Duration;

public class ResourcePackManager implements Listener {

  public ResourcePackManager() {
    Server.registerListener(this, BasicLounge.getPlugin());
  }

  public void loadResourcePack() {
    if (LoungeServer.getGame().hasTexturePack()) {
      LoungeServer.broadcastTDTitle("", "§wLoading resource pack...", Duration.ofSeconds(3));
    }
  }

  @EventHandler
  public void onResourcePackStatus(PlayerResourcePackStatusEvent e) {
    if (e.getStatus() == PlayerResourcePackStatusEvent.Status.ACCEPTED) {
      Server.runTaskLaterSynchrony(() -> {
        User user = Server.getUser(e.getPlayer());
        if (user != null) {
          user.removeResourcePacks();
        }
      }, 20, BasicLounge.getPlugin());
    }
  }
}
