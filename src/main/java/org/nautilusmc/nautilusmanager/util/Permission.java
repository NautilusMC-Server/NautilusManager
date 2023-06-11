package org.nautilusmc.nautilusmanager.util;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public enum Permission {
    // Cosmetic
    MODIFY_OTHER_PLAYERS("nautiluscosmetics.modify_other"),
    SET_ITEM_NAME("nautiluscosmetics.set_item_name"),
    INVISIBLE_FRAMES_STANDS("nautiluscosmetics.item_frame_armor_stand_invisibility"),
    NICKNAME("nautiluscosmetics.nickname"),
    NICKNAME_LIST("nautiluscosmetics.nickname.view"),
    NICKNAME_SPECIAL_CHARS("nautiluscosmetics.nickname.special_characters"),
    USE_CHAT_FORMATTING("nautiluscosmetics.chat.formatting"),
    PERSONAL_MUTE("nautiluscosmetics.personal_mute"),
    /* Color Type Permissions: nautiluscosmetics.color.[name] */
    // Teleport
    TPA("nautilusmanager.tpa"),
    USE_HOMES("nautilusmanager.home"),
    USE_WARPS("nautilusmanager.warp"),
    MANAGE_WARPS("nautilusmanager.warp.create"),
    // Crews
    CREATE_CREW("nautilusmanager.crew.create"),
    JOIN_CREW("nautilusmanager.crew.join"),
    LEAVE_CREW("nautilusmanager.crew.leave"),
    DELETE_CREW("nautilusmanager.crew.delete"),
    KICK_CREWMATES("nautilusmanager.crew.kick"),
    CLOSE_CREW("nautilusmanager.crew.close"),
    OPEN_CREW("nautilusmanager.crew.open"),
    MAKE_CAPTAIN("nautilusmanager.crew.makecaptain"),
    INVITE_TO_CREW("nautilusmanager.crew.invite"),
    CLOSED_INVITE_TO_CREW("nautilusmanager.crew.closedinvite"),
    CREW_INFO("nautilusmanager.crew.info"),
    DELETE_OTHER_CREWS("nautilusmanager.crew.delete.other"),
    DECLARE_WAR("nautilusmanager.crew.declarewar"),
    SET_CREW_PREFIX("nautilusmanager.crew.setprefix"),
    CLEAR_CREW_PREFIX("nautilusmanager.crew.clearprefix"),
    // Other
    DENY("nautilusmanager.deny"),
    CONFIRM("nautilusmanager.confirm"),
    // Administration
    EDIT_SPAWN("nautilusmanager.edit_spawn"),
    VANISH("nautilusmanager.vanish"),
    BAN("nautilusmanager.ban"),
    STAFF_CHAT("nautilusmanager.chat.staff"),
    RELOAD("nautilusmanager.reload");

    public final String key;

    Permission(String key) {
        this.key = key;
    }

    public String toString() {
        return key;
    }

    public static LuckPerms LUCKPERMS;

    public static void init() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LUCKPERMS = provider.getProvider();
        }
    }

    public static void addGroup(OfflinePlayer player, String group) {
        User user = getUser(player);
        user.data().add(InheritanceNode.builder(group).build());
        save(user);
    }

    public static boolean removeGroup(OfflinePlayer player, String group) {
        User user = getUser(player);
        if (!user.getNodes().contains(InheritanceNode.builder(group).build())) {
            return false;
        }
        user.data().remove(InheritanceNode.builder(group).build());
        save(user);
        return true;
    }

    private static User getUser(OfflinePlayer player) {
        return LUCKPERMS.getUserManager().getUser(player.getUniqueId());
    }

    private static void save(User user) {
        LUCKPERMS.getUserManager().saveUser(user);
    }
}
