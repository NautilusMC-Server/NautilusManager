package org.nautilusmc.nautilusmanager.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TimetopCommand extends NautilusCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(!(commandSender instanceof Player player)) {
            commandSender.sendMessage("You must be a player to use this command!");
            return true;
        }

        HashMap<Long, String> map = new HashMap<>(); //time played, player name
        long time;
        long total = 0L;
        for(OfflinePlayer p : Bukkit.getOfflinePlayers()) {
            time = p.getStatistic(Statistic.PLAY_ONE_MINUTE) * 1000L / 20;
            map.put(time, p.getName());
            total += time;
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
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cThere aren't yet " + page + " pages of this " +
                    (((list.size() - 1) / 10) + 1) + "-page leaderboard!"));
            return true;
        }
        if(page == 0) page = 1;
        int index = (page - 1) * 10;

        player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&2&lLeaderboard:&r&2 Time Played " +
                "&7(page " + page + " of " + (((list.size() - 1) / 10) + 1) + ")\n"));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&7&oServer total: " + toHours(total)));
        for(int i = index; i < index + 10; i++) {
            if(i > list.size() - 1) return true;
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&a" + (i + 1) + ". &f" + map.get(list.get(i)) + ": " + toHours(list.get(i))));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }

    private String toHours(long time) {
        String str = time / (1000D * 60 * 60) + "";
        return str.split("\\.")[0] + "." + str.split("\\.")[1].substring(0, 2) + " hours";
    }
}
