/*
 * basic-lounge.main
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
