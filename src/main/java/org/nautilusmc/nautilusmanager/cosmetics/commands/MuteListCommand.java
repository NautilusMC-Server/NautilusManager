package org.nautilusmc.nautilusmanager.cosmetics.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.cosmetics.MuteManager;
import org.nautilusmc.nautilusmanager.teleport.TpaManager;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.List;

public class MuteListCommand extends NautilusCommand {

    private static final int PAGE_SIZE = 10;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command.").color(NautilusCommand.ERROR_COLOR));
            return true;
        }
        if (!player.hasPermission(MUTE_PERM)) {
            player.sendMessage(Component.text("Not enough permissions!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        List<String> muted = MuteManager.getMuted(player).stream().map(u->Util.getName(Bukkit.getOfflinePlayer(u))).sorted().toList();

        if (muted.isEmpty()) {
            player.sendMessage(Component.text("You haven't muted anyone!").color(NautilusCommand.MAIN_COLOR));
            return true;
        }

        int pageMax = (int) Math.ceil((double) muted.size()/PAGE_SIZE);
        int page = Math.min(strings.length > 1 ? Integer.parseInt(strings[1]) : 1, pageMax);

        commandSender.sendMessage(Component.text("----- Muted (Page "+page+"/"+pageMax+") -----").color(MAIN_COLOR).decorate(TextDecoration.BOLD));

        for (int i = 0; i < PAGE_SIZE; i++) {
            int idx = (page-1)*PAGE_SIZE+i;
            if (idx >= muted.size()) break;

            String name = muted.get(idx);
            commandSender.sendMessage(Component.empty()
                    .append(Component.text(" - "))
                    .append(Component.text(name).color(ACCENT_COLOR))
                    .color(MAIN_COLOR));
        }


        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}
