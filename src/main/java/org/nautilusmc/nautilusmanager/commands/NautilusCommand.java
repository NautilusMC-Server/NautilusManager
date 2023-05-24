package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public abstract class NautilusCommand implements CommandExecutor, TabCompleter {

    public static final TextColor ERROR_COLOR = TextColor.color(255, 42, 52);
    public static final TextColor DEFAULT_CHAT_TEXT_COLOR = TextColor.color(200, 200, 200);
    public static final String DEFAULT_PERM_MESSAGE = "You do not have permission to use that command!";
    public static final String SPONSOR_PERM_MESSAGE = "Become a sponsor to unlock!";

    //cosmetic
    public static final String MODIFY_OTHER_PERM = "nautiluscosmetics.modify_other";
    public static final String NICKNAME_PERM = "nautiluscosmetics.nickname";
    public static final String NICKNAME_LIST_PERM = "nautiluscosmetics.nickname.view";
    public static final String NICKNAME_SPECIAL_CHAR_PERM = "nautiluscosmetics.nickname.special_characters";
    public static final String CHAT_FORMATTING_PERM = "nautiluscosmetics.chat_formatting";
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
    public static final String LIST_CREW_PERM = "nautilusmanager.crew.list";
    public static final String DELETE_CREW_PERM = "nautilusmanager.crew.delete";
    public static final String KICK_CREW_PERM = "nautilusmanager.crew.kick";
    public static final String CLOSE_CREW_PERM = "nautilusmanager.crew.close";
    public static final String OPEN_CREW_PERM = "nautilusmanager.crew.open";
    public static final String MAKECAPTAIN_CREW_PERM = "nautilusmanager.crew.makecaptain";
    public static final String CREW_INVITE_PERM = "nautilusmanager.crew.invite";
    public static final String CREW_INFO_PERM = "nautilusmanager.crew.invite";

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
}
