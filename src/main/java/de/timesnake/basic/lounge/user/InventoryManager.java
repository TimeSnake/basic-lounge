/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.inventory.*;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.server.StateManager;
import de.timesnake.channel.util.message.ChannelDiscordMessage;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.library.chat.ExTextColor;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class InventoryManager implements UserInventoryInteractListener,
    InventoryHolder {

  public static final ExItemStack LEAVE_ITEM = new ExItemStack(Material.RED_DYE)
      .setDisplayName("§cLeave (hold right)");

  public static final ExItemStack JOIN_LOUNGE_ITEM = ExItemStack.getLeatherArmor(Material.LEATHER_HELMET, Color.GRAY)
      .setDisplayName("§6Join")
      .setSlot(0)
      .onInteract(e -> {
        LoungeUser user = (LoungeUser) e.getUser();

        if (Server.getGameNotServiceUsers().size() >= LoungeServer.getGame().getMaxPlayers()) {
          user.sendPluginTDMessage(Plugin.LOUNGE, "§wGame is full");
          return;
        }

        if (LoungeServer.getGameCountdown() <= LoungeServer.JOINING_CLOSED) {
          user.sendPluginTDMessage(Plugin.LOUNGE, "§wGame is already starting");
          return;
        }

        user.joinLounge();
        LoungeServer.getLoungeScoreboardManager().getTablist().reloadEntry(user, true);
        user.sendPluginTDMessage(Plugin.LOUNGE, "§sJoined the game");

        LoungeServer.getStateManager().onPlayerUpdate();
        LoungeServer.getStateManager().checkStart();
      }, true);

  public static final ExItemStack SETTINGS_ITEM = new ExItemStack(Material.CLOCK)
      .setDisplayName("§6Settings")
      .onInteract(e -> ((LoungeUser) e.getUser()).openInventorySettings(), true);

  public static final ExItemStack QUICK_START = new ExItemStack(Material.NETHER_STAR)
      .setDisplayName("§6Quick Start")
      .setSlot(0)
      .setLore("§fClick to start the game in 30s")
      .onClick(e -> {
        LoungeUser user = ((LoungeUser) e.getUser());

        if (LoungeServer.getStateManager().isState(StateManager.State.STARTING)) {
          if (LoungeServer.getGameCountdown() <= 30) {
            user.sendPluginTDMessage(Plugin.LOUNGE, "§wGame is already starting");
            user.closeInventory();
            return;
          }
          LoungeServer.getTimeManager().setGameCountdown(30);
          LoungeServer.getStateManager().checkStart();
          user.sendPluginTDMessage(Plugin.LOUNGE, "§sForced a quick start");
        } else {
          user.sendPluginTDMessage(Plugin.LOUNGE, "§wCountdown must run to force a quick-start");
        }
        user.closeInventory();
      }, true);

  public static final ExItemStack WAIT = new ExItemStack(1, Material.CLOCK)
      .setSlot(1)
      .setDisplayName("§6Wait")
      .setLore("§fClick to toggle waiting")
      .onClick(e -> {
        LoungeUser user = ((LoungeUser) e.getUser());

        if (LoungeServer.getTimeManager().getGameCountdown() <= LoungeServer.JOINING_CLOSED) {
          user.sendPluginTDMessage(Plugin.LOUNGE, "§wGame is already starting");
          user.clearInventory();
          return;
        }
        LoungeServer.getStateManager().setManualWaiting(!LoungeServer.getStateManager().isManualWaiting());

        if (LoungeServer.getStateManager().isManualWaiting()) {
          LoungeServer.getInventoryManager().updateItem(1, e.getClickedItem().enchant().setExLore(List.of("",
              "§2Enabled")));
        } else {
          LoungeServer.getInventoryManager().updateItem(1, e.getClickedItem().disenchant().setExLore(List.of("",
              "§cDisabled")));
        }
        user.updateInventory();
      }, true);

  public static final ExItemStack START_SERVER = new ExItemStack(Material.BEACON)
      .setSlot(2)
      .setDisplayName("§cGame Server")
      .setLore("§fClick to force the game server to start", "§fUse only if the game is not starting")
      .onClick(e -> {
        e.getUser().runCommand("/startserver");
        e.getUser().closeInventory();
      }, true);

  public static final ExItemStack DISCORD = new ExItemStack(Material.NOTE_BLOCK)
      .setSlot(3)
      .setDisplayName("§9Discord")
      .setLore("§fClick to toggle the discord bot")
      .onClick(e -> {
        LoungeUser user = ((LoungeUser) e.getUser());

        if (LoungeServer.getGameServer().getTeamAmount() > 1) {
          if (LoungeServer.getStateManager().isGameServerReady()) {
            LoungeServer.getGameServer().setDiscord(!LoungeServer.getGameServer().isDiscord());
            Server.getChannel().sendMessage(new ChannelServerMessage<>(Server.getName(),
                MessageType.Server.DISCORD, LoungeServer.getGameServer().isDiscord()));
            if (LoungeServer.getGameServer().isDiscord()) {
              LoungeServer.getInventoryManager().updateItem(3, e.getClickedItem().enchant().setLore("", "§2Enabled"));
            } else {
              LoungeServer.getInventoryManager().updateItem(3, e.getClickedItem().disenchant().setLore("",
                  "§cDisabled"));
              Server.getChannel().sendMessage(new ChannelDiscordMessage<>(LoungeServer.getGameServer().getName(),
                  MessageType.Discord.DESTROY_CHANNELS, new LinkedList<>()));
            }
            user.updateInventory();
          } else {
            user.sendPluginTDMessage(Plugin.LOUNGE, "§wGame server is not ready");
          }
        } else {
          user.closeInventory();
          user.sendPluginTDMessage(Plugin.LOUNGE, "§wToo few teams to enable discord");
        }
      });

  private static final Integer LEAVE_TIME = 1200;
  private final ExInventory settingsInv;

  private final ExItemStack gameDescriptionItem = new ExItemStack(Material.WRITTEN_BOOK,
      "§6Game Description");

  private final HashMap<User, ItemHoldClick> clickedLeaveUsers = new HashMap<>();

  public InventoryManager() {

    if (LoungeServer.getGame().getDescription() != null) {
      BookMeta meta = ((BookMeta) this.gameDescriptionItem.getItemMeta());

      List<BaseComponent[]> pages = new ArrayList<>();

      for (String page : LoungeServer.getGame().getDescription()) {
        String[] lines = page.split("\\\\n");
        BaseComponent[] baseComponent = new BaseComponent[lines.length];
        for (int i = 0; i < lines.length; i++) {
          baseComponent[i] = new TextComponent(lines[i] + "\n");
        }
        pages.add(baseComponent);
      }

      meta.spigot().setPages(pages);
      meta.setAuthor("Game System");
      meta.setTitle(LoungeServer.getGame().getDisplayName());

      this.gameDescriptionItem.setItemMeta(meta);
    }

    this.settingsInv = new ExInventory(9, "Settings", this, QUICK_START, WAIT, START_SERVER, DISCORD);
    Server.getInventoryEventManager().addInteractListener(this, LEAVE_ITEM);
  }

  private void updateItem(int slot, ExItemStack item) {
    this.settingsInv.setItemStack(slot, item);
    this.settingsInv.update();
  }

  @Override
  public void onUserInventoryInteract(UserInventoryInteractEvent e) {
    User user = e.getUser();
    ExItemStack item = e.getClickedItem();

    if (item.equals(LEAVE_ITEM)) {
      if (e.getAction() == Action.RIGHT_CLICK_BLOCK
          || e.getAction() == Action.RIGHT_CLICK_AIR) {
        if (!this.clickedLeaveUsers.containsKey(user)) {
          this.clickedLeaveUsers.put(user, new ItemHoldClick(LEAVE_TIME));
        } else {
          if (this.clickedLeaveUsers.get(user).click()) {
            user.sendActionBarText(Component.empty());
            ((LoungeUser) user).leaveLounge();
            e.setCancelled(true);
          } else {
            user.sendActionBarText(Component.text("Leaving...", ExTextColor.WARNING));
          }
        }
      }
    }
  }

  public ExItemStack getGameDescriptionItem() {
    return gameDescriptionItem;
  }

  public ExInventory getSettingsInv() {
    return settingsInv;
  }

  @Override
  public @NotNull Inventory getInventory() {
    return settingsInv.getInventory();
  }
}
