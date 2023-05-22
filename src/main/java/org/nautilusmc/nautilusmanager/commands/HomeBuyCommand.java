package org.nautilusmc.nautilusmanager.commands;

import com.google.common.collect.BiMap;
import net.kyori.adventure.text.Component;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.util.LuckPermsUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeBuyCommand implements CommandExecutor {

    private static HashMap<Integer, Node> homePermissions = new HashMap<Integer, Node>();
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            return false;
        }

        Player player = (Player) commandSender;
        int limit = getHomeLimit(player);
        if (limit == 0) {
            LuckPermsUtil.addPermission(player, "essentials.sethome.multiple.two");
            limit = 2;
        }
        if (limit >= 15) {
            player.sendMessage(Component.text("Home limit reached!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }
        ItemStack diamondBlockCost = new ItemStack(Material.DIAMOND_BLOCK, limit + 1);
        if (!player.getInventory().contains(diamondBlockCost)) {
            player.sendMessage(Component.text("Not enough diamond blocks!").color(NautilusCommand.ERROR_COLOR));
            player.sendMessage(Component.text("Next home cost: " + (limit + 1) + " diamond blocks").color(NautilusCommand.ERROR_COLOR));
            return true;
        }
        player.getInventory().remove(diamondBlockCost);
        incrementHomeLimit(player);
        player.sendMessage(Component.text("Home limit set to " + (limit + 1) + "homes").color(NautilusCommand.DEFAULT_CHAT_TEXT_COLOR));
        return true;
    }

    public static void init() {
        int i = 1;
        homePermissions.put(i++, PermissionNode.builder().permission("essentials.sethome.multiple.one").build());
        homePermissions.put(i++, PermissionNode.builder().permission("essentials.sethome.multiple.two").build());
        homePermissions.put(i++, PermissionNode.builder().permission("essentials.sethome.multiple.three").build());
        homePermissions.put(i++, PermissionNode.builder().permission("essentials.sethome.multiple.four").build());
        homePermissions.put(i++, PermissionNode.builder().permission("essentials.sethome.multiple.five").build());
        homePermissions.put(i++, PermissionNode.builder().permission("essentials.sethome.multiple.six").build());
        homePermissions.put(i++, PermissionNode.builder().permission("essentials.sethome.multiple.seven").build());
        homePermissions.put(i++, PermissionNode.builder().permission("essentials.sethome.multiple.eight").build());
        homePermissions.put(i++, PermissionNode.builder().permission("essentials.sethome.multiple.nine").build());
        homePermissions.put(i++, PermissionNode.builder().permission("essentials.sethome.multiple.ten").build());
        homePermissions.put(i++, PermissionNode.builder().permission("essentials.sethome.multiple.eleven").build());
        homePermissions.put(i++, PermissionNode.builder().permission("essentials.sethome.multiple.twelve").build());
        homePermissions.put(i++, PermissionNode.builder().permission("essentials.sethome.multiple.thirteen").build());
        homePermissions.put(i++, PermissionNode.builder().permission("essentials.sethome.multiple.fourteen").build());
        homePermissions.put(i++, PermissionNode.builder().permission("essentials.sethome.multiple.fifteen").build());


    }
    private void setHomeLimit(Player player, int i) {
        if (getHomeLimit(player) != 0) {
            LuckPermsUtil.removePermission(player, homePermissions.get(i).toString());
        }
        LuckPermsUtil.addPermission(player, "essentials.sethome.multiple." + i);
    }
    private void incrementHomeLimit(Player player) {
        int limit = getHomeLimit(player);
        setHomeLimit(player, ++limit);
    }
    private int getHomeLimit(Player player) {
        User user = LuckPermsUtil.getUser(player);
        for (int i = 2; i <= 15; i ++) {
            if (user.getNodes().contains(homePermissions.get(i))) {
                return i;
            }
        }

        return 0;
    }

}
