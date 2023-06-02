package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.Collection;

public abstract class NautilusCommand implements CommandExecutor, TabCompleter {

    public static final TextColor ERROR_COLOR = TextColor.color(255, 42, 52);
    public static final TextColor MAIN_COLOR = TextColor.color(255, 188, 0);
    public static final TextColor ACCENT_COLOR = TextColor.color(255, 252, 162);

    public static final String DEFAULT_PERM_MESSAGE = "You do not have permission to use that command!";
    public static final String SPONSOR_PERM_MESSAGE = "Become a sponsor to unlock!";

    //cosmetic
    public static final String MODIFY_OTHER_PERM = "nautiluscosmetics.modify_other";
    public static final String ITEM_NAME_PERM = "nautiluscosmetics.set_item_name";
    public static final String ITEM_FRAME_ARMOR_STAND_INVISIBILITY_PERM = "nautiluscosmetics.item_frame_armor_stand_invisibility";
    public static final String MUTE_PERM = "nautiluscosmetics.personal_mute";
    public static final String NICKNAME_PERM = "nautiluscosmetics.nickname";
    public static final String NICKNAME_LIST_PERM = "nautiluscosmetics.nickname.view";
    public static final String NICKNAME_SPECIAL_CHAR_PERM = "nautiluscosmetics.nickname.special_characters";
    public static final String CHAT_FORMATTING_PERM = "nautiluscosmetics.chat.formatting";
    public static final String VANISH_PERM = "nautilusmanager.vanish";
    public static final String BAN_PERM = "nautilusmanager.ban";
    public static final String STAFF_CHAT_PERM = "nautilusmanager.chat.staff";
    public static final String RELOAD_PERM = "nautilusmanager.reload";

    //teleport
    public static final String TPA_PERM = "nautilusmanager.tpa";
    public static final String HOMES_PERM = "nautilusmanager.home";
    public static final String WARP_TO_WARPS_PERM = "nautilusmanager.warp";
    public static final String CREATE_WARPS_PERM = "nautilusmanager.warp.create";

    //crews
    public static final String CREATE_CREW_PERM = "nautilusmanager.crew.create";
    public static final String JOIN_CREW_PERM = "nautilusmanager.crew.join";
    public static final String LEAVE_CREW_PERM = "nautilusmanager.crew.leave";
    public static final String DELETE_CREW_PERM = "nautilusmanager.crew.delete";
    public static final String KICK_CREW_PERM = "nautilusmanager.crew.kick";
    public static final String CLOSE_CREW_PERM = "nautilusmanager.crew.close";
    public static final String OPEN_CREW_PERM = "nautilusmanager.crew.open";
    public static final String MAKECAPTAIN_CREW_PERM = "nautilusmanager.crew.makecaptain";
    public static final String CREW_INVITE_PERM = "nautilusmanager.crew.invite";
    public static final String CREW_CLOSED_INVITE_PERM = "nautilusmanager.crew.closedinvite";
    public static final String CREW_INFO_PERM = "nautilusmanager.crew.info";
    public static final String DELETE_OTHER_CREW_PERM = "nautilusmanager.crew.delete.other";
    public static final String DECLARE_WAR_PERM = "nautilusmanager.crew.declarewar";
    public static final String SET_PREFIX_PERM = "nautilusmanager.crew.setprefix";
    public static final String CLEAR_PREFIX_PERM = "nautilusmanager.crew.clearprefix";
    public static final String END_WAR_PERM = "nautilusmanager.crew.endwar";

    //Other
    public static final String DENY_PERM = "nautilusmanager.deny";
    public static final String CONFIRM_PERM = "nautilusmanager.confirm";



    /*
     * Color Type Permissions: nautiluscosmetics.color.[name]
     */

    // TODO: not worth it to do this the night before launch,
    //  but in the future use this class to standardize commands

    protected static String getFormattedArgs(String[] args, int index) {
        String out = "";
        if (index > args.length - 1) {
            return "";
        }
        for (int i = index; i < args.length; i++) {
            out += args[i];
            if (i < args.length - 1) {
                out += " ";
            }
        }
        return out;
    }

    protected static void error(Player player, String message) {
        player.sendMessage(Component.text(message).color(ERROR_COLOR));
    }
    public static Collection<String> getOnlineNames() {
        return Bukkit.getOnlinePlayers().stream().map(Util::getName).toList();
    }
}
