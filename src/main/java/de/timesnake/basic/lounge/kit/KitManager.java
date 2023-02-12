/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.kit;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.entity.MapDisplayBuilder;
import de.timesnake.basic.game.util.game.Kit;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.library.chat.ExTextColor;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;

import java.awt.*;

public class KitManager {

    private static final Color BACKGROUND = new Color(0, 0, 0, 0);

    private final KitSelection kitSelection;
    private ExItemStack kitMenu;
    private final MapDisplayBuilder builder;

    public KitManager() {
        this.kitSelection = new KitSelection();

        this.builder = new MapDisplayBuilder(128, 128);
        this.builder.drawText(8, 8, new Font(MapDisplayBuilder.ExMapFont.MINECRAFT, Font.BOLD, 32),
                Color.BLACK, "Selected Kits:", BACKGROUND, MapDisplayBuilder.Align.LEFT);

        this.kitMenu = this.builder.onItems()[0][0].setDisplayName("§6Kits");
    }

    public void updateKits() {
        int offset = 8 + 32 + 8;
        this.builder.drawRectangle(8, offset, 120, 128 - offset, BACKGROUND);
        for (User user : LoungeServer.getUsers()) {
            Kit kit = ((LoungeUser) user).getSelectedKit();
            offset += 32;
        }

        this.kitMenu = this.builder.onItems()[0][0].setDisplayName("§6Kits");
    }

    public void loadUserKits() {
        if (!LoungeServer.getGameServer().areKitsEnabled()) {
            return;
        }

        for (User user : Server.getPreGameUsers()) {

            Kit kit = ((LoungeUser) user).getSelectedKit();

            if (kit == null || kit.getName().equals(Kit.RANDOM.getName())) {
                int random = (int) (Math.random() * (LoungeServer.getGame().getKits().size() - 1));
                ((LoungeUser) user).setSelectedKit(LoungeServer.getGame().getKit(random));
            } else {
                ((LoungeUser) user).setSelectedKit(kit);
            }
            user.sendPluginMessage(Plugin.LOUNGE,
                    Component.text("You will get the kit ", ExTextColor.PERSONAL)
                            .append(Component.text(((LoungeUser) user).getSelectedKit().getName(), ExTextColor.VALUE)));
        }
    }

    public Inventory getKitSelectionInventory() {
        return this.kitSelection.getInventory();
    }

    public ExItemStack getKitSelectionItem() {
        return this.kitSelection.getItem();
    }


}
