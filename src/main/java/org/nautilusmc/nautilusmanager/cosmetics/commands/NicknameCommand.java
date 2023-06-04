package org.nautilusmc.nautilusmanager.cosmetics.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.cosmetics.Nickname;
import org.nautilusmc.nautilusmanager.util.Emoji;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class NicknameCommand extends NautilusCommand {

    private static final int PAGE_SIZE = 5;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length < 1) return false;

        Function<OfflinePlayer, Component> generateNicknameListing = player -> {
            String username = Objects.requireNonNullElse(player.getName(), "(unknown player)");
            String nickname = Objects.requireNonNullElse(Nickname.getNickname(player), username);
            return Component.text(username)
                    .append(Component.text(" " + Emoji.RIGHT.getRaw() + " "))
                    .append(player instanceof Player onlinePlayer ? onlinePlayer.displayName() : Component.text(nickname));
        };

        switch (strings[0]) {
            case "list" -> {
                if (!commandSender.hasPermission(Permission.NICKNAME_LIST)) {
                    commandSender.sendMessage(ErrorMessage.NO_PERMISSION);
                    return true;
                }

                try {
                    int onlinePlayerCount = Bukkit.getOnlinePlayers().size();
                    int pageCount = (int) Math.ceil((double) onlinePlayerCount / PAGE_SIZE);
                    int page = Math.max(Math.min(strings.length > 1 ? Integer.parseInt(strings[1]) : 1, pageCount), 1);

                    commandSender.sendMessage(Component.text("----- Nicknames (Page " + page + "/" + pageCount + ") -----").color(Default.INFO_COLOR).decorate(TextDecoration.BOLD));

                    List<? extends Player> playerList = Bukkit.getOnlinePlayers().stream().sorted(Comparator.comparing(Player::getName)).toList();
                    for (int i = 0; i < PAGE_SIZE; i++) {
                        int playerIndex = (page - 1) * PAGE_SIZE + i;
                        if (playerIndex >= onlinePlayerCount) break;
                        commandSender.sendMessage(Component.text(" - ").append(generateNicknameListing.apply(playerList.get(playerIndex))).color(Default.INFO_COLOR));
                    }
                } catch (NumberFormatException e) {
                    commandSender.sendMessage(ErrorMessage.INVALID_PAGE_NUMBER);
                }

            }
            case "nickname" -> {
                if (!commandSender.hasPermission(Permission.NICKNAME_LIST)) {
                    commandSender.sendMessage(ErrorMessage.NO_PERMISSION);
                    return true;
                }

                if (strings.length < 2) return false;

                OfflinePlayer player = Nickname.getPlayerFromNickname(strings[1]);
                if (player == null || player.getName() == null) {
                    commandSender.sendMessage(ErrorMessage.INVALID_PLAYER);
                    return true;
                } else {
                    commandSender.sendMessage(generateNicknameListing.apply(player).color(Default.INFO_COLOR));
                }

            }
            case "player" -> {
                if (!commandSender.hasPermission(Permission.NICKNAME_LIST)) {
                    commandSender.sendMessage(ErrorMessage.NO_PERMISSION);
                    return true;
                }

                if (strings.length < 2) return false;

                OfflinePlayer player = Util.getOfflinePlayerIfCached(strings[1]);
                if (player == null || player.getName() == null) {
                    commandSender.sendMessage(ErrorMessage.INVALID_PLAYER);
                    return true;
                } else {
                    commandSender.sendMessage(generateNicknameListing.apply(player).color(Default.INFO_COLOR));
                }

            }
            case "set", "clear" -> {
                Bukkit.dispatchCommand(commandSender, "cosmetics %s nickname %s %s".formatted(strings[0], strings.length > 1 ? strings[1] : "", strings.length > 2 ? strings[2] : ""));
            }
            default -> {
                return false;
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();

        if (strings.length == 1) {
            if (commandSender.hasPermission(Permission.NICKNAME_LIST)) {
                out.add("list");
                out.add("player");
                out.add("nickname");
            }
            out.add("set");
            out.add("clear");
        } else if (strings.length == 2) {
            if (commandSender.hasPermission(Permission.NICKNAME_LIST)) {
                if (strings[0].equalsIgnoreCase("player")) {
                    out.addAll(getOnlineUsernames());
                } else if (strings[0].equalsIgnoreCase("nickname")) {
                    out.addAll(Nickname.getNicknames());
                }
            }
        } else if (strings.length == 3) {
            if (strings[0].equalsIgnoreCase("set") && commandSender.hasPermission(Permission.MODIFY_OTHER_PLAYERS)) {
                out.addAll(getOnlineUsernames());
            }
        }

        return out.stream().filter(str -> str.toLowerCase().startsWith(strings[strings.length - 1].toLowerCase())).toList();
    }
}
