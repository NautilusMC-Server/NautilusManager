package org.nautilusmc.nautilusmanager.cosmetics.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.cosmetics.Nickname;
import org.nautilusmc.nautilusmanager.util.Emoji;
import org.nautilusmc.nautilusmanager.util.ListDisplay;
import org.nautilusmc.nautilusmanager.util.Permission;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class NicknameCommand extends Command {
    private static final ListDisplay<OfflinePlayer> NICKNAME_LIST_DISPLAY = new ListDisplay<OfflinePlayer>("Nicknames")
            .setFormatter((player) -> {
                String username = Objects.requireNonNullElse(player.getName(), "(unknown player)");
                String nickname = Objects.requireNonNullElse(Nickname.getNickname(player), username);
                return Component.text(" - ")
                        .append(Component.text(username))
                        .append(Component.text(" " + Emoji.RIGHT + " "))
                        .append(player instanceof Player onlinePlayer ? onlinePlayer.displayName() : Component.text(nickname));
            });

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 1) return false;

        switch (args[0]) {
            case "list" -> {
                if (!sender.hasPermission(Permission.NICKNAME_LIST.toString())) {
                    sender.sendMessage(NO_PERMISSION_ERROR);
                    return true;
                }

                NICKNAME_LIST_DISPLAY.setList(Bukkit.getOnlinePlayers().stream()
                        .sorted(Comparator.comparing(Player::getName))
                        .map(player -> (OfflinePlayer) player) // the compiler is picky about this :(
                        .toList());
                sender.sendMessage(NICKNAME_LIST_DISPLAY.fetchPageContent(args.length < 2 ? null : args[1]));

            }
            case "nickname" -> {
                if (!sender.hasPermission(Permission.NICKNAME_LIST.toString())) {
                    sender.sendMessage(NO_PERMISSION_ERROR);
                    return true;
                }

                if (args.length < 2) return false;

                OfflinePlayer player = Nickname.getPlayerFromNickname(args[1]);
                if (player == null || player.getName() == null) {
                    sender.sendMessage(INVALID_PLAYER_ERROR);
                    return true;
                } else {
                    sender.sendMessage(NICKNAME_LIST_DISPLAY.getFormatter().apply(player).color(INFO_COLOR));
                }

            }
            case "player" -> {
                if (!sender.hasPermission(Permission.NICKNAME_LIST.toString())) {
                    sender.sendMessage(NO_PERMISSION_ERROR);
                    return true;
                }

                if (args.length < 2) return false;

                OfflinePlayer player = Util.getOfflinePlayerIfCached(args[1]);
                if (player == null || player.getName() == null) {
                    sender.sendMessage(INVALID_PLAYER_ERROR);
                    return true;
                } else {
                    sender.sendMessage(NICKNAME_LIST_DISPLAY.getFormatter().apply(player).color(INFO_COLOR));
                }

            }
            case "set", "clear" -> {
                Bukkit.dispatchCommand(sender, "cosmetics %s nickname %s %s".formatted(args[0], args.length > 1 ? args[1] : "", args.length > 2 ? args[2] : ""));
            }
            default -> {
                return false;
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> suggestionList(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> out = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission(Permission.NICKNAME_LIST.toString())) {
                out.add("list");
                out.add("player");
                out.add("nickname");
            }
            out.add("set");
            out.add("clear");
        } else if (args.length == 2) {
            if (sender.hasPermission(Permission.NICKNAME_LIST.toString())) {
                if (args[0].equalsIgnoreCase("player")) {
                    out.addAll(getOnlineUsernames());
                } else if (args[0].equalsIgnoreCase("nickname")) {
                    out.addAll(Nickname.getNicknames());
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set") && sender.hasPermission(Permission.MODIFY_OTHER_PLAYERS.toString())) {
                out.addAll(getOnlineUsernames());
            }
        }

        return out;
    }
}
