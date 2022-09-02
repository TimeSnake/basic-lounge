package de.timesnake.basic.lounge.kit;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.game.util.Kit;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.library.basic.util.chat.ExTextColor;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;

public class KitManager {

    private final KitSelection kitSelection;

    public KitManager() {
        this.kitSelection = new KitSelection();
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
