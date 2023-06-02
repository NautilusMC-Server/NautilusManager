package org.nautilusmc.nautilusmanager.cosmetics.commands;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.ChatMsgCommand;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.cosmetics.Nickname;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.*;

public class ChatCommand extends NautilusCommand {

    private static final Map<UUID, ChatType> CHATS = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("You must be a player to use this command").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        if (strings.length == 0 || strings[0].equalsIgnoreCase("all")) {
            player.sendMessage(Component.text("Now chatting with all players").color(TextColor.color(255, 211, 41)));
            CHATS.remove(player.getUniqueId());
            return true;
        } else if (strings[0].equalsIgnoreCase("player")) {
            if (strings.length < 2) return false;
            Player chat = Util.getOnlinePlayer(strings[1]);

            if (chat == null) {
                commandSender.sendMessage(Component.text("Player not found").color(NautilusCommand.ERROR_COLOR));
                return true;
            }

            player.sendMessage(Component.text("Now chatting with ").append(chat.getPlayer().displayName()).color(TextColor.color(255, 211, 41)));
            CHATS.put(player.getUniqueId(), new ChatType("player", chat.getUniqueId()));
            return true;
        } else if (strings[0].equalsIgnoreCase("staff")) {
            if (!commandSender.hasPermission(NautilusCommand.STAFF_CHAT_PERM)) {
                commandSender.sendMessage(Component.text("Not enough permissions").color(NautilusCommand.ERROR_COLOR));
                return true;
            }

            player.sendMessage(Component.text("Now in staff chat").color(TextColor.color(255, 211, 41)));
            CHATS.put(player.getUniqueId(), ChatType.STAFF);
            return true;

        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        // lazy but sure?
        return new ChatMsgCommand().onTabComplete(commandSender, command, s, strings);
    }

    private static class ChatType {
        public static final ChatType STAFF = new ChatType("staff", null);

        public final String commandOption;
        public final UUID uuid;

        public ChatType(String commandOption, UUID uuid) {
            this.commandOption = commandOption;
            this.uuid = uuid;
        }
    }

    public static class ChatListener implements Listener {

        @EventHandler
        public void onQuit(PlayerQuitEvent e) {
            for (Map.Entry<UUID, ChatType> entry : CHATS.entrySet()) {
                if (e.getPlayer().getUniqueId().equals(entry.getValue().uuid)) {
                    CHATS.remove(entry.getKey());
                }
            }

            CHATS.remove(e.getPlayer().getUniqueId());
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onChat(AsyncChatEvent e) {
            ChatType chat = CHATS.get(e.getPlayer().getUniqueId());
            if (chat != null) {
                e.setCancelled(true);

                String name = chat.uuid != null ? " "+Util.getName(Bukkit.getPlayer(chat.uuid)) : "";
                Bukkit.getScheduler().runTask(NautilusManager.INSTANCE, () -> e.getPlayer().performCommand("chatmsg "+chat.commandOption+name+" "+Util.getTextContent(e.originalMessage())));
            }
        }
    }
}
