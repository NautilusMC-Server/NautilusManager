package org.nautilusmc.nautilusmanager.cosmetics.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
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
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class NicknameCommand extends NautilusCommand {

    private static final int PAGE_SIZE = 5;
    private static final TextColor COLOR = TextColor.color(255, 185, 21);

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length < 1) return false;

        Consumer<OfflinePlayer> sendNickname = p -> {
            String nick = Nickname.getNickname(p);
            commandSender.sendMessage(Component.text(p.getName()).append(Component.text(" → ").color(COLOR)).append(p instanceof Player pl ? pl.displayName() : Component.text(nick != null ? nick : p.getName())));
        };

        switch (strings[0]) {
            case "list": {
                if (!commandSender.hasPermission(NautilusCommand.NICKNAME_LIST_PERM)) {
                    commandSender.sendMessage(Component.text("You do not have permission to use this command").color(NautilusCommand.ERROR_COLOR));
                    return true;
                }

                int pageMax = (int) Math.ceil((double) Bukkit.getOnlinePlayers().size()/PAGE_SIZE);
                int page = Math.min(strings.length > 1 ? Integer.parseInt(strings[1]) : 1, pageMax);

                commandSender.sendMessage(Component.text("----- Nicknames (Page "+page+"/"+pageMax+") -----").color(COLOR).decorate(TextDecoration.BOLD));

                for (int i = 0; i < PAGE_SIZE; i++) {
                    int idx = (page-1)*PAGE_SIZE+i;
                    if (idx >= Bukkit.getOnlinePlayers().size()) break;

                    Player p = Bukkit.getOnlinePlayers().stream().sorted(Comparator.comparing(Player::getName)).toList().get(idx);
                    commandSender.sendMessage(Component.empty().append(Component.text(" - ").color(COLOR)).append(p.name()).append(Component.text(" → ").color(COLOR)).append(p.displayName()));
                }

                break;
            }
            case "nickname": {
                if (!commandSender.hasPermission(NautilusCommand.NICKNAME_LIST_PERM)) {
                    commandSender.sendMessage(Component.text("You do not have permission to use this command").color(NautilusCommand.ERROR_COLOR));
                    return true;
                }

                if (strings.length < 2) return false;

                OfflinePlayer p = Nickname.getPlayerFromNickname(strings[1]);
                if (p == null || p.getName() == null) {
                    commandSender.sendMessage(Component.text("Nickname not found").color(NautilusCommand.ERROR_COLOR));
                    return true;
                } else {
                    sendNickname.accept(p);
                }

                break;
            }
            case "player": {
                if (!commandSender.hasPermission(NautilusCommand.NICKNAME_LIST_PERM)) {
                    commandSender.sendMessage(Component.text("You do not have permission to use this command").color(NautilusCommand.ERROR_COLOR));
                    return true;
                }

                if (strings.length < 2) return false;

                OfflinePlayer p = Util.getOfflinePlayerIfCached(strings[1]);
                if (p == null || p.getName() == null) {
                    commandSender.sendMessage(Component.text("Player not found").color(NautilusCommand.ERROR_COLOR));
                    return true;
                } else {
                    sendNickname.accept(p);
                }

                break;
            }
            case "set":
            case "clear": {
                Bukkit.dispatchCommand(commandSender, "cosmetics %s nickname %s %s".formatted(strings[0], strings.length > 1 ? strings[1] : "", strings.length > 2 ? strings[2] : ""));

                break;
            }
            default: return false;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();

        if (strings.length == 1) {
            if (commandSender.hasPermission(NautilusCommand.NICKNAME_LIST_PERM)) {
                out.add("list");
                out.add("player");
                out.add("nickname");
            }
            out.add("set");
            out.add("clear");
        } else if (strings.length == 2) {
            if (commandSender.hasPermission(NautilusCommand.NICKNAME_LIST_PERM)) {
                if (strings[0].equalsIgnoreCase("player")) {
                    out.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
                } else if (strings[0].equalsIgnoreCase("nickname")) {
                    out.addAll(Nickname.getNicknames());
                }
            }
        } else if (strings.length == 3) {
            if (strings[0].equalsIgnoreCase("set") && commandSender.hasPermission(NautilusCommand.MODIFY_OTHER_PERM)) {
                out.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
            }
        }

        return out.stream().filter(str->str.toLowerCase().startsWith(strings[strings.length-1].toLowerCase())).toList();
    }
}
