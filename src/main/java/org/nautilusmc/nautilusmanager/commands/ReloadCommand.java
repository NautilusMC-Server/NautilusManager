package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.lenni0451.spm.PluginManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.NautilusManager;

public class ReloadCommand extends NautilusCommand {
    public static final int RELOAD_DELAY_SECONDS = 15;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!commandSender.hasPermission(Permission.RELOAD)) {
            commandSender.sendMessage(ErrorMessage.NO_PERMISSION);
            return true;
        }

        int reloadTime = 15; // in seconds

        // not putting this in tab complete because it should never be done on the production server
        if (strings.length > 0 && strings[0].equals("now")) {
            reloadTime = 0;
        }

        Bukkit.broadcast(Component.text("Reloading the plugin in "+reloadTime+" seconds to update. Server will stay online, but /reply and /back will reset.").color(TextColor.color(255, 0, 15)));
        Bukkit.getScheduler().runTaskLater(PluginManager.getInstance(), ()-> {
            Bukkit.broadcast(Component.text("Reloading...").color(TextColor.color(255, 85, 60)));
            NautilusManager.unload();
            PluginManager.getInstance().getPluginUtils().reloadPlugin(NautilusManager.INSTANCE);
            Bukkit.broadcast(Component.text("Done!").color(TextColor.color(255, 85, 60)));
        }, reloadTime * 20L);
        return true;
    }
}
