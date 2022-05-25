package de.timesnake.basic.lounge.kit;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
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
    private final HashMap<Integer, Kit> kits = new HashMap<>();

    public KitSelection() {
        this.item = new ExItemStack(Material.CRAFTING_TABLE);
        ItemMeta ksMeta = this.item.getItemMeta();
        ksMeta.setDisplayName("§6Kitselection");
        this.item.setItemMeta(ksMeta);

        if (GameServer.getGame().getKits().size() == 0 || GameServer.getGame().getKits().size() > 42) {
            this.inventory = null;
            if (LoungeServer.getGameServer().areKitsEnabled()) {
                Server.printError(Plugin.LOUNGE, "Too few/many kits for the inventory", "Kit");
            }
            return;
        }

        this.inventory =
                Server.createExInventory((int) (9 * Math.ceil((((double) GameServer.getGame().getKits().size()) / 7))), "Kitselection", this);

        ExItemStack item = this.createKitItem(Kit.RANDOM);

        this.inventory.setItemStack(0, item);
        this.kits.put(item.getId(), Kit.RANDOM);

        int i = 2;
        for (Kit kit : GameServer.getGame().getKits()) {
            while (i % 9 == 0) i += 2;
            item = this.createKitItem(kit);
            this.inventory.setItemStack(i, item);
            this.kits.put(item.getId(), kit);
            i++;
        }

        Server.getInventoryEventManager().addClickListener(this, this);
        Server.getInventoryEventManager().addInteractListener(this, this.item);
    }

    private ExItemStack createKitItem(Kit kit) {
        ExItemStack item = new ExItemStack(kit.getMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(kit.getName());
        meta.setLore(new ArrayList<>(kit.getDescription()));
        meta.addItemFlags(ItemFlag.HIDE_DYE);
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void onUserInventoryClick(UserInventoryClickEvent e) {
        LoungeUser user = ((LoungeUser) e.getUser());
        ExItemStack clickedItem = e.getClickedItem();
        Sender sender = user.asSender(Plugin.LOUNGE);
        Kit kit = this.kits.get(clickedItem.getId());

        if (LoungeServer.getGameCountdown() <= LoungeServer.KIT_SELECTION_CLOSED) {
            user.sendPluginMessage(Plugin.LOUNGE, ChatColor.WARNING + "The kit selection is closed");
            return;
        }

        if (kit != null) {
            if (kit.equals(Kit.RANDOM)) {
                user.setSelectedKit(Kit.RANDOM);
                sender.sendPluginMessage(ChatColor.PERSONAL + "You selected kit " + ChatColor.VALUE + Kit.RANDOM.getName());
                Server.printText(Plugin.LOUNGE, user.getChatName() + " selected kit " + Kit.RANDOM.getName(), "Kit");
            } else {
                user.setSelectedKit(kit);
                sender.sendPluginMessage(ChatColor.PERSONAL + "You selected kit " + ChatColor.VALUE + kit.getName());
                Server.printText(Plugin.LOUNGE, user.getChatName() + " selected kit " + kit.getName(), "Kit");
            }
            user.closeInventory();
        }
        e.setCancelled(true);
    }

    @Override
    public void onUserInventoryInteract(UserInventoryInteractEvent e) {
        LoungeUser user = (LoungeUser) e.getUser();
        if (LoungeServer.getGameCountdown() <= LoungeServer.KIT_SELECTION_CLOSED) {
            user.sendPluginMessage(Plugin.LOUNGE, ChatColor.WARNING + "The kit selection is closed");
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
