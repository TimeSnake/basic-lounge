/*
 * workspace.basic-lounge.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.basic.lounge.kit;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.ExInventory;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryClickListener;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryInteractEvent;
import de.timesnake.basic.bukkit.util.user.event.UserInventoryInteractListener;
import de.timesnake.basic.game.util.GameServer;
import de.timesnake.basic.game.util.Kit;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.library.basic.util.chat.ExTextColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;

public class KitSelection implements UserInventoryInteractListener, UserInventoryClickListener, InventoryHolder {

    private final ExInventory inventory;
    private final ExItemStack item;
    private final HashMap<ExItemStack, Kit> kits = new HashMap<>();

    public KitSelection() {
        this.item = new ExItemStack(Material.CRAFTING_TABLE);
        ItemMeta ksMeta = this.item.getItemMeta();
        ksMeta.setDisplayName("§6Kitselection");
        this.item.setItemMeta(ksMeta);

        if (GameServer.getGame().getKits().size() == 0 || GameServer.getGame().getKits().size() > 42) {
            this.inventory = null;
            if (LoungeServer.getGameServer().areKitsEnabled()) {
                Server.printWarning(Plugin.LOUNGE, "Too few/many kits for the inventory", "Kit");
            }
            return;
        }

        this.inventory = Server.createExInventory(9 * (GameServer.getGame().getKits().size() + 6) / 7,
                "Kitselection", this);

        ExItemStack item = this.createKitItem(Kit.RANDOM);

        this.inventory.setItemStack(0, item);
        this.kits.put(item, Kit.RANDOM);

        int i = 2;
        for (Kit kit : GameServer.getGame().getKits()) {
            while (i % 9 == 0) i += 2;
            item = this.createKitItem(kit);
            this.inventory.setItemStack(i, item);
            this.kits.put(item, kit);
            i++;
        }

        Server.getInventoryEventManager().addClickListener(this, this);
        Server.getInventoryEventManager().addInteractListener(this, this.item);
    }

    private ExItemStack createKitItem(Kit kit) {
        ExItemStack item = new ExItemStack(kit.getMaterial())
                .setDisplayName(kit.getName())
                .setExLore(new ArrayList<>(kit.getDescription()));
        item.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS);
        return item;
    }

    @Override
    public void onUserInventoryClick(UserInventoryClickEvent e) {
        LoungeUser user = ((LoungeUser) e.getUser());
        ExItemStack clickedItem = e.getClickedItem();
        Sender sender = user.asSender(Plugin.LOUNGE);

        Kit kit = this.kits.get(clickedItem);

        if (LoungeServer.getGameCountdown() <= LoungeServer.KIT_SELECTION_CLOSED) {
            user.sendPluginMessage(Plugin.LOUNGE, Component.text("The kit selection is closed", ExTextColor.WARNING));
            return;
        }

        if (kit != null) {
            if (kit.equals(Kit.RANDOM)) {
                user.setSelectedKit(Kit.RANDOM);
                sender.sendPluginMessage(Component.text("You selected kit ", ExTextColor.PERSONAL)
                        .append(Component.text(Kit.RANDOM.getName(), ExTextColor.VALUE)));
                Server.printText(Plugin.LOUNGE, user.getName() + " selected kit " + Kit.RANDOM.getName(), "Kit");
            } else {
                user.setSelectedKit(kit);
                sender.sendPluginMessage(Component.text("You selected kit ", ExTextColor.PERSONAL)
                        .append(Component.text(kit.getName(), ExTextColor.VALUE)));
                Server.printText(Plugin.LOUNGE, user.getName() + " selected kit " + kit.getName(), "Kit");
            }
            user.closeInventory();
        }
        e.setCancelled(true);
    }

    @Override
    public void onUserInventoryInteract(UserInventoryInteractEvent e) {
        LoungeUser user = (LoungeUser) e.getUser();
        if (LoungeServer.getGameCountdown() <= LoungeServer.KIT_SELECTION_CLOSED) {
            user.sendPluginMessage(Plugin.LOUNGE, Component.text("The kit selection is closed", ExTextColor.WARNING));
            return;
        }
        user.openInventory(this.getInventory());
        e.setCancelled(true);
    }

    @Override
    public Inventory getInventory() {
        return this.inventory.getInventory();
    }

    public ExItemStack getItem() {
        return item;
    }
}
