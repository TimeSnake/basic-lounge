package de.timesnake.basic.lounge.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserDamageByUserEvent;
import de.timesnake.basic.bukkit.util.user.event.UserDamageEvent;
import de.timesnake.basic.bukkit.util.user.event.UserJoinEvent;
import de.timesnake.basic.bukkit.util.user.event.UserQuitEvent;
import de.timesnake.basic.game.util.GameServer;
import de.timesnake.basic.lounge.chat.Plugin;
import de.timesnake.basic.lounge.server.LoungeServer;
import de.timesnake.basic.lounge.server.LoungeServerManager;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.waitinggames.WaitingGameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class UserManager implements Listener {

    @EventHandler
    public void onUserJoin(UserJoinEvent e) {
        User user = e.getUser();
        String task = user.getTask();

        // spectator
        if (user.getStatus().equals(Status.User.SPECTATOR)) {
            ((LoungeUser) user).joinSpectator();
            user.sendPluginMessage(Plugin.LOUNGE, ChatColor.WARNING + "You can join the game in a few " + "moments");
            return;
        }

        // game user
        if (task != null) {
            if (task.equalsIgnoreCase(GameServer.getGame().getName())) {
                ((LoungeUser) user).joinLounge();
                LoungeServer.checkAutoStart();
                LoungeServer.getLoungeScoreboardManager().updateScoreboardPlayerNumber();

                if (Server.getGameNotServiceUsers().size() == 1) {
                    if (user.getLastServer() != null) {
                        if (user.getLastServer().getPort().equals(LoungeServer.getGameServer().getPort())) {
                            LoungeServer.setState(LoungeServerManager.State.WAITING);
                        }
                    }
                    LoungeServer.getCurrentMap().getWorld().setTime(0);
                    LoungeServer.getGameServer().start();
                }
                return;
            }
        }

        user.sendPluginMessage(Plugin.LOUNGE, ChatColor.WARNING + "You didn't joined the lounge!");
        user.asSender(Plugin.LOUNGE).sendMessageCommandHelp("Use", "service");
        user.getInventory().clear();
    }

    @EventHandler
    public void onUserQuit(UserQuitEvent e) {
        User user = e.getUser();
        String task = user.getTask();
        if (task != null) {
            if (task.equalsIgnoreCase(GameServer.getGame().getName())) {
                LoungeServer.getLoungeScoreboardManager().updateScoreboardPlayerNumber(Server.getPreGameUsers().size());
                if (Server.getGameNotServiceUsers().size() <= GameServer.getGame().getAutoStart()) {
                    LoungeServer.resetGameCountdown();
                }

                ((LoungeUser) user).setSelectedMap(null);
                ((LoungeUser) user).setSelectedTeam(null);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!Server.getUser(e.getWhoClicked().getUniqueId()).isService()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (!Server.getUser(e.getPlayer().getUniqueId()).isService()) {
            if (e.getTo().getBlockY() <= 10) {
                e.getPlayer().teleport(LoungeServer.getSpawn());
            }
        }
    }

    @EventHandler
    public void onUserDamage(UserDamageEvent e) {
        if (!e.getDamageCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK) && !e.getDamageCause().equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onUserDamageByUser(UserDamageByUserEvent e) {
        WaitingGameManager waitingGameManager = LoungeServer.getWaitingGameManager();

        boolean gameManaged = waitingGameManager.onUserDamage(e);

        if (!gameManaged) {
            e.setCancelled(true);
        }
    }
}
