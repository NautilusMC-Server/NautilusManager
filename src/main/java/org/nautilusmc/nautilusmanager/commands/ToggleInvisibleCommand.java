package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import org.apache.commons.lang.WordUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ToggleInvisibleCommand extends NautilusCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        if (!player.hasPermission(ITEM_FRAME_ARMOR_STAND_INVISIBILITY_PERM)) {
            player.sendMessage(Component.text(SPONSOR_PERM_MESSAGE).color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        Entity lookingAt = player.getTargetEntity(5);

        boolean changed = false;
        boolean visible = false;

        if (lookingAt instanceof ItemFrame frame) {
            frame.setVisible(!frame.isVisible());
            visible = frame.isVisible();

            changed = true;
        } else if (lookingAt instanceof ArmorStand stand) {
            stand.setVisible(!stand.isVisible());
            visible = stand.isVisible();

            changed = true;
        }

        if (changed) {
            player.sendMessage(Component.text(WordUtils.capitalizeFully(lookingAt.getName().replace("_", " ")))
                    .append(Component.text(" is now "))
                    .append(Component.text(visible ? "visible" : "invisible").color(NautilusCommand.ACCENT_COLOR))
                    .append(Component.text("."))
                    .color(NautilusCommand.MAIN_COLOR));
        } else {
            player.sendMessage(Component.text("You must be looking at an item frame or armor stand").color(NautilusCommand.ERROR_COLOR));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}
