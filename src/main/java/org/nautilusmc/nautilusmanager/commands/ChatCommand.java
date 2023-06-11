package org.nautilusmc.nautilusmanager.commands;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.util.Permission;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.*;

public class ChatCommand extends Command {
    public record ChatType(String commandOption, UUID uuid) {
        public static final ChatType STAFF = new ChatType("staff", null);
    }

    private static final Map<UUID, ChatType> CHATS = new HashMap<>();

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(NOT_PLAYER_ERROR);
            return true;
        }

        if (args.length < 1 || args[0].equalsIgnoreCase("all")) {
            player.sendMessage(Component.text("Now chatting with all players.").color(INFO_COLOR));
            CHATS.remove(player.getUniqueId());
            return true;

        } else if (args[0].equalsIgnoreCase("player")) {
            if (args.length < 2) return false;

            Player chat = Util.getOnlinePlayer(args[1]);

            if (chat == null) {
                sender.sendMessage(INVALID_PLAYER_ERROR);
                return true;
            }

            player.sendMessage(Component.text("Now chatting with ")
                    .append(Component.empty().append(chat.getPlayer().displayName()).color(INFO_ACCENT_COLOR))
                    .append(Component.text("."))
                    .color(INFO_COLOR));
            CHATS.put(player.getUniqueId(), new ChatType("player", chat.getUniqueId()));
            return true;

        } else if (args[0].equalsIgnoreCase("staff")) {
            if (!sender.hasPermission(Permission.STAFF_CHAT.toString())) {
                sender.sendMessage(NO_PERMISSION_ERROR);
                return true;
            }

            player.sendMessage(Component.text("Now chatting with staff.").color(INFO_COLOR));
            CHATS.put(player.getUniqueId(), ChatType.STAFF);
            return true;

        }

        return false;
    }

    @Override
    public @Nullable List<String> suggestionList(@NotNull CommandSender commandSender, @NotNull String[] strings) {
        // lazy but sure?
        return new ChatMsgCommand().suggestionList(commandSender, strings);
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

                String name = chat.uuid != null ? " " + Util.getName(Bukkit.getPlayer(chat.uuid)) : "";
                Bukkit.getScheduler().runTask(NautilusManager.INSTANCE, () -> {
                    e.getPlayer().performCommand("chatmsg " + chat.commandOption + name + " " + Util.getTextContent(e.originalMessage()));
                });
            }
        }
    }
}
