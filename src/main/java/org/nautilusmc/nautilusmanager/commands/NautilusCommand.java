package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

public abstract class NautilusCommand implements CommandExecutor, TabCompleter {

    public static final TextColor ERROR_COLOR = TextColor.color(255, 42, 52);
    public static final TextColor DEFAULT_CHAT_TEXT_COLOR = TextColor.color(200, 200, 200);

    public static final String SPONSOR_PERM_MESSAGE = "Become a sponsor to unlock!";
    public static final String MODIFY_OTHER_PERM = "nautiluscosmetics.modify_other";
    public static final String NICKNAME_PERM = "nautiluscosmetics.nickname";
    public static final String NICKNAME_SPECIAL_CHAR_PERM = "nautiluscosmetics.nickname.special_characters";
    public static final String CHAT_FORMATTING_PERM = "nautiluscosmetics.chat_formatting";

    /*
     * Color Type Permissions: nautiluscosmetics.color.[name]
     */

    // TODO: not worth it to do this the night before launch,
    //  but in the future use this class to standardize commands
}
