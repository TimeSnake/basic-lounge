/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.kit;

import de.timesnake.basic.bukkit.util.user.inventory.ExInventory;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.user.inventory.UserInventoryClickListener;
import de.timesnake.basic.game.util.game.Kit;
import de.timesnake.basic.game.util.server.GameServer;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.library.basic.util.Loggers;
import de.timesnake.library.chat.ExTextColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

public class KitSelection {

  private final ExInventory inventory;
  private final ExItemStack item;

  public KitSelection() {
    this.item = new ExItemStack(Material.CRAFTING_TABLE).setDisplayName("§6Kits");
    this.item.onInteract(event -> {
      LoungeUser user = (LoungeUser) event.getUser();
      if (LoungeServer.getGameCountdown() <= LoungeServer.KIT_SELECTION_CLOSED) {
        user.sendPluginMessage(Plugin.LOUNGE,
            Component.text("The kit selection is closed", ExTextColor.WARNING));
        return;
      }
      user.openInventory(this.getInventory());
      event.setCancelled(true);
    });

    if (GameServer.getGame().getKits().size() == 0
        || GameServer.getGame().getKits().size() > 42) {
      this.inventory = null;
      if (LoungeServer.getGameServer().areKitsEnabled()) {
        Loggers.LOUNGE.warning("Too few/many kits for the inventory");
      }
      return;
    }

    this.inventory = new ExInventory(
        (int) (9 * Math.ceil(GameServer.getGame().getKits().size() / 7.0)),
        Component.text("Kitselection"));

    ExItemStack item = Kit.RANDOM.createDisplayItem(this.getKitClickListener(Kit.RANDOM));
    this.inventory.setItemStack(0, item);

    int i = 2;
    for (Kit kit : GameServer.getGame().getKits()) {
      while (i % 9 == 0) {
        i += 2;
      }

      this.inventory.setItemStack(i, kit.createDisplayItem(this.getKitClickListener(kit)));
      i++;
    }
  }

  private UserInventoryClickListener getKitClickListener(Kit kit) {
    return event -> {
      LoungeUser user = ((LoungeUser) event.getUser());

      if (LoungeServer.getGameCountdown() <= LoungeServer.KIT_SELECTION_CLOSED) {
        user.sendPluginMessage(de.timesnake.basic.game.util.user.Plugin.LOUNGE,
            Component.text("The kit selection is closed", ExTextColor.WARNING));
        return;
      }

      user.setSelectedKit(kit);
      user.sendPluginMessage(de.timesnake.basic.game.util.user.Plugin.LOUNGE,
          Component.text("You selected kit ", ExTextColor.PERSONAL)
              .append(Component.text(kit.getName(), ExTextColor.VALUE)));
      Loggers.LOUNGE.info(user.getName() + " selected kit " + kit.getName());

      user.closeInventory();
      event.setCancelled(true);
    };
  }

  public Inventory getInventory() {
    return this.inventory.getInventory();
  }

  public ExItemStack getItem() {
    return item;
  }
}
