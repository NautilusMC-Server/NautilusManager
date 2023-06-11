package org.nautilusmc.nautilusmanager.teleport.commands.homes;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.teleport.Homes;

public class BuyHomeCommand extends Command {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Command.NOT_PLAYER_ERROR);
            return true;
        }

        if (!player.hasPermission(Permission.USE_HOMES)) {
            player.sendMessage(Command.NO_PERMISSION_ERROR);
            return true;
        }

        int maxHomes = Homes.getMaxHomes(player);
        int maxHomesLimit = NautilusManager.INSTANCE.getConfig().getInt("homes.maxAmount");

        if (maxHomes >= maxHomesLimit) {
            player.sendMessage(Component.text("Home limit reached!").color(Default.ERROR_COLOR));
            player.sendMessage(Component.text("You cannot have more than ")
                    .append(Component.text(maxHomesLimit).color(Default.ERROR_ACCENT_COLOR))
                    .append(Component.text(" homes."))
                    .color(Default.ERROR_COLOR));
            return true;
        }

        int diamondBlockCost = maxHomes + 1;

        if (!player.getInventory().contains(Material.DIAMOND_BLOCK, diamondBlockCost)) {
            player.sendMessage(Component.text("Not enough diamond blocks!").color(Default.ERROR_COLOR));
            player.sendMessage(Component.text("You need ")
                    .append(Component.text(diamondBlockCost).color(Default.ERROR_ACCENT_COLOR))
                    .append(Component.text(" diamond blocks to buy the next home."))
                    .color(Default.ERROR_COLOR));
            return true;
        }
        player.getInventory().removeItemAnySlot(new ItemStack(Material.DIAMOND_BLOCK, diamondBlockCost));

        maxHomes++;
        Homes.setMaxHomes(player, maxHomes);
        player.sendMessage(Component.text("Home limit increased to ")
                .append(Component.text(maxHomes).color(Default.INFO_ACCENT_COLOR))
                .append(Component.text(" homes."))
                .color(Default.INFO_COLOR));
        return true;
    }
}