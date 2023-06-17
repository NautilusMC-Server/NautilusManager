package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.util.ListDisplay;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.*;

public class TimeTopCommand extends Command {
    private static final ListDisplay<Map.Entry<OfflinePlayer, Util.TimeAmount>> TIME_PLAYED_LIST_DISPLAY =
            new ListDisplay<Map.Entry<OfflinePlayer, Util.TimeAmount>>("Time Played")
                    .setFormatter(entry -> Component.empty()
                            .append(Component.text(Util.getName(entry.getKey()), INFO_ACCENT_COLOR))
                            .append(Component.text(" - "))
                            .append(Component.text(entry.getValue().toHoursMinutes(), INFO_ACCENT_COLOR)));

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        Map<OfflinePlayer, Util.TimeAmount> nameToTimePlayed = new HashMap<>();
        int totalTicks = 0;
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            int ticksPlayed = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
            nameToTimePlayed.put(player, new Util.TimeAmount(ticksPlayed));
            totalTicks += ticksPlayed;
        }

        sender.sendMessage(Component.text("Total time played: ", INFO_COLOR)
                .append(Component.text(new Util.TimeAmount(totalTicks).toHoursMinutes(), INFO_ACCENT_COLOR)));

        List<Map.Entry<OfflinePlayer, Util.TimeAmount>> leaderboard = nameToTimePlayed.entrySet().stream()
                .sorted(Map.Entry.<OfflinePlayer, Util.TimeAmount>comparingByValue().reversed())
                .toList();

        TIME_PLAYED_LIST_DISPLAY.setList(leaderboard);
        sender.sendMessage(TIME_PLAYED_LIST_DISPLAY.fetchPageContent(args.length >= 1 ? args[0] : null));

        return true;
    }
}
