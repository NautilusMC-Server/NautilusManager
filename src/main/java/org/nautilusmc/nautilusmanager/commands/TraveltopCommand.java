package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TraveltopCommand extends NautilusCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(!(commandSender instanceof Player player)) {
            commandSender.sendMessage("You must be a player to use this command!");
            return true;
        }

        HashMap<Long, String> map = new HashMap<>(); //kilometers, player name
        long cm;
        double total = 0;
        for(OfflinePlayer p : Bukkit.getServer().getOfflinePlayers()) {
            cm = p.getStatistic(Statistic.WALK_ONE_CM) + p.getStatistic(Statistic.SWIM_ONE_CM)
                    + p.getStatistic(Statistic.SPRINT_ONE_CM) + p.getStatistic(Statistic.CROUCH_ONE_CM)
                    + p.getStatistic(Statistic.WALK_ON_WATER_ONE_CM) + p.getStatistic(Statistic.AVIATE_ONE_CM)
                    + p.getStatistic(Statistic.WALK_UNDER_WATER_ONE_CM) + p.getStatistic(Statistic.MINECART_ONE_CM)
                    + p.getStatistic(Statistic.BOAT_ONE_CM) + p.getStatistic(Statistic.HORSE_ONE_CM)
                    + p.getStatistic(Statistic.STRIDER_ONE_CM) + p.getStatistic(Statistic.PIG_ONE_CM);
            double km = Math.round((float) cm / 100000 * 100) / 100.0;
            map.put(cm, p.getName());
            total += km;
        }
        ArrayList<Long> list = new ArrayList<>(map.keySet());
        Collections.sort(list);
        Collections.reverse(list);

        int page = 0;
        if(strings.length > 0)
            try {
                page = Math.abs(Integer.parseInt(strings[0]));
            } catch (NumberFormatException ignored) {}
        if(page > ((list.size() - 1) / 10) + 1) {
            player.sendMessage(Component.text("There aren't yet " + page + " pages of this " +
                    (((list.size() - 1) / 10) + 1) + "-page leaderboard!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }
        if(page == 0) page = 1;
        int index = (page - 1) * 10;

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&2&lLeaderboard:&r&2 Distance Traveled " +
                "&7(page " + page + " of " + (((list.size() - 1) / 10) + 1) + ")\n"));
        DecimalFormat d = new DecimalFormat("###,###,###,###.##");
        if(page == 1) player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7&oServer total: " + d.format(total) + " km"));
        for(int i = index; i < index + 10; i++) {
            if(i > list.size() - 1) return true;
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&a" + (i + 1) + ". &7" + map.get(list.get(i)) + "&8 - &f" + d.format((Math.round((float) list.get(i) / 100000 * 100) / 100.0)) + " km"));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
