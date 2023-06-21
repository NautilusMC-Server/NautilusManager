package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import net.lenni0451.spm.PluginManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.util.Permission;

public class ReloadCommand extends Command {
    public static final int RELOAD_DELAY_SECONDS = 15;

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission(Permission.RELOAD.toString())) {
            sender.sendMessage(Command.NO_PERMISSION_ERROR);
            return true;
        }

        // not putting this in tab complete because it should never be done on the production server
        boolean reloadNow = args.length > 0 && args[0].equalsIgnoreCase("now");

        if (!reloadNow) {
            Bukkit.broadcast(Component.text("Reloading the plugin in ")
                    .append(Component.text(RELOAD_DELAY_SECONDS).color(ERROR_ACCENT_COLOR))
                    .append(Component.text(" seconds to update. Server will stay online, but /reply and /back will reset."))
                    .color(ERROR_COLOR));
        }

        Bukkit.getScheduler().runTaskLater(PluginManager.getInstance(), () -> {
            Bukkit.broadcast(Component.text("Reloading...").color(ERROR_COLOR));
            NautilusManager.unload();
            PluginManager.getInstance().getPluginUtils().reloadPlugin(NautilusManager.INSTANCE);
            Bukkit.broadcast(Component.text("Done!").color(ERROR_COLOR));
        }, reloadNow ? 0L : RELOAD_DELAY_SECONDS * 20L);

        return true;
    }
}
