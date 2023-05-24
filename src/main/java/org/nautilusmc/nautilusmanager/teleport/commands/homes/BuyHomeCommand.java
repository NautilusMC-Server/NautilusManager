package org.nautilusmc.nautilusmanager.teleport.commands.homes;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.teleport.Homes;

public class BuyHomeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        if (!player.hasPermission(NautilusCommand.HOMES_PERM)) {
            player.sendMessage(Component.text("Not enough permissions!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        int limit = Homes.getMaxHomes(player);

        if (limit >= NautilusManager.INSTANCE.getConfig().getInt("homes.maxAmount")) {
            player.sendMessage(Component.text("Home limit reached!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        ItemStack diamondBlockCost = new ItemStack(Material.DIAMOND_BLOCK, limit + 1);

        if (!player.getInventory().containsAtLeast(diamondBlockCost, diamondBlockCost.getAmount())) {
            player.sendMessage(Component.text("Not enough diamond blocks!").color(NautilusCommand.ERROR_COLOR));
            player.sendMessage(Component.text("Next home cost: " + (limit + 1) + " diamond blocks").color(NautilusCommand.ERROR_COLOR));
            return true;
        }
        player.getInventory().removeItemAnySlot(diamondBlockCost);

        Homes.setMaxHomes(player, limit+1);
        player.sendMessage(Component.text("Home limit set to ")
                .append(Component.text(limit + 1).color(NautilusCommand.ACCENT_COLOR))
                .append(Component.text(" homes"))
                .color(NautilusCommand.MAIN_COLOR));
        return true;
    }
}