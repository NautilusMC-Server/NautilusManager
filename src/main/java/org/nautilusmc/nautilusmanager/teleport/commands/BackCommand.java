package org.nautilusmc.nautilusmanager.teleport.commands;

import com.earth2me.essentials.UserData;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.events.TeleportHandler;

import java.util.List;

public class BackCommand extends NautilusCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        Location lastLoc = TeleportHandler.getLastTeleportLocation(player);
        if (lastLoc == null) {
            commandSender.sendMessage(Component.text("Nowhere to return to!").color(NautilusCommand.ERROR_COLOR));
        }

        TeleportHandler.teleportAfterDelay(player, lastLoc, 100);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }
}
