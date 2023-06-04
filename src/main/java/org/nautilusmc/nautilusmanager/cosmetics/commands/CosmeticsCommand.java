package org.nautilusmc.nautilusmanager.cosmetics.commands;

import com.google.common.collect.ImmutableMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.ChatFormatting;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.NautilusCommand;
import org.nautilusmc.nautilusmanager.cosmetics.CosmeticsMenu;
import org.nautilusmc.nautilusmanager.cosmetics.NameColor;
import org.nautilusmc.nautilusmanager.cosmetics.Nickname;
import org.nautilusmc.nautilusmanager.util.FancyText;

import java.util.*;

public class CosmeticsCommand extends NautilusCommand {

    // setting name : # args
    private static final Map<String, Integer> SETTINGS = ImmutableMap.of(
            "color", 2,
            "nickname", 1
    );

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length < 1) {
            commandSender.sendMessage(getUsageMessage(strings));
            return true;
        }

        switch (strings[0]) {
            case "menu" -> CosmeticsMenu.openMenu(commandSender, strings);
            case "set" -> setCosmetic(commandSender, strings);
            case "clear" -> clearCosmetic(commandSender, strings);
            default -> commandSender.sendMessage(getUsageMessage(strings));
        }

        return true;
    }

    private Component getUsageMessage(String[] args) {
        StringBuilder out = new StringBuilder();

        if (args.length < 1) {
            out.append("--------------------------------\n");
            out.append("/cosmetics menu [args] - Opens cosmetics menu\n");
            out.append("/cosmetics set [args] - Sets player cosmetic options\n");
            out.append("/cosmetics clear - Clears player cosmetic options\n");
            out.append("--------------------------------");
        } else {
            out.append("Usage: /cosmetics ");
            switch (args[0]) {
                case "set" -> {
                    if (args.length < 2) {
                        out.append("set <");
                        for (int i = 0; i < SETTINGS.size(); i++) {
                            out.append(SETTINGS.keySet().stream().toList().get(i));
                            if (i != SETTINGS.size() - 1) out.append("|");
                        }
                        out.append("> [args]");
                    } else {
                        switch (args[1]) {
                            case "color" -> {
                                out.append("set color <");
                                for (FancyText.ColorType type : FancyText.ColorType.values()) {
                                    out.append(type.name().toLowerCase()).append("|");
                                }
                                out = new StringBuilder(out.substring(0, out.length() - 1) + "> <color> [color 2]");
                            }
                            case "nickname" -> out.append("set nickname <nickname>");
                        }
                    }
                }
                case "menu" -> out.append("menu");
                case "clear" -> {
                    out.append("clear <");
                    for (int i = 0; i < SETTINGS.size(); i++) {
                        out.append(SETTINGS.keySet().stream().toList().get(i));
                        if (i != SETTINGS.size() - 1) out.append("|");
                    }
                    out.append(">");
                }
                default -> out.append("<menu|set|clear> [args]");
            }
        }


        return Component.text(out.toString()).color(TextColor.color(186, 186, 186));
    }

    private void setCosmetic(CommandSender sender, String[] strings) {
        if (strings.length < 2) {
            sender.sendMessage(getUsageMessage(strings));
            return;
        };

        Player player = null;

        int playerIndex = SETTINGS.containsKey(strings[1]) ? SETTINGS.get(strings[1]) + 3 : -1;

        Optional<FancyText.ColorType> colorType = Optional.empty();
        if (playerIndex != -1 && strings.length > 2 && strings[1].equalsIgnoreCase("color")) {
            if ((colorType = Arrays.stream(FancyText.ColorType.values()).filter(t->t.name().equalsIgnoreCase(strings[2])).findFirst()).isEmpty()) {
                sender.sendMessage(getUsageMessage(strings));
                return;
            }
            playerIndex += colorType.get().numColors - 1;
        }

        if (playerIndex != -1 && strings.length >= playerIndex) {
            if (!sender.hasPermission(Permission.MODIFY_OTHER_PLAYERS)) {
                sender.sendMessage(ErrorMessage.NO_PERMISSION);
                return;
            }

            if ((player = Bukkit.getPlayerExact(strings[playerIndex - 1])) == null) {
                sender.sendMessage(ErrorMessage.INVALID_PLAYER);
                return;
            }
        }

        if (player == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ErrorMessage.NOT_PLAYER);
                return;
            }
            player = (Player) sender;
        }

        if (strings[1].equalsIgnoreCase("color")) {
            if (colorType.isEmpty() || strings.length < 3 + colorType.get().numColors) {
                sender.sendMessage(getUsageMessage(strings));
                return;
            }

            Component hasAccess = colorType.get().hasAccess(sender);
            if (hasAccess != null) {
                sender.sendMessage(hasAccess.color(Default.ERROR_COLOR));
                return;
            }

            net.minecraft.network.chat.TextColor[] colors = new net.minecraft.network.chat.TextColor[colorType.get().numColors];
            for (int i = 0; i < colors.length; i++) {
                colors[i] = net.minecraft.network.chat.TextColor.parseColor(strings[3+i].toLowerCase().replace(' ', '_'));
            }

            if (Arrays.stream(colors).anyMatch(Objects::isNull)) {
                sender.sendMessage(ErrorMessage.INVALID_COLOR);
                return;
            }

            NameColor.setNameColor(player, colorType.get(), true, Arrays.stream(colors).map(c->TextColor.color(c.getValue())).toArray(TextColor[]::new));

            return;
        } else if (strings[1].equalsIgnoreCase("nickname")) {
            if (!sender.hasPermission(Permission.NICKNAME)) {
                sender.sendMessage(ErrorMessage.NOT_SPONSOR);
                return;
            }

            if (strings.length < 3) {
                sender.sendMessage(getUsageMessage(strings));
                return;
            }

            String error = Nickname.validateNickname(player, strings[2]);
            if (error != null) {
                sender.sendMessage(Component.text(error).color(Default.ERROR_COLOR));
                return;
            }

            Nickname.setNickname(player, strings[2], true);

            return;
        }

        sender.sendMessage(getUsageMessage(strings));
    }

    private void clearCosmetic(CommandSender sender, String[] strings) {
        if (strings.length < 2) {
            sender.sendMessage(getUsageMessage(strings));
            return;
        }

        Player player = null;
        if (strings[0].equalsIgnoreCase("clear")) {
            if (strings.length > 2) {
                if (!sender.hasPermission(Permission.MODIFY_OTHER_PLAYERS)) {
                    sender.sendMessage(ErrorMessage.NO_PERMISSION);
                    return;
                }

                if ((player = Bukkit.getPlayerExact(strings[2])) == null) {
                    sender.sendMessage(ErrorMessage.INVALID_PLAYER);
                    return;
                }
            }
        }

        if (player == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ErrorMessage.NOT_PLAYER);
                return;
            }
            player = (Player) sender;
        }

        if (strings[1].equalsIgnoreCase("color")) {
            NameColor.setNameColor(player, true, NameColor.DEFAULT_COLOR);
            return;
        } else if (strings[1].equalsIgnoreCase("nickname")) {
            Nickname.setNickname(player, player.getName(), true);
            return;
        }

        sender.sendMessage(getUsageMessage(strings));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> out = new ArrayList<>();

        if (strings.length == 1) {
            out.add("menu");
            out.add("set");
            out.add("clear");
        } else if (strings.length == 2) {
            if (strings[0].equalsIgnoreCase("set") || strings[0].equalsIgnoreCase("clear")) {
                out.addAll(SETTINGS.keySet());
            }
        } else if (strings.length == 3) {
            if (strings[0].equalsIgnoreCase("set")) {
                if (strings[1].equalsIgnoreCase("color")) {
                    for (FancyText.ColorType type : FancyText.ColorType.values()) {
                        if (type.hasAccess(commandSender) == null) out.add(type.name().toLowerCase());
                    }
                }
            }
        }

        int idx = strings.length >= 2 && SETTINGS.containsKey(strings[1]) ? SETTINGS.get(strings[1])+3 : -1;

        Optional<FancyText.ColorType> type;
        if (idx >= 0 && strings.length > 2 && strings[1].equalsIgnoreCase("color") &&
                (type = Arrays.stream(FancyText.ColorType.values()).filter(t -> t.name().equalsIgnoreCase(strings[2])).findFirst()).isPresent()) {
            idx += type.get().numColors - 1;
            if (strings.length - 3 <= type.get().numColors) {
                out.addAll(Arrays.stream(ChatFormatting.values()).filter(ChatFormatting::isColor).map(ChatFormatting::getName).toList());
                out.add("#FFFFFF");
            }
        }

        if (idx >= 0 && (strings[0].equalsIgnoreCase("set") || strings[0].equalsIgnoreCase("clear")) && strings.length == idx &&
                commandSender.hasPermission(Permission.MODIFY_OTHER_PLAYERS)) {
            out.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
        }

        return out.stream().filter(str -> str.toLowerCase().startsWith(strings[strings.length - 1].toLowerCase())).toList();
    }
}
