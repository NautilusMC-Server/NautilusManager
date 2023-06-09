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
import org.nautilusmc.nautilusmanager.util.Permission;

public class BuyHomeCommand extends Command {
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(NOT_PLAYER_ERROR);
            return true;
        }

        if (!player.hasPermission(Permission.USE_HOMES.toString())) {
            player.sendMessage(NO_PERMISSION_ERROR);
            return true;
        }

        int maxHomes = Homes.getMaxHomes(player);
        int maxHomesLimit = NautilusManager.getPlugin().getConfig().getInt("homes.maxAmount");

        if (maxHomes >= maxHomesLimit) {
            player.sendMessage(Component.text("Home limit reached!")
                    .appendNewline()
                    .append(Component.text("You cannot have more than "))
                    .append(Component.text(maxHomesLimit).color(ERROR_ACCENT_COLOR))
                    .append(Component.text(" homes."))
                    .color(ERROR_COLOR));
            return true;
        }

        int diamondBlockCost = maxHomes + 1;

        if (!player.getInventory().contains(Material.DIAMOND_BLOCK, diamondBlockCost)) {
            player.sendMessage(Component.text("Not enough diamond blocks!")
                    .appendNewline()
                    .append(Component.text("You need "))
                    .append(Component.text(diamondBlockCost).color(ERROR_ACCENT_COLOR))
                    .append(Component.text(" diamond blocks to buy the next home."))
                    .color(ERROR_COLOR));
            return true;
        }
        player.getInventory().removeItemAnySlot(new ItemStack(Material.DIAMOND_BLOCK, diamondBlockCost));

        maxHomes++;
        Homes.setMaxHomes(player, maxHomes);
        player.sendMessage(Component.text("Home limit increased to ")
                .append(Component.text(maxHomes).color(INFO_ACCENT_COLOR))
                .append(Component.text(" homes."))
                .color(INFO_COLOR));

        return true;
    }
}