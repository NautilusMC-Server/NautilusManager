package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Command implements CommandExecutor, TabCompleter {
    public static final TextColor DEFAULT_COLOR = TextColor.color(200, 200, 200);
    public static final TextColor INFO_COLOR = TextColor.color(255, 188, 0);
    public static final TextColor INFO_ACCENT_COLOR = TextColor.color(255, 252, 162);
    public static final TextColor ERROR_COLOR = TextColor.color(255, 42, 52);
    public static final TextColor ERROR_ACCENT_COLOR = TextColor.color(255, 123, 130);

    public static final Component NOT_PLAYER_ERROR = Component.text("You must be a player to use that command!").color(ERROR_COLOR);
    public static final Component NO_PERMISSION_ERROR = Component.text("You do not have permission to use that command!").color(ERROR_COLOR);
    public static final Component NOT_SPONSOR_ERROR = Component.text("Become a sponsor to unlock that command! (")
            .append(Util.clickableCommand("/sponsor", true).color(ERROR_ACCENT_COLOR))
            .append(Component.text(")"))
            .color(ERROR_COLOR);
    public static final Component INVALID_PLAYER_ERROR = Component.text("Player not found!").color(ERROR_COLOR);

    protected static String getMessageFromArgs(String[] args, int start) {
        return start >= args.length ? "" : String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }

    public static List<String> getOnlineUsernames() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }

    public static List<String> getOnlineNames() {
        return Bukkit.getOnlinePlayers().stream().map(Util::getName).toList();
    }

    public static List<String> getOfflineNames() {
        return Arrays.stream(Bukkit.getOfflinePlayers()).map(Util::getName).toList();
    }

    public abstract boolean execute(@NotNull CommandSender sender, @NotNull String[] args);

    public @Nullable List<String> suggestionList(@NotNull CommandSender sender, @NotNull String[] args) {
        return new ArrayList<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String alias, @NotNull String[] args) {
        // TODO: handle some stuff automatically, e.g. permissions?
        return execute(sender, args);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> out = suggestionList(sender, args);
        return out == null ? null : out.stream()
                .filter(str -> StringUtil.startsWithIgnoreCase(str, args[args.length - 1]))
                .toList();
    }
}
