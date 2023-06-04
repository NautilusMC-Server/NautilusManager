package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.world.inventory.AnvilMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.util.FancyText;
import org.nautilusmc.nautilusmanager.util.Util;

public class ItemNameCommand extends NautilusCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(ErrorMessage.NOT_PLAYER);
            return true;
        }

        if (!player.hasPermission(Permission.SET_ITEM_NAME)) {
            player.sendMessage(ErrorMessage.NOT_SPONSOR);
            return true;
        }

        if (strings.length < 1) {
            // TODO: probably should just reset the item name lol
            player.sendMessage(Component.text("You must specify a name!").color(Default.ERROR_COLOR));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) item = player.getInventory().getItemInOffHand();

        if (item.getType().isAir()) {
            player.sendMessage(Component.text("You must be holding an item to use this command!").color(Default.ERROR_COLOR));
            return true;
        }

        String name = String.join(" ", strings);
        Component nameComponent = player.hasPermission(Permission.USE_CHAT_FORMATTING) ? Component.empty()
                .decoration(TextDecoration.ITALIC, false)
                .append(FancyText.parseChatFormatting("``italic" + name)) : Component.text(name);

        if (Util.getTextContent(nameComponent).length() > AnvilMenu.MAX_NAME_LENGTH) {
            player.sendMessage(Component.text("The name you specified is too long!").color(Default.ERROR_COLOR));
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        meta.displayName(nameComponent);
        item.setItemMeta(meta);

        return true;
    }
}
