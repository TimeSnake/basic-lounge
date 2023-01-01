/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.lounge.user.LoungeUser;
import de.timesnake.channel.util.message.ChannelDiscordMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.database.util.object.Type;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class DiscordManager {

    public void init() {
        if (!LoungeServer.getGameServer().isDiscord()) {
            return;
        }

        if (LoungeServer.getGame().getDiscordType().equals(Type.Discord.TEAMS)) {
            LinkedHashMap<String, List<UUID>> uuidsByTeam = new LinkedHashMap<>();
            for (User user : LoungeServer.getUsers()) {
                List<UUID> uuids = uuidsByTeam.computeIfAbsent(((LoungeUser) user).getTeam().getDisplayName(),
                        k -> new LinkedList<>());
                uuids.add(user.getUniqueId());
            }

            uuidsByTeam.put(LoungeServer.DISCORD_SPECTATOR, List.of());
            uuidsByTeam.put(LoungeServer.DISCORD_LOUNGE, List.of());

            Server.getChannel().sendMessage(new ChannelDiscordMessage<>(LoungeServer.getGameServer().getName(),
                    MessageType.Discord.MOVE_MEMBERS, new ChannelDiscordMessage.Allocation(uuidsByTeam)));
        } else if (LoungeServer.getGame().getDiscordType().equals(Type.Discord.DISTANCE)) {
            LinkedHashMap<String, List<UUID>> uuidsByTeam = new LinkedHashMap<>();

            uuidsByTeam.put(LoungeServer.DISCORD_SPECTATOR, List.of());
            uuidsByTeam.put(LoungeServer.DISCORD_LOUNGE, LoungeServer.getUsers().stream().map(User::getUniqueId).toList());

            Server.getChannel().sendMessage(new ChannelDiscordMessage<>(LoungeServer.getGameServer().getName(),
                    MessageType.Discord.MOVE_MEMBERS, new ChannelDiscordMessage.Allocation(uuidsByTeam)));
        }

    }

    public void cleanup() {
        Server.getChannel().sendMessage(new ChannelDiscordMessage<>(LoungeServer.getGameServer().getName(),
                MessageType.Discord.DESTROY_CHANNELS, List.of()));
    }
}
