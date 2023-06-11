package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import org.apache.commons.lang.WordUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.util.Permission;

import java.util.ArrayList;
import java.util.List;

public class ToggleInvisibleCommand extends Command {
    public static final Component INVALID_TARGET_ERROR = Component.text("You must be looking at an item frame or armor stand to use this command!").color(ERROR_COLOR);

    public static final int MAX_TARGET_DISTANCE_BLOCKS = 5;

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(NOT_PLAYER_ERROR);
            return true;
        }

        if (!player.hasPermission(Permission.INVISIBLE_FRAMES_STANDS.toString())) {
            player.sendMessage(NOT_SPONSOR_ERROR);
            return true;
        }

        Entity target = player.getTargetEntity(MAX_TARGET_DISTANCE_BLOCKS);
        boolean visible;

        if (target instanceof ItemFrame frame) {
            frame.setVisible(!frame.isVisible());
            visible = frame.isVisible();
        } else if (target instanceof ArmorStand stand) {
            stand.setVisible(!stand.isVisible());
            visible = stand.isVisible();
        } else {
            player.sendMessage(INVALID_TARGET_ERROR);
            return true;
        }

        player.sendMessage(Component.text(WordUtils.capitalizeFully(target.getName().replace("_", " ")))
                .append(Component.text(" is now "))
                .append(Component.text(visible ? "visible" : "invisible").color(INFO_ACCENT_COLOR))
                .append(Component.text("."))
                .color(INFO_COLOR));

        return true;
    }
}
