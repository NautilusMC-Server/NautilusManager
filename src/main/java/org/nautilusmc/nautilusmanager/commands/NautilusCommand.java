package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.util.Util;

import java.util.ArrayList;
import java.util.List;

public abstract class NautilusCommand implements CommandExecutor, TabCompleter {

    public static class Default {
        public static final TextColor INFO_COLOR = TextColor.color(255, 188, 0);
        public static final TextColor INFO_ACCENT_COLOR = TextColor.color(255, 252, 162);
        public static final TextColor ERROR_COLOR = TextColor.color(255, 42, 52);
        public static final TextColor ERROR_ACCENT_COLOR = TextColor.color(255, 123, 130);
    }
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

    public static class ErrorMessage {
        public static final Component NOT_PLAYER = Component.text("You must be a player to use that command!").color(Default.ERROR_COLOR);
        public static final Component NO_PERMISSION = Component.text("You do not have permission to use that command!").color(Default.ERROR_COLOR);
        public static final Component NOT_SPONSOR = Component.text("Become a sponsor to unlock that command! (")
                .append(Util.clickableCommand("/sponsor", true).color(Default.ERROR_ACCENT_COLOR))
                .append(Component.text(")"))
                .color(Default.ERROR_COLOR);
        public static final Component INVALID_PAGE_NUMBER = Component.text("Invalid page number!").color(Default.ERROR_COLOR);
        // Cosmetic
        public static final Component INVALID_COLOR = Component.text("Invalid color!").color(Default.ERROR_COLOR);
        public static final Component NICKNAME_CONFLICT_RESET = Component.text("Your nickname was reset because a player by that name has joined.").color(Default.ERROR_COLOR);
        // Teleport
        public static final Component INVALID_PLAYER = Component.text("Player not found!").color(Default.ERROR_COLOR);
        public static final Component CANNOT_TP_TO_SELF = Component.text("You can't teleport to yourself!").color(Default.ERROR_COLOR);
        public static final Component NO_PREV_LOCATION = Component.text("Nowhere to return to!").color(Default.ERROR_COLOR);
        public static final Component PENDING_TP_REQUEST = Component.text("You already have a pending request!").color(Default.ERROR_COLOR);
        public static final Component NO_PENDING_TP_REQUEST = Component.text("No pending request found!").color(Default.ERROR_COLOR);
        public static final Component NO_OUTGOING_TP_REQUEST = Component.text("You don't have an outgoing request!").color(Default.ERROR_COLOR);
        public static final Component NO_SPAWN_SET = Component.text("Spawn has not been set!").color(Default.ERROR_COLOR);
        // Crews
        public static final Component NOT_CAPTAIN = Component.text("You must be a captain to use this command!").color(Default.ERROR_COLOR);
        public static final Component NOT_IN_CREW = Component.text("You must be part of a crew to use this command!").color(Default.ERROR_COLOR);
        public static final Component ALREADY_IN_CREW = Component.text("You are already in a crew!").color(Default.ERROR_COLOR);
        public static final Component NO_PENDING_INVITES = Component.text("No pending invites!").color(Default.ERROR_COLOR);
        public static final Component NO_PENDING_WARS = Component.text("No pending war declarations!").color(Default.ERROR_COLOR);
        // Other
        public static final Component NO_PENDING_CONFIRM = Component.text("No pending tasks to confirm!").color(Default.ERROR_COLOR);
        public static final Component NO_PENDING_DENY = Component.text("No pending tasks to deny!").color(Default.ERROR_COLOR);
    }

    public static class Permission {
        // Cosmetic
        public static final String MODIFY_OTHER_PLAYERS = "nautiluscosmetics.modify_other";
        public static final String SET_ITEM_NAME = "nautiluscosmetics.set_item_name";
        public static final String NICKNAME = "nautiluscosmetics.nickname";
        public static final String NICKNAME_LIST = "nautiluscosmetics.nickname.view";
        public static final String NICKNAME_SPECIAL_CHARS = "nautiluscosmetics.nickname.special_characters";
        public static final String USE_CHAT_FORMATTING = "nautiluscosmetics.chat.formatting";
        // Teleport
        public static final String TPA = "nautilusmanager.tpa";
        public static final String USE_HOMES = "nautilusmanager.home";
        public static final String USE_WARPS = "nautilusmanager.warp";
        public static final String CREATE_WARPS = "nautilusmanager.warp.create";
        // Crews
        public static final String CREATE_CREW = "nautilusmanager.crew.create";
        public static final String JOIN_CREW = "nautilusmanager.crew.join";
        public static final String LEAVE_CREW = "nautilusmanager.crew.leave";
        public static final String DELETE_CREW = "nautilusmanager.crew.delete";
        public static final String KICK_CREWMATES = "nautilusmanager.crew.kick";
        public static final String CLOSE_CREW = "nautilusmanager.crew.close";
        public static final String OPEN_CREW = "nautilusmanager.crew.open";
        public static final String MAKE_CAPTAIN = "nautilusmanager.crew.makecaptain";
        public static final String INVITE_TO_CREW = "nautilusmanager.crew.invite";
        public static final String CLOSED_INVITE_TO_CREW = "nautilusmanager.crew.closedinvite";
        public static final String CREW_INFO = "nautilusmanager.crew.info";
        public static final String DELETE_OTHER_CREWS = "nautilusmanager.crew.delete.other";
        public static final String DECLARE_WAR = "nautilusmanager.crew.declarewar";
        public static final String SET_CREW_PREFIX = "nautilusmanager.crew.setprefix";
        public static final String CLEAR_CREW_PREFIX = "nautilusmanager.crew.clearprefix";
        // Other
        public static final String DENY = "nautilusmanager.deny";
        public static final String CONFIRM = "nautilusmanager.confirm";
        // Staff-only
        public static final String VANISH = "nautilusmanager.vanish";
        public static final String STAFF_CHAT = "nautilusmanager.chat.staff";
        public static final String RELOAD = "nautilusmanager.reload";
    }

    /*
     * Color Type Permissions: nautiluscosmetics.color.[name]
     */

    protected static String getFormattedArgs(String[] args, int start) {
        StringBuilder out = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            out.append(args[i]);
            if (i < args.length - 1) {
                out.append(" ");
            }
        }
        return out.toString();
    }

    public static List<String> getOnlineUsernames() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }

    public static List<String> getOnlineNames() {
        return Bukkit.getOnlinePlayers().stream().map(Util::getName).toList();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}
