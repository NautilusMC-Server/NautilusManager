package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.lenni0451.spm.PluginManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.NautilusManager;

import java.util.ArrayList;
import java.util.List;

public class ReloadCommand extends NautilusCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!commandSender.hasPermission(NautilusCommand.RELOAD_PERM)) {
            commandSender.sendMessage(Component.text("You do not have permission to use this command!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        Bukkit.broadcast(Component.text("Reloading the plugin in 15 seconds to update. Server will stay online, but /reply and /back will reset.").color(TextColor.color(255, 0, 15)));
        Bukkit.getScheduler().runTaskLater(PluginManager.getInstance(), ()-> {
            Bukkit.broadcast(Component.text("Reloading...").color(TextColor.color(255, 85, 60)));
            PluginManager.getInstance().getPluginUtils().reloadPlugin(NautilusManager.INSTANCE);
            Bukkit.broadcast(Component.text("Done!").color(TextColor.color(255, 85, 60)));
        }, 15 * 20L);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}
