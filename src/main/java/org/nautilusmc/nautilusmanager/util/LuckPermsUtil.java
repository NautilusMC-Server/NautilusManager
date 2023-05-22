package org.nautilusmc.nautilusmanager.util;

import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.entity.Player;
import org.nautilusmc.nautilusmanager.NautilusManager;

public class LuckPermsUtil {
    public static void addPermission(Player player, String permission) {
        // Add the permission
        User user = getUser(player);
        user.data().add(Node.builder(permission).build());

        // Now we need to save changes.
        NautilusManager.LUCKPERMS.getUserManager().saveUser(user);
    }

    public static void removePermission(Player player, String permission) {
        User user = getUser(player);
        Node perm = Node.builder(permission).build();
        user.getNodes().remove(perm);
    }

    public static User getUser(Player player) {
        return NautilusManager.LUCKPERMS.getPlayerAdapter(Player.class).getUser(player);
    }
}
