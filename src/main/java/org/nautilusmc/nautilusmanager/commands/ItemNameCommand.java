package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.world.inventory.AnvilMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.util.FancyText;
import org.nautilusmc.nautilusmanager.util.Permission;
import org.nautilusmc.nautilusmanager.util.Util;

public class ItemNameCommand extends Command {
    public static final Component NO_HELD_ITEM_ERROR = Component.text("You must be holding an item to use this command!").color(ERROR_COLOR);
    public static final Component NAME_TOO_LONG_ERROR = Component.text("The name you specified is too long!").color(ERROR_COLOR);

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Command.NOT_PLAYER_ERROR);
            return true;
        }

        if (!player.hasPermission(Permission.SET_ITEM_NAME.toString())) {
            player.sendMessage(Command.NOT_SPONSOR_ERROR);
            return true;
        }

        if (args.length < 1) {
            // TODO: probably should just reset the item name lol
            player.sendMessage(Component.text("You must specify a name!").color(ERROR_COLOR));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) item = player.getInventory().getItemInOffHand();

        if (item.getType().isAir()) {
            player.sendMessage(NO_HELD_ITEM_ERROR);
            return true;
        }

        String name = getMessageFromArgs(args, 0);
        Component nameComponent = player.hasPermission(Permission.USE_CHAT_FORMATTING.toString()) ? Component.empty()
                .decoration(TextDecoration.ITALIC, false)
                .append(FancyText.parseChatFormatting("``italic" + name)) : Component.text(name);

        if (Util.getTextContent(nameComponent).length() > AnvilMenu.MAX_NAME_LENGTH) {
            player.sendMessage(NAME_TOO_LONG_ERROR);
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        meta.displayName(nameComponent);
        item.setItemMeta(meta);

        return true;
    }
}
