package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.events.MessageStyler;
import org.nautilusmc.nautilusmanager.util.Permission;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.*;

public class BanCommand extends Command {
    public static final Component INVALID_EXPIRY_ERROR = Component.text("Invalid ban expiry time!").color(ERROR_COLOR);

    private static final Map<String, Integer> UNIT_TO_CALENDAR_FIELD = Map.of(
            "y", Calendar.YEAR,
            "mo", Calendar.MONTH,
            "w", Calendar.WEEK_OF_YEAR,
            "d", Calendar.DAY_OF_MONTH,
            "h", Calendar.HOUR_OF_DAY,
            "m", Calendar.MINUTE,
            "s", Calendar.SECOND
    );

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission(Permission.BAN.toString())) {
            sender.sendMessage(NO_PERMISSION_ERROR);
            return true;
        }

        Date expiry = null;
        if (args.length > 1 && !args[1].equalsIgnoreCase("infinite")) {
            try {
                int expiryLength = Integer.parseInt(args[1].replaceFirst("(\\d+).*", "$1"));
                String expiryUnit = args[1].replaceFirst("\\d+(.*)", "$1").toLowerCase();

                Calendar c = Util.getCalendar();
                c.setTime(new Date());
                c.add(UNIT_TO_CALENDAR_FIELD.get(expiryUnit), expiryLength);
                expiry = c.getTime();
            } catch (IllegalArgumentException ignored) {
                sender.sendMessage(INVALID_EXPIRY_ERROR);
                return true;
            }
        }

        String reason = getMessageFromArgs(args, 2);
        if (reason.isBlank()) {
            reason = "Banned by an operator";
        }

        String source = sender instanceof ConsoleCommandSender ? "Server" : sender.getName();

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        BanEntry entry = Bukkit.getBanList(BanList.Type.NAME).addBan(target.getUniqueId().toString(), reason, expiry, source);
        if (target instanceof Player onlineTarget) {
            onlineTarget.kick(MessageStyler.getBanMessage(entry));
        }

        sender.sendMessage(Component.text("Successfully banned ")
                .append(Component.text(Util.getName(target)).color(INFO_ACCENT_COLOR))
                .append(Component.text(expiry == null ? "" : " until "))
                .append(Component.text(expiry == null ? " indefinitely" : MessageStyler.DATE_FORMAT.format(expiry)).color(INFO_ACCENT_COLOR))
                .append(Component.text(": "))
                .append(Component.text(reason).color(INFO_ACCENT_COLOR))
                .color(INFO_COLOR));
        return true;
    }

    @Override
    public @Nullable List<String> suggestionList(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> out = new ArrayList<>();

        if (!sender.hasPermission(Permission.BAN.toString())) return out;

        if (args.length == 1) {
            out.addAll(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).toList());
        } else if (args.length == 2) {
            out.add("infinite");

            String value = args[1].isEmpty() ? "1" : args[1].replaceFirst("(\\d*).*", "$1");
            out.addAll(UNIT_TO_CALENDAR_FIELD.keySet().stream().map(unit -> value + unit).toList());
        }

        return out;
    }
}
