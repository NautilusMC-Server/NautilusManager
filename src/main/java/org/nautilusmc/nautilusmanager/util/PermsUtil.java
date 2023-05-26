package org.nautilusmc.nautilusmanager.util;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class PermsUtil {
    public static LuckPerms LP;

    public static void init() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LP = provider.getProvider();
        }
    }

    public static void addGroup(Player player, String group) {
        User user = getUser(player);
        user.data().add(InheritanceNode.builder(group).build());
        save(user);
    }

    public static boolean removeGroup(Player player, String group) {
        User user = getUser(player);
        if (!player.hasPermission("group." + group)) {
            return false;
        }
        user.data().remove(InheritanceNode.builder(group).build());
        save(user);
        return true;
    }

    private static User getUser(Player player) {
        return LP.getUserManager().getUser(player.getUniqueId());
    }

    private static void save(User user) {
        LP.getUserManager().saveUser(user);
    }
}
