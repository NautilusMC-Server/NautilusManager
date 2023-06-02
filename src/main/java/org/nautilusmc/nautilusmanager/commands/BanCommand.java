package org.nautilusmc.nautilusmanager.commands;

import joptsimple.internal.Strings;
import net.kyori.adventure.text.Component;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.events.MessageStyler;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.*;

public class BanCommand extends NautilusCommand {

    private static final Map<String, Integer> CALENDAR_FIELDS = Map.of(
            "y", Calendar.YEAR,
            "mo", Calendar.MONTH,
            "w", Calendar.WEEK_OF_YEAR,
            "d", Calendar.DAY_OF_MONTH,
            "h", Calendar.HOUR_OF_DAY,
            "m", Calendar.MINUTE,
            "s", Calendar.SECOND
    );

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!commandSender.hasPermission(BAN_PERM)) {
            commandSender.sendMessage(Component.text("Not enough permissions").color(ERROR_COLOR));
            return true;
        }
        if (strings.length < 2) return false;

        Date expiry;

        if (strings[1].equals("infinite")) {
            expiry = null;
        } else {
            try {
                int expiryLength = Integer.parseInt(strings[1].replaceFirst("(\\d+).*", "$1"));
                String expiryUnit = strings[1].replaceFirst("\\d+(.*)", "$1");

                Calendar c = Util.getCalendar();
                c.setTime(new Date());
                c.add(CALENDAR_FIELDS.get(expiryUnit), expiryLength);
                expiry = c.getTime();
            } catch (IllegalArgumentException ignored) {
                commandSender.sendMessage(Component.text("Invalid expiry time").color(ERROR_COLOR));
                return true;
            }
        }

        String reason = Strings.join(Arrays.copyOfRange(strings, 2, strings.length), " ");
        if (reason.isBlank()) {
            reason = "Banned by an operator";
        }

        String source = commandSender instanceof ConsoleCommandSender ? "Server" : commandSender.getName();

        OfflinePlayer target = Bukkit.getOfflinePlayer(strings[0]);

        BanEntry entry = Bukkit.getBanList(BanList.Type.NAME).addBan(target.getUniqueId().toString(), reason, expiry, source);
        if (target.isOnline()) target.getPlayer().kick(MessageStyler.getBanMessage(entry));

        commandSender.sendMessage(Component.text("Successfully banned ")
                .append(Component.text(strings[0]).color(ACCENT_COLOR))
                .append(Component.text(expiry == null ? "" : " until "))
                .append(Component.text(expiry == null ? " indefinitely" : MessageStyler.DATE_FORMAT.format(expiry))
                        .color(ACCENT_COLOR))
                .append(Component.text(": "))
                .append(Component.text(reason).color(ACCENT_COLOR))
                .color(MAIN_COLOR));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();
        if (!commandSender.hasPermission(BAN_PERM)) return out;

        if (strings.length == 1) {
            out.addAll(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).toList());
        } else if (strings.length == 2) {
            out.add("infinite");

            String str = strings[1].isEmpty() ? "1" : strings[1].replaceFirst("(\\d*).*", "$1");
            out.addAll(CALENDAR_FIELDS.keySet().stream().map(u->str+u).toList());
        }

        return out.stream().filter(str->str.toLowerCase().startsWith(strings[strings.length-1].toLowerCase())).toList();
    }
}
