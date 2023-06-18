package org.nautilusmc.nautilusmanager.cosmetics.commands;

import com.google.common.collect.ImmutableMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.ChatFormatting;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.Command;
import org.nautilusmc.nautilusmanager.cosmetics.CosmeticsMenu;
import org.nautilusmc.nautilusmanager.cosmetics.NameColor;
import org.nautilusmc.nautilusmanager.cosmetics.Nickname;
import org.nautilusmc.nautilusmanager.util.FancyText;
import org.nautilusmc.nautilusmanager.util.Permission;

import java.util.*;

public class CosmeticsCommand extends Command {
    public static final Component INVALID_COLOR_ERROR = Component.text("Invalid color!").color(ERROR_COLOR);
    
    private static final Map<String, Integer> SETTINGS_ARG_COUNTS = ImmutableMap.of(
            "color", 2,
            "nickname", 1
    );

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(getUsageMessage(args));
            return true;
        }

        switch (args[0]) {
            case "menu" -> CosmeticsMenu.openMenu(sender, args);
            case "set" -> setCosmetic(sender, args);
            case "clear" -> clearCosmetic(sender, args);
            default -> sender.sendMessage(getUsageMessage(args));
        }

        return true;
    }

    public Component getUsageMessage(String[] args) {
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
                        for (int i = 0; i < SETTINGS_ARG_COUNTS.size(); i++) {
                            out.append(SETTINGS_ARG_COUNTS.keySet().stream().toList().get(i));
                            if (i != SETTINGS_ARG_COUNTS.size() - 1) out.append("|");
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
                    for (int i = 0; i < SETTINGS_ARG_COUNTS.size(); i++) {
                        out.append(SETTINGS_ARG_COUNTS.keySet().stream().toList().get(i));
                        if (i != SETTINGS_ARG_COUNTS.size() - 1) out.append("|");
                    }
                    out.append(">");
                }
                default -> out.append("<menu|set|clear> [args]");
            }
        }


        return Component.text(out.toString(), INFO_COLOR);
    }

    private void setCosmetic(CommandSender sender, String[] strings) {
        if (strings.length < 2) {
            sender.sendMessage(getUsageMessage(strings));
            return;
        };

        Player player = null;

        int playerIndex = SETTINGS_ARG_COUNTS.containsKey(strings[1]) ? SETTINGS_ARG_COUNTS.get(strings[1]) + 3 : -1;

        Optional<FancyText.ColorType> colorType = Optional.empty();
        if (playerIndex != -1 && strings.length > 2 && strings[1].equalsIgnoreCase("color")) {
            if ((colorType = Arrays.stream(FancyText.ColorType.values())
                    .filter(t -> t.name().equalsIgnoreCase(strings[2]))
                    .findFirst())
                    .isEmpty()) {
                sender.sendMessage(getUsageMessage(strings));
                return;
            }
            playerIndex += colorType.get().numColors - 1;
        }

        if (playerIndex != -1 && strings.length >= playerIndex) {
            if (!sender.hasPermission(Permission.MODIFY_OTHER_PLAYERS.toString())) {
                sender.sendMessage(NO_PERMISSION_ERROR);
                return;
            }

            if ((player = Bukkit.getPlayer(strings[playerIndex - 1])) == null) {
                sender.sendMessage(INVALID_PLAYER_ERROR);
                return;
            }
        }

        if (player == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(NOT_PLAYER_ERROR);
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
                sender.sendMessage(hasAccess.color(ERROR_COLOR));
                return;
            }

            net.minecraft.network.chat.TextColor[] colors = new net.minecraft.network.chat.TextColor[colorType.get().numColors];
            for (int i = 0; i < colors.length; i++) {
                colors[i] = net.minecraft.network.chat.TextColor.parseColor(strings[3+i].toLowerCase().replace(' ', '_'));
            }

            if (Arrays.stream(colors).anyMatch(Objects::isNull)) {
                sender.sendMessage(INVALID_COLOR_ERROR);
                return;
            }

            NameColor.setNameColor(player, colorType.get(), true, Arrays.stream(colors).map(c -> TextColor.color(c.getValue())).toArray(TextColor[]::new));

            return;
        } else if (strings[1].equalsIgnoreCase("nickname")) {
            if (!sender.hasPermission(Permission.NICKNAME.toString())) {
                sender.sendMessage(Command.NOT_SPONSOR_ERROR);
                return;
            }

            if (strings.length < 3) {
                sender.sendMessage(getUsageMessage(strings));
                return;
            }

            String error = Nickname.validateNickname(player, strings[2]);
            if (error != null) {
                sender.sendMessage(Component.text(error).color(ERROR_COLOR));
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
                if (!sender.hasPermission(Permission.MODIFY_OTHER_PLAYERS.toString())) {
                    sender.sendMessage(Command.NO_PERMISSION_ERROR);
                    return;
                }

                if ((player = Bukkit.getPlayer(strings[2])) == null) {
                    sender.sendMessage(Command.INVALID_PLAYER_ERROR);
                    return;
                }
            }
        }

        if (player == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Command.NOT_PLAYER_ERROR);
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
    public @Nullable List<String> suggestionList(@NotNull CommandSender sender, @NotNull String[] args) {
        List<String> out = new ArrayList<>();

        if (args.length == 1) {
            out.add("menu");
            out.add("set");
            out.add("clear");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("clear")) {
                out.addAll(SETTINGS_ARG_COUNTS.keySet());
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("color")) {
                for (FancyText.ColorType type : FancyText.ColorType.values()) {
                    if (type.hasAccess(sender) == null) out.add(type.name().toLowerCase());
                }
                out.addAll(Arrays.stream(FancyText.ColorType.values())
                        .filter(type -> type.hasAccess(sender) == null)
                        .map(type -> type.name().toLowerCase())
                        .toList());
            }
        }

        int idx = args.length >= 2 && SETTINGS_ARG_COUNTS.containsKey(args[1]) ? SETTINGS_ARG_COUNTS.get(args[1])+3 : -1;

        Optional<FancyText.ColorType> type;
        if (idx >= 0 && args.length > 2 && args[1].equalsIgnoreCase("color") &&
                (type = Arrays.stream(FancyText.ColorType.values())
                        .filter(t -> t.name().equalsIgnoreCase(args[2]))
                        .findFirst())
                        .isPresent()) {
            idx += type.get().numColors - 1;
            if (args.length - 3 <= type.get().numColors) {
                out.addAll(Arrays.stream(ChatFormatting.values())
                        .filter(ChatFormatting::isColor)
                        .map(ChatFormatting::getName)
                        .toList());
                out.add("#ffffff");
            }
        }

        if (idx >= 0 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("clear")) && args.length == idx &&
                sender.hasPermission(Permission.MODIFY_OTHER_PLAYERS.toString())) {
            out.addAll(getOnlineNames());
        }

        return out;
    }
}
