package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.util.ListDisplay;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TravelTopCommand extends Command {
    public static final Statistic[] DISTANCE_STATISTICS = {
            Statistic.WALK_ONE_CM,
            Statistic.SWIM_ONE_CM,
            Statistic.SPRINT_ONE_CM,
            Statistic.CROUCH_ONE_CM,
            Statistic.WALK_ON_WATER_ONE_CM,
            Statistic.AVIATE_ONE_CM,
            Statistic.WALK_UNDER_WATER_ONE_CM,
            Statistic.MINECART_ONE_CM,
            Statistic.BOAT_ONE_CM,
            Statistic.HORSE_ONE_CM,
            Statistic.STRIDER_ONE_CM,
            Statistic.PIG_ONE_CM,
    };

    private static final ListDisplay<Map.Entry<OfflinePlayer, Double>> DISTANCE_TRAVELED_LIST_DISPLAY =
            new ListDisplay<Map.Entry<OfflinePlayer, Double>>("Distance Traveled")
                    .setFormatter(entry -> Component.empty()
                            .append(Component.text(Util.getName(entry.getKey()), INFO_ACCENT_COLOR))
                            .append(Component.text(" - "))
                            .append(Component.text(formatDistance(entry.getValue()), INFO_ACCENT_COLOR)));

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        Map<OfflinePlayer, Double> nameToKmTraveled = new HashMap<>();
        double totalKm = 0;
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            double kmTraveled = 0;
            for (Statistic statistic : DISTANCE_STATISTICS) {
                kmTraveled += player.getStatistic(statistic) * 0.001;
            }
            nameToKmTraveled.put(player, kmTraveled);
            totalKm += kmTraveled;
        }

        sender.sendMessage(Component.text("Total distance traveled: ", INFO_COLOR)
                .append(Component.text(formatDistance(totalKm), INFO_ACCENT_COLOR)));

        List<Map.Entry<OfflinePlayer, Double>> leaderboard = nameToKmTraveled.entrySet().stream()
                .sorted(Map.Entry.<OfflinePlayer, Double>comparingByValue().reversed())
                .toList();

        DISTANCE_TRAVELED_LIST_DISPLAY.setList(leaderboard);
        sender.sendMessage(DISTANCE_TRAVELED_LIST_DISPLAY.fetchPageContent(args.length >= 1 ? args[0] : null));

        return true;
    }

    public static String formatDistance(double km) {
        return "%.1f km".formatted(km);
    }
}
