package org.nautilusmc.nautilusmanager.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.events.MessageStyler;

import java.util.List;

public class LanguageCommand extends NautilusCommand {

    private final List<String> codes = List.of("bg","cs","da","de","el","en-US","es","et","fi","fr","hu","id","it","ja","ko","lt","lv",
            "nb","nl","pl","pt-BR","ro","ru","sk","sl","sv","tr","uk","zh");

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(!(commandSender instanceof Player player)) {
            commandSender.sendMessage("You must be a player to use this command.");
            return true;
        }
        if(strings.length == 0) {
            error(player, "Must include a language code. For example, /language en to switch to English.");
            return true;
        }
        if(!codes.contains(strings[0])) {
            error(player, "Must include a language code. For example, /language en to switch to English.");
            return true;
        }

        MessageStyler.setLanguage(player, strings[0]);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return codes;
    }
}
