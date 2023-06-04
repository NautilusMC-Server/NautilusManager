package org.nautilusmc.nautilusmanager.util;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class PermsUtil {
    public static LuckPerms LUCKPERMS;

    public static void init() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LUCKPERMS = provider.getProvider();
        }
    }

    public static void addGroup(OfflinePlayer player, String group) {
        User user = getUser(player);
        if (user == null) {
            return;
        }
        user.data().add(InheritanceNode.builder(group).build());
        save(user);
    }

    public static boolean removeGroup(OfflinePlayer player, String group) {
        User user = getUser(player);
        if (user == null) {
            return false;
        }
        if (!user.getNodes().contains(InheritanceNode.builder(group).build())) {
            return false;
        }
        user.data().remove(InheritanceNode.builder(group).build());
        save(user);
        return true;
    }

    private static User getUser(OfflinePlayer player) {
        if (player == null) {
            return null;
        }
        return LUCKPERMS.getUserManager().getUser(player.getUniqueId());
    }

    private static void save(User user) {
        LUCKPERMS.getUserManager().saveUser(user);
    }
}
